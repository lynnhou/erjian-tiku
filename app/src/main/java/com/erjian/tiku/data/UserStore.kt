package com.erjian.tiku.data

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class ChapterStat(var answered: Int = 0, var correct: Int = 0)

data class ExamRecord(
    val timestamp: Long = 0L,
    val total: Int = 0,
    val correct: Int = 0,
    val score: Int = 0,
    val durationSec: Int = 0,
    val passed: Boolean = false,
)

data class UserData(
    val wrongIds: List<Int> = emptyList(),
    val favoriteIds: List<Int> = emptyList(),
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
    val chapterStats: Map<String, ChapterStat> = emptyMap(),
    val examRecords: List<ExamRecord> = emptyList(),
)

class UserStore(context: Context) {
    private val file = File(context.filesDir, "user_data.json")
    private val gson = Gson()
    private val mutex = Mutex()
    private val _data = MutableStateFlow(UserData())
    val data: StateFlow<UserData> = _data.asStateFlow()

    suspend fun load() = mutex.withLock {
        if (file.exists()) {
            runCatching {
                gson.fromJson(file.readText(), UserData::class.java)
            }.getOrNull()?.let { _data.value = it }
        }
        Unit
    }

    private suspend fun persist(d: UserData) {
        withContext(Dispatchers.IO) {
            runCatching { file.writeText(gson.toJson(d)) }
        }
        Unit
    }

    private suspend fun update(block: (UserData) -> UserData) = mutex.withLock {
        val next = block(_data.value)
        _data.value = next
        persist(next)
    }

    suspend fun recordAnswer(q: Question, correct: Boolean) = update { d ->
        val wrong = d.wrongIds.toMutableList()
        if (correct) {
            wrong.remove(q.id)
        } else if (!wrong.contains(q.id)) {
            wrong.add(q.id)
        }
        val stats = d.chapterStats.toMutableMap()
        val prev = stats[q.chapter] ?: ChapterStat()
        stats[q.chapter] = ChapterStat(prev.answered + 1, prev.correct + if (correct) 1 else 0)
        d.copy(
            wrongIds = wrong,
            answeredCount = d.answeredCount + 1,
            correctCount = d.correctCount + if (correct) 1 else 0,
            chapterStats = stats,
        )
    }

    suspend fun toggleFavorite(id: Int) = update { d ->
        val favs = d.favoriteIds.toMutableList()
        if (!favs.remove(id)) favs.add(id)
        d.copy(favoriteIds = favs)
    }

    suspend fun addWrongIds(ids: List<Int>) = update { d ->
        val wrong = d.wrongIds.toMutableList()
        ids.forEach { if (!wrong.contains(it)) wrong.add(it) }
        d.copy(wrongIds = wrong)
    }

    suspend fun addExamRecord(record: ExamRecord) = update { d ->
        d.copy(examRecords = (d.examRecords + record).takeLast(50))
    }

    suspend fun removeWrong(id: Int) = update { d ->
        d.copy(wrongIds = d.wrongIds.filterNot { it == id })
    }

    suspend fun clearWrong() = update { it.copy(wrongIds = emptyList()) }

    suspend fun removeFavorite(id: Int) = update { d ->
        d.copy(favoriteIds = d.favoriteIds.filterNot { it == id })
    }

    suspend fun resetAll() = update { UserData() }
}
