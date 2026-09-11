package com.example.panchang

import kotlin.math.floor

enum class VargaKind(val label: String, val divisions: Int) {
    D1("రాశి చక్రం (D1)", 1),
    D2("హోర (D2)", 2),
    D3("ద్రేక్కాణ (D3)", 3),
    D4("చతుర్థాంశ (D4)", 4),
    D7("సప్తమాంశ (D7)", 7),
    D9("నవాంశ (D9)", 9),
    D10("దశమాంశ (D10)", 10),
    D12("ద్వాదశాంశ (D12)", 12),
    D16("షోడశాంశ (D16)", 16),
    D20("వింశాంశ (D20)", 20),
    D24("సిద్ధాంశ / చతుర్వింశాంశ (D24)", 24),
    D27("భాంశ / నక్షత్రాంశ (D27)", 27),
    D30("త్రింశాంశ (D30)", 30),
    D40("ఖవేదాంశ (D40)", 40),
    D45("అక్షవేదాంశ (D45)", 45),
    D60("షష్ట్యాంశ (D60)", 60),
    BHAVA("భావ చక్రం (Equal House)", 0),
    SRIPATI("శ్రీపతి భావ చక్రం", 0)
}

object VargaCalculator {
    private val movable = setOf(0, 3, 6, 9)
    private val fixed = setOf(1, 4, 7, 10)
    private val dual = setOf(2, 5, 8, 11)

    private fun norm12(i: Int): Int {
        var x = i % 12
        if (x < 0) x += 12
        return x
    }

    fun vargaSign(longitude: Double, kind: VargaKind): Int {
        val signIndex = floor(longitude / 30.0).toInt().coerceIn(0, 11)
        val degInSign = longitude - signIndex * 30.0
        val isOddSign = signIndex % 2 == 0

        return when (kind) {
            VargaKind.D1 -> signIndex
            VargaKind.D2 -> {
                val part = floor(degInSign / 15.0).toInt().coerceIn(0, 1)
                if (isOddSign) (if (part == 0) 4 else 3) else (if (part == 0) 3 else 4)
            }
            VargaKind.D3 -> {
                val part = floor(degInSign / 10.0).toInt().coerceIn(0, 2)
                norm12(signIndex + part * 4)
            }
            VargaKind.D4 -> {
                val part = floor(degInSign / 7.5).toInt().coerceIn(0, 3)
                norm12(signIndex + part * 3)
            }
            VargaKind.D7 -> {
                val partSize = 30.0 / 7.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 6)
                val start = if (isOddSign) signIndex else norm12(signIndex + 6)
                norm12(start + part)
            }
            VargaKind.D9 -> {
                val partSize = 30.0 / 9.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 8)
                val elementGroup = signIndex % 4
                val startMap = intArrayOf(0, 9, 6, 3)
                norm12(startMap[elementGroup] + part)
            }
            VargaKind.D10 -> {
                val part = floor(degInSign / 3.0).toInt().coerceIn(0, 9)
                val start = if (isOddSign) signIndex else norm12(signIndex + 8)
                norm12(start + part)
            }
            VargaKind.D12 -> {
                val part = floor(degInSign / 2.5).toInt().coerceIn(0, 11)
                norm12(signIndex + part)
            }
            VargaKind.D16 -> {
                val partSize = 30.0 / 16.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 15)
                val start = when (signIndex) {
                    in movable -> 0
                    in fixed -> 4
                    else -> 8
                }
                norm12(start + part)
            }
            VargaKind.D20 -> {
                val partSize = 30.0 / 20.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 19)
                val start = when (signIndex) {
                    in movable -> 0
                    in dual -> 4
                    else -> 8
                }
                norm12(start + part)
            }
            VargaKind.D24 -> {
                val partSize = 30.0 / 24.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 23)
                val start = if (isOddSign) 4 else 3
                norm12(start + part)
            }
            VargaKind.D27 -> {
                val partSize = 30.0 / 27.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 26)
                val elementGroup = signIndex % 4
                val startMap = intArrayOf(0, 3, 6, 9)
                norm12(startMap[elementGroup] + part)
            }
            VargaKind.D30 -> {
                if (isOddSign) {
                    when {
                        degInSign < 5.0 -> 0
                        degInSign < 10.0 -> 10
                        degInSign < 18.0 -> 8
                        degInSign < 25.0 -> 2
                        else -> 6
                    }
                } else {
                    when {
                        degInSign < 5.0 -> 1
                        degInSign < 12.0 -> 5
                        degInSign < 20.0 -> 11
                        degInSign < 25.0 -> 9
                        else -> 7
                    }
                }
            }
            VargaKind.D40 -> {
                val part = floor(degInSign / 0.75).toInt().coerceIn(0, 39)
                val start = if (isOddSign) 0 else 6
                norm12(start + part)
            }
            VargaKind.D45 -> {
                val partSize = 30.0 / 45.0
                val part = floor(degInSign / partSize).toInt().coerceIn(0, 44)
                val start = when (signIndex) {
                    in movable -> 0
                    in fixed -> 4
                    else -> 8
                }
                norm12(start + part)
            }
            VargaKind.D60 -> {
                val part = floor(degInSign / 0.5).toInt().coerceIn(0, 59)
                norm12(signIndex + part)
            }
            VargaKind.BHAVA, VargaKind.SRIPATI -> signIndex
        }
    }

    fun bhavaNumber(planetLongitude: Double, ascendantLongitude: Double): Int {
        var diff = (planetLongitude - ascendantLongitude) % 360.0
        if (diff < 0) diff += 360.0
        return floor(diff / 30.0).toInt().coerceIn(0, 11) + 1
    }

    fun sripatiHouseNumber(planetLongitude: Double, cusps: DoubleArray): Int {
        if (cusps.size < 13) return 1
        val p = normalize360(planetLongitude)
        for (house in 1..12) {
            val start = cusps[house]
            val end = if (house == 12) cusps[1] else cusps[house + 1]
            val span = forwardArc(start, end)
            val pos = forwardArc(start, p)
            if (pos < span || kotlin.math.abs(pos - span) < 1e-8) return house
        }
        return 1
    }

    private fun normalize360(x0: Double): Double {
        var x = x0 % 360.0
        if (x < 0) x += 360.0
        return x
    }

    private fun forwardArc(start: Double, end: Double): Double {
        var d = normalize360(end) - normalize360(start)
        if (d < 0) d += 360.0
        return d
    }
}
