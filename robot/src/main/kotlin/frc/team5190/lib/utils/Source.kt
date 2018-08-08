package frc.team5190.lib.utils

import kotlin.math.absoluteValue
import kotlin.math.sign

typealias DoubleSource = Source<Double>
typealias BooleanSource = Source<Boolean>

interface Source<T> {
    val value: T
}

fun <T> constSource(value: T) = object : Source<T> {
    override val value = value
}

fun <F, T> Source<F>.withProcessing(processor: (F) -> T) = object : Source<T> {
    override val value: T
        get() = processor(this@withProcessing.value)
}

fun DoubleSource.withThreshold(threshold: Double = 0.5): BooleanSource = withProcessing {
    val currentValue = this@withThreshold.value
    currentValue >= threshold
}

fun DoubleSource.withDeadband(deadband: Double, scaleDeadband: Boolean = true, maxMagnitude: Double = 1.0): DoubleSource = withProcessing {
    val currentValue = this@withDeadband.value
    if (currentValue in (-deadband)..deadband) return@withProcessing 0.0 // in deadband
    // outside deadband
    if (!scaleDeadband) return@withProcessing currentValue
    // scale so deadband is effective 0
    ((currentValue.absoluteValue - deadband) / (maxMagnitude - deadband)) * currentValue.sign
}