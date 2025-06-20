package com.example.aimoodjournal.common

import java.math.BigDecimal
import java.math.RoundingMode

fun Float.roundTo(decimalPlaces: Int): Float {
    return BigDecimal(this.toDouble())
        .setScale(decimalPlaces, RoundingMode.HALF_UP)
        .toFloat()
}