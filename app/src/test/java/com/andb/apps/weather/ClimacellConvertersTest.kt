package com.andb.apps.weather

import com.andb.apps.weather.data.model.climacell.MinutelySummaryPoint
import com.andb.apps.weather.data.model.climacell.generateMinutelySummary
import org.junit.Test

class ClimacellConvertersTest {
    @Test
    fun minutelySummaryTest() {
        assert(generateMinutelySummary(false, listOf()) == "No rain")
        assert(
            generateMinutelySummary(
                false,
                listOf(MinutelySummaryPoint(23, 0.4))
            ) == "Rain starting in 23 minutes"
        )
        assert(
            generateMinutelySummary(
                true,
                listOf(MinutelySummaryPoint(15, 0.0))
            ) == "Rain ending in 15 minutes"
        )
        println(
            generateMinutelySummary(
                false,
                listOf(MinutelySummaryPoint(14, 0.4), MinutelySummaryPoint(18, 0.0))
            )
        )
        assert(
            generateMinutelySummary(
                false,
                listOf(MinutelySummaryPoint(14, 0.4), MinutelySummaryPoint(18, 0.0))
            ) == "Rain starting in 14 minutes and ending 4 minutes later"
        )
        assert(
            generateMinutelySummary(
                true,
                listOf(MinutelySummaryPoint(12, 0.0), MinutelySummaryPoint(29, 0.7))
            ) == "Rain ending in 12 minutes and starting 17 minutes later"
        )
        assert(
            generateMinutelySummary(
                false,
                listOf(
                    MinutelySummaryPoint(3, 0.4),
                    MinutelySummaryPoint(34, 0.0),
                    MinutelySummaryPoint(41, 0.9)
                )
            ) == "Intermittent rain"
        )
        assert(
            generateMinutelySummary(
                true,
                listOf(
                    MinutelySummaryPoint(23, 0.0),
                    MinutelySummaryPoint(55, 0.1),
                    MinutelySummaryPoint(58, 0.0)
                )
            ) == "Intermittent rain"
        )
    }
}