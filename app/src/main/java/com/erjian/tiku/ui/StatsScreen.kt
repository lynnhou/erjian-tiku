package com.erjian.tiku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erjian.tiku.data.ChapterInfo
import com.erjian.tiku.data.QuestionRepository
import com.erjian.tiku.data.UserStore
import com.erjian.tiku.ui.theme.CorrectGreen
import com.erjian.tiku.ui.theme.WrongRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    userStore: UserStore,
    onBack: () -> Unit,
) {
    val userData by userStore.data.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var chapters by remember { mutableStateOf<List<ChapterInfo>>(emptyList()) }
    var showReset by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        chapters = QuestionRepository.chapters(context)
    }

    val accuracy = if (userData.answeredCount > 0) {
        userData.correctCount * 100 / userData.answeredCount
    } else {
        0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("练习统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Card {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem("累计答题", "${userData.answeredCount}")
                    StatItem("答对", "${userData.correctCount}")
                    StatItem("正确率", "$accuracy%")
                    StatItem("错题", "${userData.wrongIds.size}")
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "章节正确率",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            chapters.forEach { chapter ->
                val stat = userData.chapterStats[chapter.name]
                val acc = if (stat != null && stat.answered > 0) {
                    stat.correct * 100 / stat.answered
                } else {
                    null
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = chapter.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.padding(8.dp))
                    if (acc == null) {
                        Text(
                            text = "未练习",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(acc / 100f)
                                    .fillMaxHeight()
                                    .background(if (acc >= 60) CorrectGreen else WrongRed),
                            )
                        }
                        Spacer(Modifier.padding(8.dp))
                        Text(
                            text = "$acc%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "模考记录",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            if (userData.examRecords.isEmpty()) {
                Text(
                    text = "暂无模考记录",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                userData.examRecords.reversed().forEach { record ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatDateTime(record.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = "${record.correct}/${record.total}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.padding(8.dp))
                            Text(
                                text = "${record.score} 分",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (record.passed) CorrectGreen else WrongRed,
                            )
                            Spacer(Modifier.padding(8.dp))
                            Text(
                                text = if (record.passed) "合格" else "不合格",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (record.passed) CorrectGreen else WrongRed,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = { showReset = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("清空所有学习数据")
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("清空学习数据") },
            text = { Text("将清空错题本、收藏、练习统计和模考记录，且不可恢复，确定吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReset = false
                        scope.launch { userStore.resetAll() }
                    },
                ) { Text("确定清空") }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
