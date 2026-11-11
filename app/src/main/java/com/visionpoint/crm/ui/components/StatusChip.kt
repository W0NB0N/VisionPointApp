package com.visionpoint.crm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.visionpoint.crm.ui.theme.StatusCompletedBg
import com.visionpoint.crm.ui.theme.StatusCompletedColor
import com.visionpoint.crm.ui.theme.StatusPendingBg
import com.visionpoint.crm.ui.theme.StatusPendingColor
import com.visionpoint.crm.ui.theme.StatusReadyBg
import com.visionpoint.crm.ui.theme.StatusReadyColor

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status.lowercase()) {
        "completed" -> StatusCompletedBg to StatusCompletedColor
        "ready" -> StatusReadyBg to StatusReadyColor
        else -> StatusPendingBg to StatusPendingColor
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}
