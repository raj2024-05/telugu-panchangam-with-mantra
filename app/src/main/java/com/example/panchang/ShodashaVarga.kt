package com.example.panchang

import kotlin.math.floor

/**
 * Shodasha Varga - the 16 classical divisional (varga) charts of Parashari
 * jyotisha, plus the Rashi (D1) and Bhava (house) charts.
 *
 * For every varga except D1, D2 and D30 the result follows the general rule:
 *   partIndex = floor(degreeWithinSign / (30/n))
 *   resultSign = (startSign(rashiIndex) + step * partIndex) mod 12
 * D2 (Hora) and D30 (Trimsamsa) use their own classical (non-uniform / two-way)
 * rules and are special-cased below.
 */

enum class VargaChart(val code: String, val teluguName: String, val divisions: Int) {
    D1("D1", "రాశి చక్రం (D1)", 1),
    D2("D2", "హోర (D2)", 2),
    D3("D3", "ద్రేక్కాణం (D3)", 3),
    D4("D4", "చతుర్థాంశ (D4)", 4),
    D7("D7", "సప్తాంశ (D7)", 7),
    D9("D9", "నవాంశ (D9)", 9),
    D10("D10", "దశాంశ (D10)", 10),
    D12("D12", "ద్వాదశాంశ (D12)", 12),
    D16("D16", "షోడశాంశ (D16)", 16),
    D20("D20", "వింశాంశ (D20)", 20),
    D24("D24", "చతుర్వింశాంశ (D24)", 24),
    D27("D27", "సప్తవింశాంశ / భాంశ (D27)", 27),
    D30("D30", "త్రింశాంశ (D30)", 30),
    D40("D40", "ఖవేదాంశ (D40)", 40),
    D45("D45", "అక్షవేదాంశ (D45)", 45),
    D60("D60", "షష్ట్యంశ (D60)", 60),
    BHAVA("BHAVA", "భావ చక్రం (Bhava)", 0) // handled separately using house cusps
}

object ShodashaVargaCalculator {

    /** 0=movable(chara), 1=fixed(sthira), 2=dual(dwiswabhava) */
    private fun quality(rashiIndex: Int) = rashiIndex % 3

    /** 0=fire, 1=earth, 2=air, 3=water */
    private fun element(rashiIndex: Int) = rashiIndex % 4

    private fun isOddSign(rashiIndex: Int) = rashiIndex % 2 == 0 // Aries(0) is an odd sign

    /** Computes the resulting sign index (0=Aries..11=Pisces) for the given varga. */
    fun signForVarga(chart: VargaChart, siderealLongitude: Double): Int {
        val rashiIndex = floor(siderealLongitude / 30.0).toInt().coerceIn(0, 11)
        val degInRashi = siderealLongitude % 30.0

        return when (chart) {
            VargaChart.D1 -> rashiIndex

            VargaChart.D2 -> { // Hora: only Leo/Cancer
                val half = if (degInRashi < 15.0) 0 else 1
                val leo = 4; val cancer = 3
                if (isOddSign(rashiIndex)) (if (half == 0) leo else cancer)
                else (if (half == 0) cancer else leo)
            }

            VargaChart.D30 -> trimsamsaSign(rashiIndex, degInRashi)

            else -> {
                val n = chart.divisions
                val partIndex = floor(degInRashi / (30.0 / n)).toInt().coerceIn(0, n - 1)
                val (start, step) = startAndStep(chart, rashiIndex)
                (start + step * partIndex).mod(12)
            }
        }
    }

    private fun startAndStep(chart: VargaChart, rashiIndex: Int): Pair<Int, Int> {
        val q = quality(rashiIndex)     // 0 movable, 1 fixed, 2 dual
        val e = element(rashiIndex)     // 0 fire, 1 earth, 2 air, 3 water
        val odd = isOddSign(rashiIndex)
        return when (chart) {
            VargaChart.D3 -> rashiIndex to 4                              // own / 5th / 9th
            VargaChart.D4 -> rashiIndex to 3                              // own / 4th / 7th / 10th
            VargaChart.D7 -> (if (odd) rashiIndex else (rashiIndex + 6) % 12) to 1
            VargaChart.D9 -> when (q) {
                0 -> rashiIndex to 1
                1 -> (rashiIndex + 8) % 12 to 1
                else -> (rashiIndex + 4) % 12 to 1
            }
            VargaChart.D10 -> (if (odd) rashiIndex else (rashiIndex + 8) % 12) to 1
            VargaChart.D12 -> rashiIndex to 1
            VargaChart.D16 -> when (q) { 0 -> 0 to 1; 1 -> 4 to 1; else -> 8 to 1 } // Aries/Leo/Sagittarius
            VargaChart.D20 -> when (q) { 0 -> 0 to 1; 1 -> 8 to 1; else -> 4 to 1 } // Aries/Sagittarius/Leo
            VargaChart.D24 -> (if (odd) 4 else 3) to 1                    // Leo / Cancer
            VargaChart.D27 -> when (e) { 0 -> 0 to 1; 1 -> 3 to 1; 2 -> 6 to 1; else -> 9 to 1 } // fire/earth/air/water
            VargaChart.D40 -> (if (odd) 0 else 6) to 1                    // Aries / Libra
            VargaChart.D45 -> when (q) { 0 -> 0 to 1; 1 -> 4 to 1; else -> 8 to 1 } // Aries/Leo/Sagittarius
            VargaChart.D60 -> rashiIndex to 1
            else -> rashiIndex to 1
        }
    }

    /** Trimsamsa (D30) - unequal, planetary-lord based division (classical rule). */
    private fun trimsamsaSign(rashiIndex: Int, degInRashi: Double): Int {
        val odd = isOddSign(rashiIndex)
        // sign indices: Aries0 Taurus1 Gemini2 Cancer3 Leo4 Virgo5 Libra6 Scorpio7
        // Sagittarius8 Capricorn9 Aquarius10 Pisces11
        return if (odd) {
            when {
                degInRashi < 5.0 -> 0   // Mars -> Aries
                degInRashi < 10.0 -> 10 // Saturn -> Aquarius
                degInRashi < 18.0 -> 8  // Jupiter -> Sagittarius
                degInRashi < 25.0 -> 2  // Mercury -> Gemini
                else -> 6               // Venus -> Libra
            }
        } else {
            when {
                degInRashi < 5.0 -> 1   // Venus -> Taurus
                degInRashi < 12.0 -> 5  // Mercury -> Virgo
                degInRashi < 20.0 -> 11 // Jupiter -> Pisces
                degInRashi < 25.0 -> 9  // Saturn -> Capricorn
                else -> 7               // Mars -> Scorpio
            }
        }
    }

    /**
     * Bhava (house) placement using the sidereal house-cusp boundaries already
     * computed by Swiss Ephemeris (swe_houses). cusps must be a 13-element
     * array as returned by swe_houses (index 1..12 = house cusps 1..12).
     */
    fun bhavaHouse(siderealLongitude: Double, cusps: DoubleArray): Int {
        for (h in 1..12) {
            val start = cusps[h]
            val end = cusps[if (h == 12) 1 else h + 1]
            val inHouse = if (start < end) {
                siderealLongitude >= start && siderealLongitude < end
            } else { // wraps past 360/0
                siderealLongitude >= start || siderealLongitude < end
            }
            if (inHouse) return h
        }
        return 1
    }
}
