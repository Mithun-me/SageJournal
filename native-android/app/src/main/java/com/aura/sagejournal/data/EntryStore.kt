package com.aura.sagejournal.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aura.sagejournal.domain.BloomDay
import com.aura.sagejournal.domain.DayState
import com.aura.sagejournal.domain.TrendPoint
import com.aura.sagejournal.domain.moodClarityScore
import com.aura.sagejournal.domain.JournalEntry
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.domain.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "entries")
data class EntryRow(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val mood: String,
    val createdAt: Long,
    val isFavorite: Boolean = false,
    val location: String? = null,
    val words: Int = 0,
)

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EntryRow>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun byId(id: String): EntryRow?

    @Upsert
    suspend fun upsert(row: EntryRow)

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM entries")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM entries")
    suspend fun count(): Int
}

/**
 * Derived counters. Scoring matches the web build's rule rather than a new
 * one, so a migrated journal keeps roughly the same total: App.tsx awarded
 * +50 on save and +10 on a mood tap.
 */
data class AuraStats(
    val streakDays: Int = 0,
    val points: Int = 0,
    val entryCount: Int = 0,
    val checkIns: Int = 0,
)

private const val POINTS_PER_ENTRY = 50
private const val POINTS_PER_CHECK_IN = 10

/** One row per calendar day: the mood you tapped and how many check-ins. */
@Entity(tableName = "days")
data class DayRow(
    @PrimaryKey val dayKey: String,
    val mood: String? = null,
    val checkIns: Int = 0,
)

@Dao
interface DayDao {
    @Query("SELECT * FROM days")
    fun observeAll(): Flow<List<DayRow>>

    @Query("SELECT * FROM days WHERE dayKey = :key")
    suspend fun byKey(key: String): DayRow?

    @Upsert
    suspend fun upsert(row: DayRow)

    @Query("DELETE FROM days")
    suspend fun clear()
}

@Database(entities = [EntryRow::class, DayRow::class], version = 2, exportSchema = false)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun entries(): EntryDao
    abstract fun days(): DayDao
}

/**
 * Adds the days table without touching entries. A destructive migration would
 * have been one line, but it would also delete the user's journal.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS days (" +
                "dayKey TEXT NOT NULL PRIMARY KEY, " +
                "mood TEXT, " +
                "checkIns INTEGER NOT NULL DEFAULT 0)"
        )
    }
}

/**
 * Owns the entries table and maps rows to the domain model.
 *
 * Dates are stored as epoch millis and formatted for display, rather than the
 * web build's approach of persisting "Today" and "08:12" as strings — those
 * stop being true the next morning.
 */
class EntryStore(context: Context) {
    private val db = Room
        .databaseBuilder(context.applicationContext, AuraDatabase::class.java, "aura.db")
        .addMigrations(MIGRATION_1_2)
        .build()

    private val dao = db.entries()
    private val dayDao = db.days()

    val entries: Flow<List<JournalEntry>> = dao.observeAll().map { rows ->
        rows.map { it.toDomain() }
    }

    suspend fun save(title: String, content: String, mood: Mood, location: String? = null) {
        val now = System.currentTimeMillis()
        dao.upsert(
            EntryRow(
                id = "entry-$now",
                title = title.ifBlank { "Untitled reflection" },
                content = content,
                mood = mood.name,
                createdAt = now,
                location = location,
                words = content.trim().split(Regex("\\s+")).count { it.isNotEmpty() },
            )
        )
    }

    /** Today's check-in row, or null before the first tap of the day. */
    val today: Flow<DayRow?> = dayDao.observeAll().map { rows ->
        rows.firstOrNull { it.dayKey == dayKey(System.currentTimeMillis()) }
    }

    /**
     * The heatmap, derived rather than seeded: mood comes from the day's
     * check-in and the dot size from words actually written that day.
     */
    val bloom: Flow<List<BloomDay>> = combine(
        dayDao.observeAll(),
        dao.observeAll(),
    ) { days, rows ->
        val byKey = days.associateBy { it.dayKey }
        val wordsByKey = rows.groupBy { dayKey(it.createdAt) }
            .mapValues { (_, v) -> v.sumOf { it.words } }
        val todayKey = dayKey(System.currentTimeMillis())

        windowOf35().map { key ->
            val day = byKey[key]
            BloomDay(
                label = key.takeLast(2),
                mood = day?.mood?.let { m -> runCatching { Mood.valueOf(m) }.getOrNull() },
                words = wordsByKey[key] ?: 0,
                state = when {
                    key == todayKey -> DayState.Today
                    key > todayKey -> DayState.Future
                    else -> DayState.Past
                },
            )
        }
    }

    /** Streak, points and totals, all derived from the two tables. */
    val stats: Flow<AuraStats> = combine(
        dayDao.observeAll(),
        dao.observeAll(),
    ) { days, rows ->
        val active = days.filter { it.checkIns > 0 }.map { it.dayKey }.toSet()
        val checkIns = days.sumOf { it.checkIns }
        AuraStats(
            streakDays = streakLength(active),
            points = rows.size * POINTS_PER_ENTRY + checkIns * POINTS_PER_CHECK_IN,
            entryCount = rows.size,
            checkIns = checkIns,
        )
    }

