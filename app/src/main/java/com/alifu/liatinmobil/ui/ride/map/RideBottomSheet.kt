package com.alifu.liatinmobil.ui.ride.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.R
import com.alifu.liatinmobil.domain.model.RidePhase
import com.alifu.liatinmobil.ui.common.CarIcon
import com.alifu.liatinmobil.ui.ride.RideUiState
import com.alifu.liatinmobil.ui.ride.etaMinutes

@Composable
fun RideBottomSheet(
    state: RideUiState,
    onTogglePause: () -> Unit,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = state.snapshot.phase
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 40.dp, height = 4.dp)
                    .background(MaterialTheme.colorScheme.outline, CircleShape),
            )
            Text(
                rideTitle(state),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("ride_title"),
            )
            Text(
                rideSubtitle(state),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { state.snapshot.tripProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
            if (phase != RidePhase.SEARCHING_DRIVER) DriverRow(state)
            if (phase == RidePhase.ARRIVED) {
                Button(
                    onClick = onPlayAgain,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("play_again_button"),
                ) { Text(stringResource(R.string.ride_play_again), style = MaterialTheme.typography.titleMedium) }
            } else {
                OutlinedButton(
                    onClick = onTogglePause,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp).testTag("pause_button"),
                ) {
                    val label = if (state.isPaused) R.string.ride_resume else R.string.ride_pause
                    Text(stringResource(label), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun DriverRow(state: RideUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(52.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(state.driver.avatarEmoji, style = MaterialTheme.typography.headlineSmall) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(state.driver.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(state.car.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    state.driver.plateNumber,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        CarIcon(state.car, size = 64.dp)
    }
}

@Composable
private fun rideTitle(state: RideUiState): String {
    val destination = state.route.destination
    return when (state.snapshot.phase) {
        RidePhase.SEARCHING_DRIVER -> stringResource(R.string.ride_title_searching)
        RidePhase.DRIVER_COMING -> stringResource(R.string.ride_title_coming)
        RidePhase.PICKING_UP -> stringResource(R.string.ride_title_pickup)
        RidePhase.ON_TRIP -> stringResource(R.string.ride_title_trip, destination.emoji, destination.name)
        RidePhase.ARRIVED -> stringResource(R.string.ride_title_arrived, destination.name)
    }
}

@Composable
private fun rideSubtitle(state: RideUiState): String {
    val snapshot = state.snapshot
    return when (snapshot.phase) {
        RidePhase.SEARCHING_DRIVER -> stringResource(R.string.ride_subtitle_searching)
        RidePhase.DRIVER_COMING -> etaMinutes(snapshot.millisToPickup)
            ?.let { stringResource(R.string.ride_subtitle_coming, it) }
            ?: stringResource(R.string.ride_subtitle_coming_soon)
        RidePhase.PICKING_UP -> stringResource(R.string.ride_subtitle_pickup)
        RidePhase.ON_TRIP -> when {
            snapshot.isStoppedAtLight -> stringResource(R.string.ride_subtitle_red_light)
            else -> etaMinutes(snapshot.millisToArrival)
                ?.let { stringResource(R.string.ride_subtitle_trip, it) }
                ?: stringResource(R.string.ride_subtitle_trip_soon)
        }
        RidePhase.ARRIVED -> stringResource(R.string.ride_subtitle_arrived, state.car.name)
    }
}
