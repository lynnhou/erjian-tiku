package com.erjian.tiku.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class ChapterInfo(val name: String, val count: Int)

object QuestionRepository {
    private val mutex = Mutex()
    private var cached: List<Question>? = null

    suspend fun questions(context: Context): List<Question> = mutex.withLock {
        cached ?: withContext(Dispatchers.IO) {
            val text = context.assets.open("questions.json").use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
            QuestionBank.fromJson(text)
        }.also { cached = it }
    }

    suspend fun chapters(context: Context): List<ChapterInfo> {
        val order = LinkedHashMap<String, Int>()
        questions(context).forEach { q ->
            order[q.chapter] = (order[q.chapter] ?: 0) + 1
        }
        return order.map { (name, count) -> ChapterInfo(name, count) }
    }
}
