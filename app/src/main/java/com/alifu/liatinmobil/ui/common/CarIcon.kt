package com.alifu.liatinmobil.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.domain.model.CarType
import com.alifu.liatinmobil.ui.ride.map.drawTopDownCar

private const val FACING_RIGHT = 90f

/** The same top-down car used on the map, facing right, for lists and headers. */
@Composable
fun CarIcon(car: CarType, modifier: Modifier = Modifier, size: Dp = 56.dp) {
    Canvas(modifier.size(size).semantics { contentDescription = car.name }) {
        drawTopDownCar(center, FACING_RIGHT, car.color, this.size.minDimension * 0.9f)
    }
}

val CarType.color: Color get() = Color(bodyColorArgb)
