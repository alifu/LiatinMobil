package com.alifu.liatinmobil.ui.ride

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.R
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.ui.common.BackCircleButton
import com.alifu.liatinmobil.ui.common.color
import com.alifu.liatinmobil.ui.ride.map.CityMapCanvas
import com.alifu.liatinmobil.ui.ride.map.RideBottomSheet

private const val CAMERA_SWITCH_MILLIS = 900
private const val PULSE_MILLIS = 1_600

@Composable
fun RideMapScreen(
    state: RideUiState,
    onFrame: (deltaMillis: Long) -> Unit,
    onTogglePause: () -> Unit,
    onBack: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RideFrameClock(onFrame)
    KeepScreenOn()

    var showWholeRoute by rememberSaveable { mutableStateOf(false) }
    val overviewBlend by animateFloatAsState(
        targetValue = if (showWholeRoute || state.snapshot.phase == RidePhase.ARRIVED) 1f else 0f,
        animationSpec = tween(CAMERA_SWITCH_MILLIS),
        label = "overview",
    )
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    var topBarHeightPx by remember { mutableFloatStateOf(0f) }
    val pulse = if (state.snapshot.phase == RidePhase.SEARCHING_DRIVER) searchingPulse() else 0f

    Box(modifier.fillMaxSize()) {
        CityMapCanvas(
            route = state.route,
            snapshot = state.snapshot,
            carColor = state.car.color,
            contentDescription = stringResource(R.string.ride_map_description),
            modifier = Modifier.fillMaxSize().testTag("ride_map"),
            overviewBlend = overviewBlend,
            topInsetPx = topBarHeightPx,
            bottomInsetPx = sheetHeightPx,
            searchingPulse = pulse,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .onSizeChanged { topBarHeightPx = it.height.toFloat() }
                .statusBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackCircleButton(onClick = onBack)
            Box(Modifier.weight(1f))
            CameraToggle(showWholeRoute = showWholeRoute, onToggle = { showWholeRoute = !showWholeRoute })
        }
        RideBottomSheet(
            state = state,
            onTogglePause = onTogglePause,
            onPlayAgain = onPlayAgain,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { sheetHeightPx = it.height.toFloat() },
        )
    }
}

@Composable
private fun CameraToggle(showWholeRoute: Boolean, onToggle: () -> Unit) {
    Surface(onClick = onToggle, shape = RoundedCornerShape(50), shadowElevation = 4.dp) {
        Text(
            stringResource(if (showWholeRoute) R.string.ride_follow_car else R.string.ride_show_route),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

/** 0..1 repeating value for the radar rings while a driver is being searched. */
@Composable
private fun searchingPulse(): Float {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse",
    )
    return pulse
}

/** Feeds real frame-to-frame time to the ride. Frames stop when the app is hidden. */
@Composable
private fun RideFrameClock(onFrame: (Long) -> Unit) {
    val currentOnFrame by rememberUpdatedState(onFrame)
    LaunchedEffect(Unit) {
        var lastFrame = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                currentOnFrame(now - lastFrame)
                lastFrame = now
            }
        }
    }
}

/** A child watching a 5+ minute ride should not see the screen go dark. */
@Composable
private fun KeepScreenOn() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
}
