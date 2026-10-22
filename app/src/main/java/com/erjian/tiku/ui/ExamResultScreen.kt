package com.erjian.tiku.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erjian.tiku.data.UserStore
import com.erjian.tiku.ui.theme.CorrectGreen
import com.erjian.tiku.ui.theme.CorrectGreenBg
import com.erjian.tiku.ui.theme.WrongRed
import com.erjian.tiku.ui.theme.WrongRedBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamResultScreen(
    examVm: ExamViewModel,
    userStore: UserStore,
    onBack: () -> Unit,
    onPracticeWrong: () -> Unit,
) {
    val result = examVm.result
    if (result == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val userData by userStore.data.collectAsStateWithLifecycle()
    val passColor = if (result.passed) CorrectGreen else WrongRed
    val passBg = if (result.passed) CorrectGreenBg else WrongRedBg

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("考试结果") },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(passBg),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${result.score}",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = passColor,
                    )
                    Text(
                        text = "分",
                        style = MaterialTheme.typography.titleMedium,
                        color = passColor,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (result.passed) "恭喜，考试合格！" else "未达到合格线（60分），继续加油",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = passColor,
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ResultStat("答对", "${result.correct}", Modifier.weight(1f))
                ResultStat("答错", "${result.wrong}", Modifier.weight(1f))
                ResultStat("未答", "${result.unanswered}", Modifier.weight(1f))
                ResultStat("用时", formatTime(result.durationSec), Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "合格线 60 分 · 总分 ${result.total} 分",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            if (userData.wrongIds.isNotEmpty()) {
                Button(
                    onClick = onPracticeWrong,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("练习错题本（${userData.wrongIds.size} 题）")
                }
                Spacer(Modifier.height(10.dp))
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("返回首页")
            }
        }
    }
}

@Composable
private fun ResultStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
