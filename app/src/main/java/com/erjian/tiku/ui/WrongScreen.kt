package com.erjian.tiku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.erjian.tiku.data.Question
import com.erjian.tiku.data.QuestionRepository
import com.erjian.tiku.data.UserStore
import com.erjian.tiku.ui.components.TypeBadge
import com.erjian.tiku.ui.theme.CorrectGreen
import com.erjian.tiku.ui.theme.WrongRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongScreen(
    userStore: UserStore,
    onBack: () -> Unit,
    onPractice: () -> Unit,
) {
    val userData by userStore.data.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var expandedId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(userData.wrongIds) {
        val qs = QuestionRepository.questions(context)
        questions = userData.wrongIds.mapNotNull { id -> qs.firstOrNull { it.id == id } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("错题本") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (questions.isNotEmpty()) {
                        TextButton(onClick = { scope.launch { userStore.clearWrong() } }) {
                            Text("清空")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (questions.isNotEmpty()) {
                Surface(tonalElevation = 3.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = onPractice,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("练习全部错题（${questions.size}）")
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            questions.isEmpty() -> EmptyBox(
                message = "错题本是空的\n练习中答错的题会自动出现在这里",
                modifier = Modifier.padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(questions, key = { it.id }) { q ->
                    val expanded = expandedId == q.id
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row {
                                TypeBadge(question = q)
                                Spacer(Modifier.padding(6.dp))
                                Text(
                                    text = q.question,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (expanded) {
                                Spacer(Modifier.height(8.dp))
                                q.options.forEach { opt ->
                                    val isAnswer = opt.key in q.answer
                                    Text(
                                        text = "${opt.key}. ${opt.text}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isAnswer) CorrectGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isAnswer) FontWeight.Medium else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 2.dp),
                                    )
                                }
                                if (q.analysis.isNotBlank()) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "解析：${q.analysis}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                TextButton(onClick = {
                                    expandedId = if (expanded) null else q.id
                                }) {
                                    Text(if (expanded) "收起" else "查看答案")
                                }
                                Spacer(Modifier.weight(1f))
                                TextButton(
                                    onClick = { scope.launch { userStore.removeWrong(q.id) } },
                                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                        contentColor = WrongRed,
                                    ),
                                ) {
                                    Text("移除")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
