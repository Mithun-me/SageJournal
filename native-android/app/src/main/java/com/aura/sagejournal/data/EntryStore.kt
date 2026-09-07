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
import com.aura.sagejournal.domain.JournalEntry
import com.aura.sagejournal.domain.Mood
import com.aura.sagejournal.domain.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

@Database(entities = [EntryRow::class], version = 1, exportSchema = false)
abstract class AuraDatabase : RoomDatabase() {
    abstract fun entries(): EntryDao
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
        .build()

    private val dao = db.entries()

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

    suspend fun toggleFavourite(id: String) {
        val row = dao.byId(id) ?: return
        dao.upsert(row.copy(isFavorite = !row.isFavorite))
    }

    suspend fun reset() = withContext(Dispatchers.IO) {
        dao.clear()
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
