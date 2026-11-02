package com.alifu.liatinmobil.domain.model

/** A selectable car. [bodyColorArgb] is an ARGB color so the domain stays UI-free. */
data class CarType(
    val id: String,
    val name: String,
    val description: String,
    val bodyColorArgb: Long,
    val stars: Int,
)

/** A pretend driver shown in the ride bottom sheet. */
data class Driver(
    val name: String,
    val plateNumber: String,
    val avatarEmoji: String,
)
