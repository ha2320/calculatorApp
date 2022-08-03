package com.example.cal.models

import com.example.cal.interfaces.Calculating

class Calculation(override var number1: Number=0, override var number2: Number?): Calculating{
    constructor(number1: Number) : this(number1, null)
    // secondary constructor, called if number2 is unavailable

}