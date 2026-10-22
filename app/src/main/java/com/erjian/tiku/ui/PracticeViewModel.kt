package com.erjian.tiku.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.erjian.tiku.data.Question
import com.erjian.tiku.data.UserStore
import kotlinx.coroutines.launch

class PracticeViewModel(
    private val questions: List<Question>,
    private val userStore: UserStore,
    val mode: String,
) : ViewModel() {

    var index by mutableStateOf(0)
        private set

    val selections = mutableStateMapOf<Int, Set<String>>()
    val graded = mutableStateMapOf<Int, Boolean>()
    val gradedCorrect = mutableStateMapOf<Int, Boolean>()

    val current: Question? get() = questions.getOrNull(index)
    val total: Int get() = questions.size
    val isLast: Boolean get() = index >= total - 1
    val isFirst: Boolean get() = index <= 0

    fun selectedKeys(q: Question): Set<String> = selections[q.id] ?: emptySet()
    fun isGraded(q: Question): Boolean = graded[q.id] == true
    fun wasCorrect(q: Question): Boolean = gradedCorrect[q.id] == true

    fun select(q: Question, key: String) {
        if (graded[q.id] == true) return
        val cur = selections[q.id] ?: emptySet()
        selections[q.id] = if (q.isMulti) {
            if (key in cur) cur - key else cur + key
        } else {
            setOf(key)
        }
        if (!q.isMulti) grade(q)
    }

    fun submitMulti(q: Question) {
        if ((selections[q.id]?.size ?: 0) > 0) grade(q)
    }

    private fun grade(q: Question) {
        val sel = selections[q.id] ?: emptySet()
        if (sel.isEmpty()) return
        val correct = sel.toSet() == q.answer.toSet()
        graded[q.id] = true
        gradedCorrect[q.id] = correct
        viewModelScope.launch { userStore.recordAnswer(q, correct) }
    }

    fun next() {
        if (index < total - 1) index++
    }

    fun prev() {
        if (index > 0) index--
    }

    class Factory(
        private val questions: List<Question>,
        private val userStore: UserStore,
        private val mode: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PracticeViewModel(questions, userStore, mode) as T
    }
}
