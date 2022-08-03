package com.example.cal.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.cal.R
import com.example.cal.databinding.ActivityMainBinding
import com.example.cal.models.Calculation
import com.example.cal.models.Operator

class MainActivity : AppCompatActivity() {
    private var binding : ActivityMainBinding? = null
    private var resultStr: String = "" // string value of the calculated result
    private var numb1Str: String = ""
    private var numb2Str: String = ""

    private var formulaStr: String = ""
    // represent the math
    // made up from $numb1Str$currentOperator$numb2Str

    // two variables below define whether a number or the math is being edited
    private var editingFirstNumber = true
    private var editingSecondNumber = false

    /*
    * all position markers are declared with value of -5
    * it is an invalid position in a string/array
    * can not be -1
    * due to the fact that it will be compared to an_array_length-1
     */
    private var numb1DotPos = -5
    private var numb2DotPos = -5
    private var operatorPosition = -5 // necessary for delete-1-character button
    private var currentOperator: Operator = Operator.UNDEFINED
    private var digitButtonClickListener: View.OnClickListener? = null
    private var operatorButtonClickListener: View.OnClickListener? = null
    private var calculation: Calculation? = null

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        numb1Str = savedInstanceState.getString("firstNumberString","0.0").ifBlank { "0.0" }
        numb2Str = savedInstanceState.getString("secondNumberString","0.0").ifBlank { "0.0" }
        resultStr = savedInstanceState.getString("resultString", "0.0").ifBlank { "0.0" }
        currentOperator = getOpFromString(savedInstanceState.getString("operator",""))
        updateFormula()
        Log.d("result check",resultStr)
        binding?.resultOutput?.text = resultStr
    }

    private fun getOpFromString(operatorStr: String?): Operator
            = when (operatorStr){
        "+" -> Operator.PLUS
        "-" -> Operator.MINUS
        "*" -> Operator.MULTIPLY
        "/" -> Operator.DIVIDE
        else -> Operator.UNDEFINED
    }

    // configuration change calls onRestart() -> onStart()
    // therefore onCreate isn't overridden
    // all setup is done in onStart()
    override fun onStart() {
        super.onStart()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        declareDigitAndOpsOnClickListeners()
        // set onClickListeners
        handleClicks()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("firstNumberString", numb1Str)
        outState.putString("secondNumberString", numb2Str)
        outState.putString("resultString", resultStr)
        outState.putString("operator", currentOperator.getVal())
    }
    override fun onStop() {
        super.onStop()
        binding = null
        digitButtonClickListener = null
        operatorButtonClickListener = null
    }

    private fun declareDigitAndOpsOnClickListeners() {
        // Declare OnClick Listener for Digit Buttons
        digitButtonClickListener = View.OnClickListener { digitButton ->
            // if old result is available, clear it and start a new calculation
            if (resultStr.isNotBlank()) {
                onStartNewCalculation()
                resultStr = ""
            }
            val numberDigit: String = getNumberStringValueFromDigitButtonID(digitButton!!.id)

            // replace first 0 digits while second character isn't a dot
            // insert new digit to current number
            if (editingFirstNumber) numb1Str =
                if (numb1Str != "" && numb1Str.first() == '0' && numb1Str[1] != '.') numberDigit
                else numb1Str + numberDigit
            else numb2Str =
                if (numb2Str != "" && numb2Str.first() == '0' && numb2Str[1] != '.') numberDigit
                else numb2Str + numberDigit
            updateFormula()
        }
        // Declare OnClick Listener for Operator Buttons
        operatorButtonClickListener = View.OnClickListener { operatorButton ->
            val newOp: Operator = getOpStringFromOpButton(operatorButton.id)
            val highPriorityOperators = listOf(
                Operator.MULTIPLY,
                Operator.DIVIDE,
                Operator.POWER
            )

            /*
            * if minus is pressed
            * does it define a negative number input or a minus mark?
            * if currentOp has high priority: number2 is negative
            * or else, what if currentOp isn't defined?
            * number2 wouldn't be being edited by then
            * which makes number1 negative if currently blank
            * or else it is an ordinary minus math
            * however, old result is taken as number1 on start of a new calculation
            */
            if (
                newOp == Operator.MINUS
                && (currentOperator in highPriorityOperators || currentOperator == Operator.UNDEFINED)
                && resultStr.isBlank()
            ) {
                if (editingFirstNumber && numb1Str.isBlank()) numb1Str = "-"
                else if (editingSecondNumber && numb2Str.isBlank()) numb2Str = "-"
                else if (numb2Str.isBlank()) onFinishFirstNumber(newOp)
            } else if (numb1Str.isNotBlank()) {
                // if number1 isn't blank, it would probably have been finished
                if (resultStr.isNotBlank()) {
                    onStartNewCalculation()
                    numb1Str = resultStr
                    resultStr = ""
                }

                /*
                * finish number1,
                * specify currentOp,
                * move on to number2
                */
                onFinishFirstNumber(newOp)
            } else if (resultStr.isBlank() && numb1Str.isBlank()) {
                // this is the start of a new calculation, seems negative
                numb1Str = "-"
            }
            updateFormula()
        }
    }

    private fun onFinishFirstNumber(newOp: Operator) {
        editingFirstNumber = false
        editingSecondNumber = true
        operatorPosition = numb1Str.length
        currentOperator = newOp
        updateFormula()
    }

    private fun handleClicks() {
        // handle clicks for digit buttons
        /* pressing a number / dot after a math will lead to a new calculation
        even with the old result stay onscreen */
        binding!!.numb1.setOnClickListener(digitButtonClickListener)
        binding!!.numb2.setOnClickListener(digitButtonClickListener)
        binding!!.numb3.setOnClickListener(digitButtonClickListener)
        binding!!.numb4.setOnClickListener(digitButtonClickListener)
        binding!!.numb5.setOnClickListener(digitButtonClickListener)
        binding!!.numb6.setOnClickListener(digitButtonClickListener)
        binding!!.numb7.setOnClickListener(digitButtonClickListener)
        binding!!.numb8.setOnClickListener(digitButtonClickListener)
        binding!!.numb9.setOnClickListener(digitButtonClickListener)
        binding!!.numb0.setOnClickListener(digitButtonClickListener)

        binding?.dot?.setOnClickListener{   onDotClickEvent()   }

        // handle clicks for operator buttons
        /* minus mark (" - " ) also define a negative number, therefore should have specific cases handled
        *  */
        binding!!.opPlus.setOnClickListener(operatorButtonClickListener)
        binding!!.opMinus.setOnClickListener(operatorButtonClickListener)
        binding!!.opMultiply.setOnClickListener(operatorButtonClickListener)
        binding!!.opDivide.setOnClickListener(operatorButtonClickListener)
        binding!!.opPower.setOnClickListener(operatorButtonClickListener)

        // handle clicks for action buttons
        handleDeleteButtonClick()   // split into a separated function for being too long
        binding!!.clearAll.setOnClickListener{clearAll()}

        /*
         absolutely must press "=" button to show result
         pressing any other operator button before showing the result will change the math
         */
        binding!!.showResult.setOnClickListener { showResult() }
    }

    private fun onDotClickEvent() {
        if (resultStr.isNotBlank()) {
            // on click =-> start a new math if old result hasn't been cleared
            onStartNewCalculation()
            resultStr = ""
        }
        // automatically recognize 0. if pressed before an empty number
        if (editingFirstNumber) {
            if (numb1Str == "" || numb1Str.startsWith("-")) numb1Str += "0."
            else if (numb1DotPos == -5) numb1Str += "."
            numb1DotPos = numb1Str.length
        } else if (editingSecondNumber) {
            if (numb2Str == "" || numb2Str.startsWith("-")) numb2Str += "0."
            else if (numb2DotPos == -5) numb2Str += "."
            numb2DotPos = numb2Str.length
        }
        updateFormula()
    }

    private fun showResult() {
        calculation =
            if (numb2Str.isBlank()) Calculation(numb1Str.ifBlank { "0" }.toDouble())
            else Calculation(number1 = numb1Str.toDouble(), number2 = numb2Str.toDouble())
        try {
            resultStr = doMath()
        } catch (e: Exception){
            binding?.errorTextOutput?.text = e.message
        }
        binding?.resultOutput?.text = resultStr.ifBlank {"0.0"}
    }

    private fun doMath() = when (currentOperator) {
        Operator.PLUS -> calculation?.plus().toString()
        Operator.MINUS -> calculation?.minus().toString()
        Operator.MULTIPLY -> calculation?.multiply().toString()
        Operator.DIVIDE -> calculation?.divide().toString()
        Operator.POWER -> calculation?.power().toString()
        else -> calculation?.formatDecimalBy6Digits(numb1Str.ifBlank { "0" }.toDouble()).toString()
    }

    private fun clearAll() {
        onStartNewCalculation()
        updateFormula()
        resultStr = ""
        binding!!.resultOutput.text = "0.0"
    }

    private fun onStartNewCalculation() {
        numb1Str = ""
        numb2Str = ""
        numb1DotPos = -5
        numb2DotPos = -5
        currentOperator = Operator.UNDEFINED
        editingFirstNumber = true
        editingSecondNumber = false
    }

    private fun handleDeleteButtonClick() {
        binding?.characterDeleter?.setOnClickListener {
            // set edited object
            when {
                formulaStr.length-1 == operatorPosition -> {
                    editingFirstNumber = false
                    editingSecondNumber = false
                }
                formulaStr.length-1 < operatorPosition -> {
                    editingFirstNumber = true
                    editingSecondNumber = false
                }
                formulaStr.length-1 > operatorPosition && operatorPosition > 0 -> {
                    editingFirstNumber = false
                    editingSecondNumber = true
                }
            }
            if (editingFirstNumber && numb1Str!="") {
                numb1Str = numb1Str.dropLast(1)
                if(numb1DotPos == numb1Str.length) numb2DotPos = -5
            }
            else if (editingSecondNumber) {
                if( numb2Str!= "") numb2Str = numb2Str.dropLast(1)
                else if( operatorPosition == -5 ) numb1Str = numb1Str.dropLast(1)
                if(numb2DotPos == numb2Str.length) numb2DotPos = -5
            }
            else {
                currentOperator = Operator.UNDEFINED
                editingFirstNumber = true
                operatorPosition = -5
            }
            resultStr = ""
            updateFormula()
        }
    }

    private fun getOpStringFromOpButton(operatorButtonID: Int): Operator
            = when (operatorButtonID) {
        binding!!.opPlus.id -> Operator.PLUS
        binding!!.opMinus.id -> Operator.MINUS
        binding!!.opMultiply.id -> Operator.MULTIPLY
        binding!!.opDivide.id -> Operator.DIVIDE
        binding!!.opPower.id -> Operator.POWER
        else -> Operator.UNDEFINED
    }

    private fun getNumberStringValueFromDigitButtonID(numberButtonID: Int): String
            =  when(numberButtonID){
        binding!!.numb1.id -> resources.getString(R.string._1)
        binding!!.numb2.id -> resources.getString(R.string._2)
        binding!!.numb3.id -> resources.getString(R.string._3)
        binding!!.numb4.id -> resources.getString(R.string._4)
        binding!!.numb5.id -> resources.getString(R.string._5)
        binding!!.numb6.id -> resources.getString(R.string._6)
        binding!!.numb7.id -> resources.getString(R.string._7)
        binding!!.numb8.id -> resources.getString(R.string._8)
        binding!!.numb9.id -> resources.getString(R.string._9)
        binding!!.numb0.id -> resources.getString(R.string._0)
        else -> {""}
    }

    private fun updateFormula() {
        formulaStr =
            if(currentOperator == Operator.UNDEFINED) numb1Str
            else if (numb2Str.startsWith("-")) "$numb1Str${currentOperator.getVal()}($numb2Str)"
            else "$numb1Str${currentOperator.getVal()}$numb2Str"
        binding!!.formulaOutput.text = formulaStr.ifBlank { "0.0" }
    }
}

