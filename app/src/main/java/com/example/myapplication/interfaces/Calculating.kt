package com.example.cal.interfaces

import com.example.cal.exceptions.InvalidFormulaException
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

interface Calculating{
    var number1: Number     // is required
    var number2: Number? // will be replaced by 0 or 1 if null, corresponding to called function
    fun plus(): Double
        = formatDecimalBy6Digits(number1.toDouble() + (number2?.toDouble() ?: 0.0))

    fun minus(): Double
        = formatDecimalBy6Digits(number1.toDouble() - (number2?.toDouble() ?: 0.0))

    fun multiply(): Double
        = formatDecimalBy6Digits(number1.toDouble() * (number2?.toDouble() ?: 1.0))

    fun divide(): Double {
        if (number2?.toDouble() == 0.0) throw InvalidFormulaException("Cannot divide by 0")
        return formatDecimalBy6Digits(number1.toDouble() / (number2?.toDouble() ?: 1.0))
    }

    fun power(): Double
        = formatDecimalBy6Digits(number1.toDouble().pow(number2?.toDouble() ?: 1.0))

    fun formatDecimalBy6Digits(number: Number): Double
        =  BigDecimal(number.toDouble()).setScale(6,RoundingMode.HALF_UP).toDouble()

}