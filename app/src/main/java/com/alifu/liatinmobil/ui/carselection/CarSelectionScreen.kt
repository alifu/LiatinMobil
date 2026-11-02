package com.alifu.liatinmobil.ui.carselection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifu.liatinmobil.R
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.domain.model.CarType
import com.alifu.liatinmobil.domain.model.RideDuration
import com.alifu.liatinmobil.domain.model.Route
import com.alifu.liatinmobil.ui.common.BackCircleButton
import com.alifu.liatinmobil.ui.common.color
import com.alifu.liatinmobil.ui.ride.map.CityMapCanvas
import com.alifu.liatinmobil.ui.theme.DestinationOrange
import com.alifu.liatinmobil.ui.theme.LiatinMobilTheme

@Composable
fun CarSelectionScreen(
    route: Route,
    cars: List<CarType>,
    onBack: () -> Unit,
    onBook: (car: CarType, durationMinutes: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(cars.isNotEmpty()) { "CarSelectionScreen needs at least one car" }
    var selectedCarId by rememberSaveable { mutableStateOf(cars.first().id) }
    var durationMinutes by rememberSaveable { mutableIntStateOf(RideDuration.DEFAULT_MINUTES) }
    val selectedCar = cars.firstOrNull { it.id == selectedCarId } ?: cars.first()

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.weight(1f).testTag("car_selection_list"),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { MapPreview(route, selectedCar, onBack) }
            item { RouteSummary(route, Modifier.padding(horizontal = 16.dp)) }
            item { SectionTitle(stringResource(R.string.car_selection_car_section)) }
            items(cars, key = { it.id }) { car ->
                CarOptionCard(
                    car = car,
                    isSelected = car.id == selectedCar.id,
                    onSelect = { selectedCarId = car.id },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            item { SectionTitle(stringResource(R.string.car_selection_duration_section)) }
            item {
                DurationChips(
                    selectedMinutes = durationMinutes,
                    onSelect = { durationMinutes = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
        BookBar(onClick = { onBook(selectedCar, durationMinutes) })
    }
}

@Composable
private fun MapPreview(route: Route, car: CarType, onBack: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)),
    ) {
        CityMapCanvas(
            route = route,
            snapshot = null,
            carColor = car.color,
            contentDescription = stringResource(
                R.string.car_selection_map_description, route.origin.name, route.destination.name,
            ),
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            Modifier.statusBarsPadding().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackCircleButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Surface(shape = RoundedCornerShape(50), shadowElevation = 4.dp) {
                Text(
                    stringResource(R.string.car_selection_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun RouteSummary(route: Route, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RouteStop(MaterialTheme.colorScheme.primary, "${route.origin.emoji} ${route.origin.name}")
            RouteStop(DestinationOrange, "${route.destination.emoji} ${route.destination.name}")
        }
    }
}

@Composable
private fun RouteStop(dotColor: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(dotColor, CircleShape))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
    )
}

@Composable
private fun DurationChips(selectedMinutes: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RideDuration.optionsMinutes.forEach { minutes ->
            FilterChip(
                selected = minutes == selectedMinutes,
                onClick = { onSelect(minutes) },
                label = { Text(stringResource(R.string.car_selection_duration_option, minutes)) },
                modifier = Modifier.weight(1f).height(48.dp).testTag("duration_$minutes"),
            )
        }
    }
}

@Composable
private fun BookBar(onClick: () -> Unit) {
    Surface(shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .height(56.dp)
                .testTag("book_button"),
        ) {
            Text(stringResource(R.string.car_selection_book), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CarSelectionPreview() {
    LiatinMobilTheme {
        CarSelectionScreen(LocalRouteRepository.getRoutes()[5], CarCatalog.cars, onBack = {}, onBook = { _, _ -> })
    }
}
