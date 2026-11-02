package com.alifu.liatinmobil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.alifu.liatinmobil.data.CarCatalog
import com.alifu.liatinmobil.data.LocalRouteRepository
import com.alifu.liatinmobil.data.RouteRepository
import com.alifu.liatinmobil.ui.carselection.CarSelectionScreen
import com.alifu.liatinmobil.ui.common.MissingContentScreen
import com.alifu.liatinmobil.ui.dashboard.DashboardScreen
import com.alifu.liatinmobil.ui.ride.RideRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    routeRepository: RouteRepository = LocalRouteRepository,
) {
    NavHost(navController, startDestination = DashboardDestination, modifier = modifier) {
        composable<DashboardDestination> {
            DashboardScreen(
                routes = routeRepository.getRoutes(),
                onRouteClick = { navController.navigate(CarSelectionDestination(it.id)) },
            )
        }
        composable<CarSelectionDestination> { entry ->
            val route = routeRepository.findRoute(entry.toRoute<CarSelectionDestination>().routeId)
            if (route == null) {
                MissingContentScreen(onBack = { navController.popBackStack() })
            } else {
                CarSelectionScreen(
                    route = route,
                    cars = CarCatalog.cars,
                    onBack = { navController.popBackStack() },
                    onBook = { car, minutes -> navController.navigate(RideDestination(route.id, car.id, minutes)) },
                )
            }
        }
        composable<RideDestination> { entry ->
            val args = entry.toRoute<RideDestination>()
            RideRoute(
                routeRepository = routeRepository,
                routeId = args.routeId,
                carId = args.carId,
                durationMinutes = args.durationMinutes,
                onBack = { navController.popBackStack() },
                onPlayAgain = { navController.popBackStack(DashboardDestination, inclusive = false) },
            )
        }
    }
}
