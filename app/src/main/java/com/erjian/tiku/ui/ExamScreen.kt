package com.erjian.tiku.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import com.erjian.tiku.data.Question
import com.erjian.tiku.ui.components.OptionRow
import com.erjian.tiku.ui.components.OptionState
import com.erjian.tiku.ui.components.QuestionMetaRow
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    examVm: ExamViewModel,
    onFinished: () -> Unit,
    onExit: () -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    var showSubmit by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }

    LaunchedEffect(examVm.started) {
        while (!examVm.submitted && examVm.remainingSec > 0) {
            delay(1000)
            examVm.tick()
        }
        if (!examVm.submitted && examVm.remainingSec <= 0) {
            examVm.submit()
        }
    }

    LaunchedEffect(examVm.submitted) {
        if (examVm.submitted) {
            onFinished()
        }
    }

    BackHandler(enabled = !examVm.submitted) {
        showExit = true
    }

    val q: Question? = examVm.current()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = formatTime(examVm.remainingSec),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExit = true }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "退出考试",
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showSubmit = true }) {
                        Text("交卷", color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
    ) { padding ->
        if (q == null) {
            Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("考试加载中…")
            }
            return@Scaffold
        }

        val selected = examVm.selectedKeys(q)
        val total = examVm.questions.size
        val answered = examVm.answeredCount()

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "第 ${examVm.currentIndex + 1}/$total 题",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "已答 $answered · 未答 ${total - answered}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            LinearProgressIndicator(
                progress = answered.toFloat() / total.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 16.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                QuestionMetaRow(question = q, indexText = "剩余 ${formatTime(examVm.remainingSec)}")
                Spacer(Modifier.height(12.dp))
                Text(
                    text = q.question,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(16.dp))
                q.options.forEach { opt ->
                    val isSelected = opt.key in selected
                    OptionRow(
                        keyLabel = opt.key,
                        text = opt.text,
                        selected = isSelected,
                        state = if (isSelected) OptionState.Selected else OptionState.Neutral,
                        onClick = { examVm.toggle(q, opt.key) },
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(24.dp))
            }
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { showSheet = true }) { Text("答题卡") }
                    Spacer(Modifier.weight(1f))
                    if (examVm.currentIndex > 0) {
                        OutlinedButton(onClick = examVm::prev) { Text("上一题") }
                    }
                    Button(
                        onClick = {
                            if (examVm.isLast) showSubmit = true else examVm.next()
                        },
                    ) {
                        Text(if (examVm.isLast) "交卷" else "下一题")
                    }
                }
            }
        }
    }

    if (showSheet) {
        AnswerSheetDialog(
            examVm = examVm,
            onDismiss = { showSheet = false },
            onJump = { index ->
                examVm.goTo(index)
                showSheet = false
            },
        )
    }

    if (showSubmit) {
        val unanswered = examVm.questions.size - examVm.answeredCount()
        AlertDialog(
            onDismissRequest = { showSubmit = false },
            title = { Text("确认交卷") },
            text = {
                Text(
                    if (unanswered > 0) "还有 $unanswered 题未作答，未作答不计分，确定交卷吗？"
                    else "全部题目均已作答，确定交卷吗？"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSubmit = false
                        examVm.submit()
                    },
                ) { Text("确认交卷") }
            },
            dismissButton = {
                TextButton(onClick = { showSubmit = false }) { Text("继续答题") }
            },
        )
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("退出考试") },
            text = { Text("退出后本次作答将不会被保存，确定退出吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExit = false
                        onExit()
                    },
                ) { Text("退出") }
            },
            dismissButton = {
                TextButton(onClick = { showExit = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun AnswerSheetDialog(
    examVm: ExamViewModel,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit,
) {
    val questions = examVm.questions
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("答题卡") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 360.dp),
            ) {
                items(count = questions.size) { index ->
                    val question = questions[index]
                    val isAnswered = (examVm.selections[question.id]?.size ?: 0) > 0
                    val isCurrent = index == examVm.currentIndex
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isAnswered -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .clickable { onJump(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        },
    )
}
