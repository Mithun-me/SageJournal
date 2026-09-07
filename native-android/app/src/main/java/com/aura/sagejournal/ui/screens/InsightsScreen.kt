package com.aura.sagejournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.sagejournal.domain.Milestone
import com.aura.sagejournal.domain.TrendPoint
import com.aura.sagejournal.ui.components.GlassSurface
import com.aura.sagejournal.ui.components.TrendChart
import com.aura.sagejournal.ui.theme.AuraColors
import com.aura.sagejournal.ui.theme.AuraShapes
import com.aura.sagejournal.ui.theme.AuraType

@Composable
fun InsightsScreen(
    totalPoints: Int,
    streak: Int,
    week: List<TrendPoint>,
    milestones: List<Milestone>,
    aiInsight: String,
) {
    var selected by remember { mutableIntStateOf(4) }  // Friday, as in the web build
    var range by remember { mutableStateOf("7d") }

    val avgClarity = if (week.isEmpty()) 0 else week.sumOf { it.clarityScore } / week.size
    val periodPoints = week.sumOf { it.pointsEarned }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Stats grid — 2x2 here rather than the web's 4-across, which would
        // give ~90dp columns on a phone.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Modifier.weight(1f), Icons.Filled.Star, AuraColors.Primary,
                "TOTAL POINTS", "%,d".format(totalPoints), Color.White)
            StatCard(Modifier.weight(1f), Icons.Filled.LocalFireDepartment, AuraColors.Warm,
                "STREAK", "$streak", Color.White, suffix = "days")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(Modifier.weight(1f), Icons.Filled.MonitorHeart, AuraColors.Primary,
                "AVG CLARITY", "$avgClarity%", AuraColors.Primary)
            StatCard(Modifier.weight(1f), Icons.Filled.Bolt, AuraColors.Warm,
                "PERIOD GAINS", "+$periodPoints", AuraColors.Warm)
        }

        GlassSurface(Modifier.fillMaxWidth(), AuraShapes.CardRaised) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Mood & Points",
                            color = AuraColors.TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "Clarity against points earned",
                            color = AuraColors.TextTertiary,
                            fontSize = AuraType.overline,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("7d", "14d").forEach { r ->
                            val on = r == range
                            Text(
                                r,
                                color = if (on) AuraColors.OnPrimary else AuraColors.TextTertiary,
                                fontSize = AuraType.overline,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(AuraShapes.Pill)
                                    .background(
                                        if (on) AuraColors.Primary else Color.White.copy(alpha = 0.06f)
                                    )
                                    .clickable { range = r }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot(AuraColors.Primary, "Clarity")
                    LegendDot(AuraColors.Warm, "Points")
                    LegendDot(AuraColors.Indigo, "Trend")
                }

                TrendChart(
                    points = week,
                    selectedIndex = selected,
                    onSelect = { selected = it },
                )

                week.getOrNull(selected)?.let { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(AuraShapes.Card)
                            .background(Color(0xE60B0E20))
                            .border(1.dp, AuraColors.Hairline, AuraShapes.Card)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, AuraColors.Hairline, RoundedCornerShape(11.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text(p.mood.emoji, fontSize = 18.sp) }

                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    p.fullDate,
                                    color = Color.White,
                                    fontSize = AuraType.meta,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "${p.clarityScore}%",
                                    color = AuraColors.PrimaryBright,
                                    fontSize = AuraType.label,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "+${p.pointsEarned} pts",
                                    color = AuraColors.Warm,
                                    fontSize = AuraType.label,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            Text(
                                p.notes,
                                color = AuraColors.TextSecondary,
                                fontSize = AuraType.label,
                            )
                        }
                    }
                }
            }
        }

        GlassSurface(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Filled.AutoAwesome, null, Modifier.size(18.dp), AuraColors.Primary)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Pattern noticed",
                        color = AuraColors.TextPrimary,
                        fontSize = AuraType.cardTitle,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        aiInsight,
                        color = AuraColors.TextSecondary,
                        fontSize = AuraType.meta,
                        lineHeight = 19.sp,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Milestones",
                color = AuraColors.TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            milestones.forEach { MilestoneRow(it) }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    valueColor: Color,
    suffix: String? = null,
) {
    GlassSurface(modifier) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(icon, null, Modifier.size(14.dp), iconTint)
                Text(
                    label,
                    color = AuraColors.TextTertiary,
                    fontSize = AuraType.overline,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value, color = valueColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                suffix?.let {
                    Text(it, color = AuraColors.TextTertiary, fontSize = AuraType.label)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(Modifier.size(8.dp).clip(AuraShapes.Pill).background(color))
        Text(label, color = AuraColors.TextTertiary, fontSize = AuraType.overline)
    }
}

@Composable
private fun MilestoneRow(m: Milestone) {
    GlassSurface(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (m.achieved) AuraColors.Primary.copy(alpha = 0.16f)
                        else Color.White.copy(alpha = 0.04f)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (m.achieved) Text(m.emoji, fontSize = 19.sp)
                else Icon(Icons.Filled.Lock, null, Modifier.size(16.dp), AuraColors.TextTertiary)
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    m.title,
                    color = if (m.achieved) AuraColors.TextPrimary else AuraColors.TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(m.subtitle, color = AuraColors.TextTertiary, fontSize = AuraType.label)

                if (!m.achieved && m.target > 0) {
                    val frac = m.progress.toFloat() / m.target
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(AuraShapes.Pill)
                            .background(Color.White.copy(alpha = 0.07f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(frac)
                                .height(5.dp)
                                .clip(AuraShapes.Pill)
                                .background(AuraColors.Primary)
                        )
                    }
                    Text(
                        "${m.progress} of ${m.target}",
                        color = AuraColors.TextTertiary,
                        fontSize = AuraType.overline,
                    )
                }
            }

            if (m.achieved) {
                Text(
                    "Earned",
                    color = AuraColors.PrimaryBright,
                    fontSize = AuraType.overline,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(AuraShapes.Pill)
                        .background(AuraColors.Primary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}
