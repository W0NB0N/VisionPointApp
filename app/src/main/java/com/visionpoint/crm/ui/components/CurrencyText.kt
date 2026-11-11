package com.visionpoint.crm.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val formatted = formatter.format(amount)

    Text(
        text = formatted,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        color = color
    )
}
