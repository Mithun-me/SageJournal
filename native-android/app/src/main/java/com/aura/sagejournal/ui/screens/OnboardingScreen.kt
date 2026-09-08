package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType
import kotlinx.coroutines.launch

private data class Page(
    val emoji: String,
    val headline: String,
    val body: String,
    val cta: String,
)

/**
 * The redesign supplied only step 2 of 3 ("One tap is a whole entry"), so the
 * first and last pages are written here. They stay close to what the app
 * actually does: the mood check-in is the core loop, and You already tells the
 * user their journal is "On this device only", so the closing page says that
 * rather than promising a cloud that is not wired.
 */
private val pages = listOf(
    Page(
        emoji = "🌿",
        headline = "A quiet place\nfor your days.",
        body = "No feed, no streak shaming, nobody else reading. Just somewhere " +
            "to notice how things are landing.",
        cta = "Continue",
    ),
    Page(
        emoji = "😌",
        headline = "One tap is\na whole entry.",
        body = "Choose how you feel and Aura remembers the day. Write only when " +
            "you have something to say.",
        cta = "Continue",
    ),
    Page(
        emoji = "🔒",
        headline = "It stays on\nthis phone.",
        body = "Your journal is stored on this device. Nothing leaves it until " +
            "you choose to sync, and even then only your nickname and entries.",
        cta = "Begin",
    ),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(AuraColors.Background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { index ->
            val page = pages[index]
            Column(
                Modifier.fillMaxSize().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                RingMark(page.emoji)
                Text(
                    page.headline,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 40.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 48.dp),
                )
                Text(
                    page.body,
                    color = AuraColors.TextSecondary,
                    fontSize = AuraType.body,
                    lineHeight = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 18.dp),
                )
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { i ->
                    val active = i == pagerState.currentPage
                    Box(
                        Modifier
                            .height(7.dp)
                            .width(if (active) 24.dp else 7.dp)
                            .clip(AuraShapes.Pill)
                            .background(
                                if (active) AuraColors.Primary
                                else Color.White.copy(alpha = 0.24f)
                            )
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(AuraShapes.Pill)
                    .background(Color.White)
                    .clickable {
                        val next = pagerState.currentPage + 1
                        if (next < pages.size) {
                            scope.launch { pagerState.animateScrollToPage(next) }
                        } else onDone()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    pages[pagerState.currentPage].cta,
                    color = AuraColors.Background,
                    fontSize = AuraType.body,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                "Skip introduction",
                color = AuraColors.TextMuted,
                fontSize = AuraType.bodySmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(AuraShapes.Pill)
                    .clickable(onClick = onDone)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

/** Three concentric rings, 198 / 148 / 94dp, from the frame. */
@Composable
private fun RingMark(emoji: String) {
    Box(Modifier.size(198.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(198.dp)
                .clip(AuraShapes.Pill)
                .border(1.dp, Color.White.copy(alpha = 0.16f), AuraShapes.Pill)
        )
        Box(
            Modifier
                .size(148.dp)
                .clip(AuraShapes.Pill)
                .border(1.dp, AuraColors.Primary.copy(alpha = 0.4f), AuraShapes.Pill)
        )
        Box(
            Modifier
                .size(94.dp)
                .clip(AuraShapes.Pill)
                .background(AuraColors.Primary.copy(alpha = 0.22f))
                .border(1.dp, AuraColors.Primary.copy(alpha = 0.6f), AuraShapes.Pill),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 34.sp)
        }
    }
}
