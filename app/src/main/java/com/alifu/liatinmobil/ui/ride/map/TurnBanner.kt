package com.alifu.liatinmobil.ui.ride.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.R
import com.alifu.liatinmobil.domain.model.TurnDirection
import com.alifu.liatinmobil.domain.simulation.UpcomingTurn
import com.alifu.liatinmobil.ui.common.METERS_PER_UNIT
import kotlin.math.roundToInt

private const val DISTANCE_STEP_METERS = 10

/** Big green sign with an arrow, shown a few seconds before every sharp turn. */
@Composable
fun TurnBanner(turn: UpcomingTurn, modifier: Modifier = Modifier) {
    val isLeft = turn.direction == TurnDirection.LEFT
    val meters = (turn.distanceAhead * METERS_PER_UNIT / DISTANCE_STEP_METERS).roundToInt() * DISTANCE_STEP_METERS
    val textColor = MaterialTheme.colorScheme.onPrimary
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 8.dp,
        modifier = modifier.testTag("turn_banner").semantics(mergeDescendants = true) {},
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (isLeft) "⬅️" else "➡️", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(if (isLeft) R.string.ride_turn_left else R.string.ride_turn_right),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
                Text(
                    stringResource(R.string.ride_turn_distance, meters),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.9f),
                )
            }
        }
    }
}