    /**
     * The Insights week, derived. Clarity comes from the day's mood via the
     * existing moodClarityScore map; points use the same rule as the totals.
     */
    val weekTrend: Flow<List<TrendPoint>> = combine(
        dayDao.observeAll(),
        dao.observeAll(),
    ) { days, rows ->
        val byKey = days.associateBy { it.dayKey }
        val entriesByKey = rows.groupBy { dayKey(it.createdAt) }

        lastSevenDays().map { (key, millis) ->
            val day = byKey[key]
            val mood = day?.mood?.let { m -> runCatching { Mood.valueOf(m) }.getOrNull() }
            val dayEntries = entriesByKey[key].orEmpty()
            TrendPoint(
                name = dowFmt.format(Date(millis)),
                fullDate = fullFmt.format(Date(millis)),
                mood = mood,
                clarityScore = mood?.let { moodClarityScore[it] } ?: 0,
                pointsEarned = dayEntries.size * POINTS_PER_ENTRY +
                    (day?.checkIns ?: 0) * POINTS_PER_CHECK_IN,
                entriesCount = dayEntries.size,
                notes = when {
                    dayEntries.isNotEmpty() -> dayEntries.first().title
                    mood != null -> "Checked in as ${mood.label.lowercase()}, nothing written"
                    else -> "No check-in that day"
                },
            )
        }
    }

    /** A mood tap: records the day's mood and counts the check-in, capped at 4. */
    suspend fun checkIn(mood: Mood) = withContext(Dispatchers.IO) {
        val key = dayKey(System.currentTimeMillis())
        val existing = dayDao.byKey(key)
        dayDao.upsert(
            DayRow(
                dayKey = key,
                mood = mood.name,
                checkIns = ((existing?.checkIns ?: 0) + 1).coerceAtMost(4),
            )
        )
    }

    suspend fun toggleFavourite(id: String) {
        val row = dao.byId(id) ?: return
        dao.upsert(row.copy(isFavorite = !row.isFavorite))
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        dao.clear()
        dayDao.clear()
        seed()
    }

    /** Seeds the sample journal the first time the app runs. */
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.count() == 0) seed()
    }

    private suspend fun seed() {
        val day = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        SeedData.entries.forEachIndexed { i, e ->
            dao.upsert(
                EntryRow(
                    id = e.id,
                    title = e.title,
                    content = e.content,
                    mood = e.mood.name,
                    createdAt = now - i * day / 2,
                    isFavorite = e.isFavorite,
                    location = e.location,
                    words = e.content.split(" ").size,
                )
            )
        }
    }
}

private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
private val dateFmt = SimpleDateFormat("d MMM", Locale.getDefault())

private fun EntryRow.toDomain(): JournalEntry {
    val cal = Calendar.getInstance()
    val today = cal.get(Calendar.DAY_OF_YEAR)
    cal.timeInMillis = createdAt
    val label = when (today - cal.get(Calendar.DAY_OF_YEAR)) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> dateFmt.format(Date(createdAt))
    }
    return JournalEntry(
        id = id,
        title = title,
        content = content,
        date = label,
        time = timeFmt.format(Date(createdAt)),
        timestamp = createdAt,
        mood = runCatching { Mood.valueOf(mood) }.getOrDefault(Mood.Calm),
        isFavorite = isFavorite,
        location = location,
    )
}

private val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

private fun dayKey(millis: Long): String = keyFmt.format(Date(millis))

/**
 * Five Monday-aligned weeks ending with the week containing today, so today
 * lands on its own weekday in the last row and the rest of that week reads as
 * future — which is what the frames show.
 */
private fun windowOf35(): List<String> {
    val cal = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        // Back to this week's Monday, then back four more weeks.
        val delta = (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
        add(Calendar.DAY_OF_YEAR, -delta - 28)
    }
    return (0 until 35).map {
        dayKey(cal.timeInMillis).also { _ -> cal.add(Calendar.DAY_OF_YEAR, 1) }
    }
}

/**
 * Consecutive days with at least one check-in, counting back from today.
 * Missing today does not break the streak until the day is actually over —
 * otherwise every streak would read zero each morning until you tapped.
 */
private fun streakLength(activeDays: Set<String>): Int {
    val cal = Calendar.getInstance()
    if (!activeDays.contains(dayKey(cal.timeInMillis))) {
        cal.add(Calendar.DAY_OF_YEAR, -1)
        if (!activeDays.contains(dayKey(cal.timeInMillis))) return 0
    }
    var days = 0
    while (activeDays.contains(dayKey(cal.timeInMillis))) {
        days++
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return days
}

private val dowFmt = SimpleDateFormat("EEE", Locale.getDefault())
private val fullFmt = SimpleDateFormat("EEE d MMM", Locale.getDefault())

/** The seven days ending today. */
private fun lastSevenDays(): List<Pair<String, Long>> {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }
    return (0 until 7).map {
        val pair = dayKey(cal.timeInMillis) to cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        pair
    }
}
