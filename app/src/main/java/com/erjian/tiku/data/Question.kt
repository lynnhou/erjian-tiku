package com.erjian.tiku.data

import org.json.JSONArray
import org.json.JSONObject

data class Option(val key: String, val text: String)

data class Question(
    val id: Int,
    val type: String,
    val question: String,
    val options: List<Option>,
    val answer: List<String>,
    val analysis: String,
    val topic: String,
    val chapter: String,
) {
    val isMulti: Boolean get() = type == "multi"
    val isJudge: Boolean get() = type == "judge"
    val typeLabel: String
        get() = when (type) {
            "single" -> "单选题"
            "multi" -> "多选题"
            "judge" -> "判断题"
            else -> type
        }
}

object QuestionBank {

    fun fromJson(json: String): List<Question> {
        val root = JSONObject(json)
        val arr = root.getJSONArray("questions")
        val list = ArrayList<Question>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val opts = o.getJSONArray("options")
            val options = ArrayList<Option>(opts.length())
            for (j in 0 until opts.length()) {
                val op = opts.getJSONObject(j)
                options.add(Option(op.getString("key"), op.getString("text")))
            }
            val ansArr = o.getJSONArray("answer")
            val answer = ArrayList<String>(ansArr.length())
            for (j in 0 until ansArr.length()) {
                answer.add(ansArr.getString(j))
            }
            list.add(
                Question(
                    id = o.getInt("id"),
                    type = o.getString("type"),
                    question = o.getString("question"),
                    options = options,
                    answer = answer,
                    analysis = o.optString("analysis", ""),
                    topic = o.optString("topic", ""),
                    chapter = o.optString("chapter", ""),
                )
            )
        }
        return list
    }
}
