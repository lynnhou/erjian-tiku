package com.erjian.tiku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erjian.tiku.data.QuestionRepository
import com.erjian.tiku.data.UserData
import com.erjian.tiku.ui.theme.WrongRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userData: UserData,
    onNavigate: (String) -> Unit,
) {
    val context = LocalContext.current
    var total by remember { mutableStateOf(0) }
    var cSingle by remember { mutableStateOf(0) }
    var cMulti by remember { mutableStateOf(0) }
    var cJudge by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        val qs = QuestionRepository.questions(context)
        total = qs.size
        cSingle = qs.count { it.type == "single" }
        cMulti = qs.count { it.type == "multi" }
        cJudge = qs.count { it.type == "judge" }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("二建建筑题库") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "二级建造师 · 建筑工程实务",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "题库共 $total 题 · 已练习 ${userData.answeredCount} 题",
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HeaderChip("单选 $cSingle")
                        HeaderChip("多选 $cMulti")
                        HeaderChip("判断 $cJudge")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EntryCard(
                    title = "顺序练习",
                    subtitle = "逐题练习 · 即时判分",
                    icon = Icons.Filled.FormatListBulleted,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(Routes.practice("sequential")) },
                    modifier = Modifier.weight(1f),
                )
                EntryCard(
                    title = "章节练习",
                    subtitle = "按章节各个击破",
                    icon = Icons.Filled.Category,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavigate(Routes.CHAPTERS) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EntryCard(
                    title = "模拟考试",
                    subtitle = "随机组卷 · 计时交卷",
                    icon = Icons.Filled.Assignment,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = { onNavigate(Routes.EXAM) },
                    modifier = Modifier.weight(1f),
                )
                EntryCard(
                    title = "错题本",
                    subtitle = "攻克薄弱知识点",
                    icon = Icons.Filled.ErrorOutline,
                    color = WrongRed,
                    badge = userData.wrongIds.size,
                    onClick = { onNavigate(Routes.WRONG) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EntryCard(
                    title = "我的收藏",
                    subtitle = "收藏的重点题",
                    icon = Icons.Filled.Star,
                    color = Color(0xFFFFB300),
                    badge = userData.favoriteIds.size,
                    onClick = { onNavigate(Routes.FAVORITE) },
                    modifier = Modifier.weight(1f),
                )
                EntryCard(
                    title = "练习统计",
                    subtitle = "正确率与模考记录",
                    icon = Icons.Filled.BarChart,
                    color = MaterialTheme.colorScheme.secondary,
                    onClick = { onNavigate(Routes.STATS) },
                    modifier = Modifier.weight(1f),
                )
            }

            val lastExam = userData.examRecords.lastOrNull()
            if (lastExam != null) {
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "最近模考：${lastExam.correct}/${lastExam.total} 题 · ${lastExam.score} 分",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = if (lastExam.passed) "合格" else "不合格",
                            color = if (lastExam.passed) com.erjian.tiku.ui.theme.CorrectGreen else WrongRed,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.18f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EntryCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Int? = null,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp),
                )
                if (badge != null && badge > 0) {
                    Spacer(Modifier.width(4.dp))
                    Surface(color = WrongRed, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "$badge",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}
