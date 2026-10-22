package com.erjian.tiku.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erjian.tiku.data.Question
import com.erjian.tiku.data.QuestionRepository
import com.erjian.tiku.data.UserData
import com.erjian.tiku.data.UserStore
import com.erjian.tiku.ui.components.AnalysisCard
import com.erjian.tiku.ui.components.OptionRow
import com.erjian.tiku.ui.components.OptionState
import com.erjian.tiku.ui.components.QuestionMetaRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    mode: String,
    chapter: String?,
    userStore: UserStore,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val userData by userStore.data.collectAsStateWithLifecycle()
    var questions by remember { mutableStateOf<List<Question>?>(null) }

    LaunchedEffect(Unit) {
        userStore.load()
        val qs = QuestionRepository.questions(context)
        val d = userStore.data.value
        questions = when (mode) {
            "chapter" -> qs.filter { it.chapter == chapter }
            "wrong" -> d.wrongIds.mapNotNull { id -> qs.firstOrNull { it.id == id } }
            "favorite" -> d.favoriteIds.mapNotNull { id -> qs.firstOrNull { it.id == id } }
            else -> qs
        }
    }

    val title = when (mode) {
        "chapter" -> chapter ?: "章节练习"
        "wrong" -> "错题练习"
        "favorite" -> "收藏练习"
        else -> "顺序练习"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { padding ->
        val qs = questions
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                qs == null -> LoadingBox()
                qs.isEmpty() -> EmptyBox(
                    when (mode) {
                        "wrong" -> "错题本是空的\n答错的题会自动出现在这里"
                        "favorite" -> "还没有收藏题目\n练习时点击星标即可收藏"
                        else -> "暂无题目"
                    }
                )
                else -> {
                    val vm: PracticeViewModel = viewModel(
                        factory = PracticeViewModel.Factory(qs, userStore, mode),
                        key = mode + (chapter ?: ""),
                    )
                    PracticeBody(vm, userStore, userData, onBack)
                }
            }
        }
    }
}

@Composable
private fun PracticeBody(
    vm: PracticeViewModel,
    userStore: UserStore,
    userData: UserData,
    onBack: () -> Unit,
) {
    val q = vm.current ?: return
    val selected = vm.selectedKeys(q)
    val isGraded = vm.isGraded(q)
    val scope = rememberCoroutineScope()
    val isFav = userData.favoriteIds.contains(q.id)

    Column(modifier = Modifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = (vm.index + 1).toFloat() / vm.total.toFloat(),
            modifier = Modifier.fillMaxWidth().height(4.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            QuestionMetaRow(question = q, indexText = "第 ${vm.index + 1}/${vm.total} 题")
            Spacer(Modifier.height(12.dp))
            Text(
                text = q.question,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(16.dp))
            q.options.forEach { opt ->
                val state = when {
                    !isGraded ->
                        if (opt.key in selected) OptionState.Selected else OptionState.Neutral
                    opt.key in q.answer -> OptionState.Correct
                    opt.key in selected -> OptionState.Wrong
                    else -> OptionState.Neutral
                }
                OptionRow(
                    keyLabel = opt.key,
                    text = opt.text,
                    selected = opt.key in selected,
                    state = state,
                    onClick = { vm.select(q, opt.key) },
                )
                Spacer(Modifier.height(10.dp))
            }
            if (isGraded) {
                Spacer(Modifier.height(6.dp))
                AnalysisCard(
                    correct = vm.wasCorrect(q),
                    answerText = q.answer.sorted().joinToString(""),
                    analysis = q.analysis,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
        Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { scope.launch { userStore.toggleFavorite(q.id) } },
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "收藏",
                        tint = if (isFav) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!vm.isFirst) {
                    OutlinedButton(onClick = vm::prev) { Text("上一题") }
                    Spacer(Modifier.width(6.dp))
                }
                Spacer(Modifier.weight(1f))
                when {
                    !isGraded && q.isMulti -> Button(
                        onClick = { vm.submitMulti(q) },
                        enabled = selected.isNotEmpty(),
                    ) { Text("提交答案") }
                    isGraded && !vm.isLast -> Button(onClick = vm::next) { Text("下一题") }
                    isGraded && vm.isLast -> Button(onClick = onBack) { Text("完成") }
                    else -> Button(onClick = {}, enabled = false) { Text("提交答案") }
                }
            }
        }
    }
}
