package com.alifu.liatinmobil.ui.ride

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.alifu.liatinmobil.domain.simulation.RideSoundCue
import com.alifu.liatinmobil.ui.common.BackCircleButton
import com.alifu.liatinmobil.ui.common.CircleTextButton
import com.alifu.liatinmobil.ui.common.color
import com.alifu.liatinmobil.ui.ride.map.CityMapCanvas
import com.alifu.liatinmobil.ui.ride.map.RideBottomSheet
import com.alifu.liatinmobil.ui.ride.map.TurnBanner
import com.alifu.liatinmobil.ui.ride.map.carHopHeight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

private const val CAMERA_SWITCH_MILLIS = 900

/** A honk makes the car jump about twice as high as a normal hop. */
private const val HONK_HOP_HEIGHT = 2f
private const val HONK_UP_MILLIS = 120
private const val HONK_DOWN_MILLIS = 260

/** Top controls are 48 dp high with 12 dp padding around them. */
private val TURN_BANNER_TOP_PADDING = 72.dp
private const val PULSE_MILLIS = 1_600

@Composable
fun RideMapScreen(
    state: RideUiState,
    onFrame: (deltaMillis: Long) -> Unit,
    onTogglePause: () -> Unit,
    onBack: () -> Unit,
    onPlayAgain: () -> Unit,
    soundCues: Flow<RideSoundCue> = emptyFlow(),
    modifier: Modifier = Modifier,
) {
    RideFrameClock(onFrame)
    KeepScreenOn()

    val soundPlayer = rememberRideSoundPlayer()
    var isMuted by rememberSaveable { mutableStateOf(false) }
    val currentlyMuted by rememberUpdatedState(isMuted)
    LaunchedEffect(soundCues) {
        soundCues.collect { cue -> if (!currentlyMuted) soundPlayer.play(cue) }
    }
    LaunchedEffect(isMuted) { if (isMuted) soundPlayer.stopVoice() }

    val honkHop = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val honk = {
        if (!currentlyMuted) soundPlayer.playHorn()
        scope.launch {
            honkHop.snapTo(0f)
            honkHop.animateTo(HONK_HOP_HEIGHT, tween(HONK_UP_MILLIS))
            honkHop.animateTo(0f, tween(HONK_DOWN_MILLIS))
        }
        Unit
    }

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
            carHop = maxOf(carHopHeight(state.elapsedMillis, state.snapshot.speedRatio), honkHop.value),
            onCarTap = honk,
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
            CircleTextButton(
                label = if (isMuted) "🔇" else "🔊",
                contentDescription = stringResource(if (isMuted) R.string.ride_unmute else R.string.ride_mute),
                onClick = { isMuted = !isMuted },
                modifier = Modifier.testTag("mute_button"),
            )
            Spacer(Modifier.width(8.dp))
            CameraToggle(showWholeRoute = showWholeRoute, onToggle = { showWholeRoute = !showWholeRoute })
        }
        state.snapshot.upcomingTurn?.let { turn ->
            AnimatedVisibility(
                visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
                modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = TURN_BANNER_TOP_PADDING),
                enter = fadeIn() + slideInVertically { -it },
            ) { TurnBanner(turn) }
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
