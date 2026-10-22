package com.erjian.tiku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.erjian.tiku.data.Question
import com.erjian.tiku.data.QuestionRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamSetupScreen(
    examVm: ExamViewModel,
    onBack: () -> Unit,
    onStart: (Int, Int) -> Unit,
) {
    val context = LocalContext.current
    var all by remember { mutableStateOf<List<Question>>(emptyList()) }

    LaunchedEffect(Unit) {
        all = QuestionRepository.questions(context)
    }

    fun start(count: Int, minutes: Int) {
        if (all.isEmpty()) return
        examVm.start(pickExamQuestions(all, count), minutes)
        onStart(count, minutes)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模拟考试") },
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
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "考试规则",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "从题库按比例随机抽取单选、多选、判断题组卷，满分 100 分，" +
                            "60 分及以上合格。考试过程中可随时修改答案，交卷或倒计时结束后自动批改，" +
                            "错题将自动收入错题本。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            ExamModeCard(
                title = "快速检测",
                desc = "20 题 · 20 分钟",
                tip = "碎片时间快速自测",
                enabled = all.isNotEmpty(),
                onClick = { start(20, 20) },
            )
            Spacer(Modifier.height(12.dp))
            ExamModeCard(
                title = "标准模拟",
                desc = "100 题 · 120 分钟",
                tip = "贴近真实考试的题量与节奏",
                enabled = all.isNotEmpty(),
                onClick = { start(100, 120) },
            )
            Spacer(Modifier.height(12.dp))
            ExamModeCard(
                title = "全真演练",
                desc = "${all.size} 题 · 180 分钟",
                tip = "完整题库随机组卷，考前冲刺",
                enabled = all.isNotEmpty(),
                onClick = { start(all.size, 180) },
            )
        }
    }
}

@Composable
private fun ExamModeCard(
    title: String,
    desc: String,
    tip: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(32.dp),
            )
            Spacer(Modifier.padding(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
