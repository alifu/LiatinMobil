package com.alifu.liatinmobil.ui.carselection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.domain.model.CarType
import com.alifu.liatinmobil.ui.common.CarIcon

@Composable
fun CarOptionCard(car: CarType, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) colors.primary else colors.outline),
        modifier = modifier.fillMaxWidth().testTag("car_option_${car.id}"),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CarIcon(car, size = 60.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(car.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(car.description, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text("⭐".repeat(car.stars), style = MaterialTheme.typography.bodySmall)
            }
            RadioButton(selected = isSelected, onClick = onSelect)
        }
    }
}
