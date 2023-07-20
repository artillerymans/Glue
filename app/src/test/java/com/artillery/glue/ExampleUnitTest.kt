package com.artillery.glue

import org.junit.Test

import org.junit.Assert.*
import java.math.BigDecimal
import java.util.TimeZone

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val defaultTimeZone = TimeZone.getDefault()
        val rawOffsetMillis = defaultTimeZone.rawOffset
        val rawOffsetHours = rawOffsetMillis / (1000 * 60 * 60)
        println(rawOffsetHours)
    }


    @Test
    fun string2Int(){
        val valueStr = "2835217448"
        println(valueStr.toLong())
        /*val big = BigDecimal(valueStr)

        println(big.toString())
        println(big.toInt().toUInt())*/

    }
}