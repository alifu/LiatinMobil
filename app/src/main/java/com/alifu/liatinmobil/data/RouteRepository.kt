package com.alifu.liatinmobil.data

import com.alifu.liatinmobil.domain.model.Route

interface RouteRepository {
    fun getRoutes(): List<Route>
    fun findRoute(id: Int): Route?
}
