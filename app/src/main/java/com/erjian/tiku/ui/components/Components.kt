package com.erjian.tiku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.erjian.tiku.data.Question
import com.erjian.tiku.ui.theme.CorrectGreen
import com.erjian.tiku.ui.theme.CorrectGreenBg
import com.erjian.tiku.ui.theme.JudgeOrange
import com.erjian.tiku.ui.theme.MultiPurple
import com.erjian.tiku.ui.theme.SingleBlue
import com.erjian.tiku.ui.theme.WrongRed
import com.erjian.tiku.ui.theme.WrongRedBg

enum class OptionState { Neutral, Selected, Correct, Wrong }

@Composable
fun TypeBadge(question: Question, modifier: Modifier = Modifier) {
    val color = when (question.type) {
        "single" -> SingleBlue
        "multi" -> MultiPurple
        "judge" -> JudgeOrange
        else -> SingleBlue
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = question.typeLabel,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun TopicBadge(text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun OptionRow(
    keyLabel: String,
    text: String,
    selected: Boolean,
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = when (state) {
        OptionState.Neutral -> if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        OptionState.Selected -> MaterialTheme.colorScheme.primaryContainer
        OptionState.Correct -> CorrectGreenBg
        OptionState.Wrong -> WrongRedBg
    }
    val borderColor = when (state) {
        OptionState.Correct -> CorrectGreen
        OptionState.Wrong -> WrongRed
        OptionState.Selected -> MaterialTheme.colorScheme.primary
        OptionState.Neutral -> if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = when (state) {
        OptionState.Correct -> CorrectGreen
        OptionState.Wrong -> WrongRed
        else -> MaterialTheme.colorScheme.onSurface
    }
    val badgeBg = when (state) {
        OptionState.Correct -> CorrectGreen
        OptionState.Wrong -> WrongRed
        else -> MaterialTheme.colorScheme.primary
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = keyLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
fun QuestionMetaRow(question: Question, indexText: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TypeBadge(question)
        TopicBadge(question.chapter.ifBlank { question.topic })
        Spacer(Modifier.weight(1f))
        Text(
            text = indexText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun AnalysisCard(
    correct: Boolean,
    answerText: String,
    analysis: String,
    modifier: Modifier = Modifier,
) {
    val color = if (correct) CorrectGreen else WrongRed
    val bg = if (correct) CorrectGreenBg else WrongRedBg
    Surface(
        color = bg,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (correct) "回答正确" else "回答错误",
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "正确答案：$answerText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (analysis.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "解析：$analysis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
