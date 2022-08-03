package com.example.cal.models

enum class Operator() {
    PLUS, MINUS, DIVIDE, MULTIPLY, POWER, UNDEFINED;
    fun getVal(): String // get string value of called operator
    = when(this){
        PLUS -> "+"
        MINUS -> "-"
        MULTIPLY -> "*"
        DIVIDE -> "/"
        POWER -> "^"
        else -> ""
    }
}