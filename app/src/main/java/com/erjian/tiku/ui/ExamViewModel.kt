package com.erjian.tiku.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.erjian.tiku.data.ExamRecord
import com.erjian.tiku.data.Question
import com.erjian.tiku.data.UserStore
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class ExamOutcome(
    val total: Int,
    val correct: Int,
    val wrong: Int,
    val unanswered: Int,
    val score: Int,
    val passed: Boolean,
    val durationSec: Int,
    val wrongQuestionIds: List<Int>,
)

class ExamViewModel(
    application: Application,
    private val userStore: UserStore,
) : AndroidViewModel(application) {

    var started by mutableStateOf(false)
        private set
    var questions by mutableStateOf<List<Question>>(emptyList())
        private set
    var currentIndex by mutableStateOf(0)
        private set
    var submitted by mutableStateOf(false)
        private set
    var remainingSec by mutableStateOf(0)
        private set
    var elapsedSec by mutableStateOf(0)
        private set
    var totalMinutes by mutableStateOf(0)
        private set

    val selections = mutableStateMapOf<Int, Set<String>>()

    var result: ExamOutcome? by mutableStateOf(null)
        private set

    fun start(questions: List<Question>, minutes: Int) {
        this.questions = questions
        this.totalMinutes = minutes
        remainingSec = minutes * 60
        elapsedSec = 0
        currentIndex = 0
        submitted = false
        started = true
        selections.clear()
        result = null
    }

    fun current(): Question? = questions.getOrNull(currentIndex)

    val isLast: Boolean get() = currentIndex >= questions.size - 1

    fun goTo(index: Int) {
        if (index in questions.indices) currentIndex = index
    }

    fun next() {
        if (currentIndex < questions.size - 1) currentIndex++
    }

    fun prev() {
        if (currentIndex > 0) currentIndex--
    }

    fun selectedKeys(q: Question): Set<String> = selections[q.id] ?: emptySet()

    fun toggle(q: Question, key: String) {
        if (submitted) return
        val cur = selections[q.id] ?: emptySet()
        selections[q.id] = if (q.isMulti) {
            if (key in cur) cur - key else cur + key
        } else {
            setOf(key)
        }
    }

    fun answeredCount(): Int = selections.values.count { it.isNotEmpty() }

    fun tick() {
        if (!submitted && remainingSec > 0) {
            remainingSec--
            elapsedSec++
        }
    }

    fun submit() {
        if (submitted) return
        var correct = 0
        val wrongIds = mutableListOf<Int>()
        questions.forEach { q ->
            val sel = selections[q.id] ?: emptySet()
            if (sel.isEmpty()) return@forEach
            if (sel.toSet() == q.answer.toSet()) correct++ else wrongIds.add(q.id)
        }
        val total = questions.size
        val answered = total - questions.count { (selections[it.id]?.size ?: 0) == 0 }
        val wrong = answered - correct
        val score = if (total > 0) correct * 100 / total else 0
        val outcome = ExamOutcome(
            total = total,
            correct = correct,
            wrong = wrong,
            unanswered = total - answered,
            score = score,
            passed = score >= 60,
            durationSec = elapsedSec,
            wrongQuestionIds = wrongIds,
        )
        result = outcome
        submitted = true
        viewModelScope.launch {
            userStore.addExamRecord(
                ExamRecord(
                    timestamp = System.currentTimeMillis(),
                    total = total,
                    correct = correct,
                    score = score,
                    durationSec = elapsedSec,
                    passed = score >= 60,
                )
            )
            if (wrongIds.isNotEmpty()) userStore.addWrongIds(wrongIds)
        }
    }

    class Factory(
        private val application: Application,
        private val userStore: UserStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExamViewModel(application, userStore) as T
    }
}

fun pickExamQuestions(all: List<Question>, total: Int): List<Question> {
    val pool = all.shuffled()
    val byType = listOf("single", "multi", "judge").associateWith { t ->
        pool.filter { it.type == t }.toMutableList()
    }
    val nSingle = (total * 0.4).roundToInt()
    val nMulti = (total * 0.2).roundToInt()
    val nJudge = total - nSingle - nMulti
    val result = ArrayList<Question>(total)
    byType["single"]?.let { list ->
        repeat(minOf(nSingle, list.size)) { if (list.isNotEmpty()) result.add(list.removeAt(0)) }
    }
    byType["multi"]?.let { list ->
        repeat(minOf(nMulti, list.size)) { if (list.isNotEmpty()) result.add(list.removeAt(0)) }
    }
    byType["judge"]?.let { list ->
        repeat(minOf(nJudge, list.size)) { if (list.isNotEmpty()) result.add(list.removeAt(0)) }
    }
    if (result.size < total) {
        val used = result.toHashSet()
        pool.filter { it !in used }.take(total - result.size).forEach { result.add(it) }
    }
    return result.shuffled()
}
