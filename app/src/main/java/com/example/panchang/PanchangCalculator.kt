package com.example.panchang

import de.thmac.swisseph.DblObj
import de.thmac.swisseph.SweConst
import de.thmac.swisseph.SweDate
import de.thmac.swisseph.SwissEph
import java.util.*
import kotlin.math.*

/**
 * Panchang (Hindu almanac) calculator backed by the Swiss Ephemeris (Moshier
 * analytical model - SEFLG_MOSEPH). This gives arc-second level accuracy for
 * planetary longitudes with NO external ephemeris data files required, so it
 * works fully offline on Android.
 *
 * Ayanamsa used: Lahiri (Chitrapaksha) - the standard for Indian Panchangs.
 */

data class PanchangResult(
    val tithiIndex: Int,
    val tithiName: String,
    val paksham: String,
    val nakshatraName: String,
    val yogaName: String,
    val karanaName: String,
    val varaName: String,
    val masaName: String,
    val sunriseMinutes: Double,
    val sunsetMinutes: Double,
    val rahuKalam: Pair<Double, Double>,
    val yamagandam: Pair<Double, Double>,
    val gulikaKalam: Pair<Double, Double>,
    val moonRashi: String,
    val sunRashi: String,
    // Local minutes from midnight; can exceed 1440 for an after-midnight ending.
    val tithiEndMinutes: Double = Double.NaN,
    val nakshatraEndMinutes: Double = Double.NaN,
    val yogaEndMinutes: Double = Double.NaN,
    val karanaEndMinutes: Double = Double.NaN,
    val varjyamWindows: List<Pair<Double, Double>> = emptyList(),
    val durmuhurthamWindows: List<Pair<Double, Double>> = emptyList()
)

data class DashaPeriod(
    val lordTelugu: String,
    val startYearsFromNow: Double,
    val endYearsFromNow: Double
)

data class VimshottariDashaNode(
    val level: Int,
    val lordTelugu: String,
    val startJd: Double,
    val endJd: Double,
    val children: List<VimshottariDashaNode> = emptyList()
)

object SwissEphManager {
    // Passing null means "no ephemeris data directory configured" - the library
    // automatically falls back to the built-in Moshier model (SEFLG_MOSEPH),
    // which needs no external files and is accurate to about 1 arc-second for
    // the planets (a few arc-seconds for the Moon) - more than sufficient for
    // Panchang / horoscope purposes.
    val swe: SwissEph by lazy {
        val s = SwissEph() // no ephemeris path configured -> falls back to Moshier
        s.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0.0, 0.0)
        s
    }
}

object PanchangCalculator {

    // Sidereal (Nirayana) planet longitude, Moshier model, arc-second accuracy.
    private val CALC_FLAGS =
        SweConst.SEFLG_MOSEPH or SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_SPEED

    private fun norm360(d: Double): Double {
        var x = d % 360.0
        if (x < 0) x += 360.0
        return x
    }

    /** Julian Day (UT) for a calendar date + fractional UTC hour, via Swiss Ephemeris. */
    fun julianDay(year: Int, month: Int, day: Int, hourUTC: Double): Double {
        return SweDate(year, month, day, hourUTC).julDay
    }

    /** Sidereal ecliptic longitude (degrees, Lahiri ayanamsa) of a Swiss Ephemeris body. */
    fun planetLongitude(jdUT: Double, seBody: Int): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        SwissEphManager.swe.swe_calc_ut(jdUT, seBody, CALC_FLAGS, xx, serr)
        return norm360(xx[0])
    }

    fun sunLongitude(jdUT: Double) = planetLongitude(jdUT, SweConst.SE_SUN)
    fun moonLongitude(jdUT: Double) = planetLongitude(jdUT, SweConst.SE_MOON)

    /** All nine grahas (Navagraha) sidereal longitudes, degrees. Ketu = Rahu + 180. */
    fun navagrahaLongitudes(jdUT: Double): LinkedHashMap<String, Double> {
        val map = LinkedHashMap<String, Double>()
        map["సూర్యుడు"] = planetLongitude(jdUT, SweConst.SE_SUN)
        map["చంద్రుడు"] = planetLongitude(jdUT, SweConst.SE_MOON)
        map["కుజుడు"] = planetLongitude(jdUT, SweConst.SE_MARS)
        map["బుధుడు"] = planetLongitude(jdUT, SweConst.SE_MERCURY)
        map["గురువు"] = planetLongitude(jdUT, SweConst.SE_JUPITER)
        map["శుక్రుడు"] = planetLongitude(jdUT, SweConst.SE_VENUS)
        map["శని"] = planetLongitude(jdUT, SweConst.SE_SATURN)
        val rahu = planetLongitude(jdUT, SweConst.SE_MEAN_NODE)
        map["రాహువు"] = rahu
        map["కేతువు"] = norm360(rahu + 180.0)
        return map
    }

    /** Sidereal Ascendant (Lagna), degrees - via Swiss Ephemeris house computation. */
    fun ascendantLongitude(jdUT: Double, lat: Double, lon: Double): Double {
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        val flags = SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_MOSEPH
        SwissEphManager.swe.swe_houses(jdUT, flags, lat, lon, 'P'.code, cusps, ascmc)
        return norm360(ascmc[0])
    }

    /**
     * Full sidereal house-cusp array (Placidus, index 1..12 = houses 1..12) -
     * used for the Bhava (house) chart. Index 0 is unused by the library.
     */
    fun houseCusps(jdUT: Double, lat: Double, lon: Double): DoubleArray {
        val cusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        val flags = SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_MOSEPH
        SwissEphManager.swe.swe_houses(jdUT, flags, lat, lon, 'P'.code, cusps, ascmc)
        for (i in cusps.indices) cusps[i] = norm360(cusps[i])
        return cusps
    }

    /**
     * Precise sunrise/sunset via Swiss Ephemeris' swe_rise_trans (accounts for
     * atmospheric refraction, solar disc radius, geographic position). Returns
     * minutes-after-local-midnight for the given date/place/timezone.
     */
    fun sunriseSunset(year: Int, month: Int, day: Int, lat: Double, lon: Double, tz: Double): Pair<Double, Double> {
        val swe = SwissEphManager.swe
        val jdLocalMidnightUT = julianDay(year, month, day, -tz)
        val geopos = doubleArrayOf(lon, lat, 0.0)
        val serr = StringBuffer()

        val riseObj = DblObj()
        val setObj = DblObj()
        swe.swe_rise_trans(
            jdLocalMidnightUT, SweConst.SE_SUN, StringBuffer(""), SweConst.SEFLG_MOSEPH,
            SweConst.SE_CALC_RISE, geopos, 1013.25, 15.0, riseObj, serr
        )
        swe.swe_rise_trans(
            jdLocalMidnightUT, SweConst.SE_SUN, StringBuffer(""), SweConst.SEFLG_MOSEPH,
            SweConst.SE_CALC_SET, geopos, 1013.25, 15.0, setObj, serr
        )

        // DblObj exposes a public Java field named "val" - escaped with backticks
        // here because `val` is a reserved keyword in Kotlin.
        val riseMinutes = (riseObj.`val` - jdLocalMidnightUT) * 1440.0
        val setMinutes = (setObj.`val` - jdLocalMidnightUT) * 1440.0
        return Pair(riseMinutes, setMinutes)
    }

    /** Weekday order used for Rahu kalam etc: index matches Calendar.DAY_OF_WEEK (1=Sunday). */
    private val rahuSegment = intArrayOf(-1, 8, 2, 7, 5, 6, 4, 3)
    private val yamaSegment = intArrayOf(-1, 5, 4, 3, 2, 1, 7, 6)
    private val gulikaSegment = intArrayOf(-1, 7, 6, 5, 4, 3, 2, 1)

    private fun segmentToTime(sunrise: Double, sunset: Double, segment: Int): Pair<Double, Double> {
        val dayLen = sunset - sunrise
        val part = dayLen / 8.0
        val start = sunrise + part * (segment - 1)
        val end = start + part
        return Pair(start, end)
    }

    /** Telugu name for the 60 Karana halves in one lunar month. */
    private fun karanaName(index: Int): String {
        val recurring = arrayOf("బవ", "బాలవ", "కౌలవ", "తైతిల", "గరజ", "వణిజ", "విష్టి")
        return when (index) {
            0 -> "కింస్తుఘ్న"
            in 1..56 -> recurring[(index - 1) % 7]
            57 -> "శకుని"
            58 -> "చతుష్పాద"
            59 -> "నాగ"
            else -> ""
        }
    }

    fun compute(
        year: Int, month: Int, day: Int,
        lat: Double, lon: Double, tz: Double
    ): PanchangResult {
        // A daily Panchanga is defined from the local sunrise.  Therefore all
        // tithi/nakshatra/yoga/karana names and their next endings are derived
        // from the exact sunrise instant, not from local noon.
        val (sunrise, sunset) = sunriseSunset(year, month, day, lat, lon, tz)
        val localMidnightJd = julianDay(year, month, day, -tz)
        val sunriseJd = localMidnightJd + sunrise / 1440.0

        val sunLong = sunLongitude(sunriseJd)
        val moonLong = moonLongitude(sunriseJd)
        val diff = norm360(moonLong - sunLong)

        val tithiZero = floor(diff / 12.0).toInt().coerceIn(0, 29)
        val tithiIndex = tithiZero + 1
        val paksham = if (tithiIndex <= 15) "శుక్ల పక్షం" else "కృష్ణ పక్షం"
        val tithiName = PanchangNames.tithiName(tithiIndex)

        val nakIndex = floor(moonLong / (360.0 / 27.0)).toInt().coerceIn(0, 26)
        val nakshatraName = PanchangNames.nakshatraNames[nakIndex]

        val yogaSum = norm360(sunLong + moonLong)
        val yogaIndex = floor(yogaSum / (360.0 / 27.0)).toInt().coerceIn(0, 26)
        val yogaName = PanchangNames.yogaNames[yogaIndex]

        val karanaNum = floor(diff / 6.0).toInt().coerceIn(0, 59)
        val karanaName = karanaName(karanaNum)

        val cal = GregorianCalendar(year, month - 1, day)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val varaName = PanchangNames.varaNames[dow - 1]

        val sunRashiIndex = floor(sunLong / 30.0).toInt().coerceIn(0, 11)
        val masaName = PanchangNames.masaNames[sunRashiIndex]
        val sunRashi = PanchangNames.rashiNames[sunRashiIndex]
        val moonRashiIndex = floor(moonLong / 30.0).toInt().coerceIn(0, 11)
        val moonRashi = PanchangNames.rashiNames[moonRashiIndex]

        val rahu = segmentToTime(sunrise, sunset, rahuSegment[dow])
        val yama = segmentToTime(sunrise, sunset, yamaSegment[dow])
        val gulika = segmentToTime(sunrise, sunset, gulikaSegment[dow])

        val tithiEndJd = findBoundary(sunriseJd, ::tithiIndexAt, tithiZero, true)
        val nakEndJd = findBoundary(sunriseJd, ::nakshatraIndexAt, nakIndex, true)
        val yogaEndJd = findBoundary(sunriseJd, ::yogaIndexAt, yogaIndex, true)
        val karEndJd = findBoundary(sunriseJd, ::karanaIndexAt, karanaNum, true)

        return PanchangResult(
            tithiIndex=tithiIndex, tithiName=tithiName, paksham=paksham,
            nakshatraName=nakshatraName, yogaName=yogaName, karanaName=karanaName,
            varaName=varaName, masaName=masaName, sunriseMinutes=sunrise, sunsetMinutes=sunset,
            rahuKalam=rahu, yamagandam=yama, gulikaKalam=gulika,
            moonRashi=moonRashi, sunRashi=sunRashi,
            tithiEndMinutes=(tithiEndJd-localMidnightJd)*1440.0,
            nakshatraEndMinutes=(nakEndJd-localMidnightJd)*1440.0,
            yogaEndMinutes=(yogaEndJd-localMidnightJd)*1440.0,
            karanaEndMinutes=(karEndJd-localMidnightJd)*1440.0,
            varjyamWindows=calculateVarjyamWindows(localMidnightJd,sunriseJd),
            durmuhurthamWindows=calculateDurmuhurthamWindows(sunrise,sunset,dow)
        )
    }

    private fun tithiIndexAt(jd: Double): Int {
        val diff = norm360(moonLongitude(jd) - sunLongitude(jd))
        return floor(diff / 12.0).toInt()
    }

    private fun nakshatraIndexAt(jd: Double): Int {
        return floor(moonLongitude(jd) / (360.0 / 27.0)).toInt().coerceIn(0, 26)
    }

    private fun yogaIndexAt(jd: Double): Int {
        val sum = norm360(moonLongitude(jd) + sunLongitude(jd))
        return floor(sum / (360.0 / 27.0)).toInt().coerceIn(0, 26)
    }

    private fun karanaIndexAt(jd: Double): Int {
        val diff = norm360(moonLongitude(jd) - sunLongitude(jd))
        return floor(diff / 6.0).toInt().coerceIn(0, 59)
    }

    private fun findBoundary(
        jd: Double,
        classifier: (Double) -> Int,
        current: Int,
        forward: Boolean
    ): Double {
        val step = 1.0 / 24.0
        var a = jd
        var b = jd

        repeat(72) {
            b = if (forward) b + step else b - step
            if (classifier(b) != current) {
                var lo = min(a, b)
                var hi = max(a, b)
                repeat(32) {
                    val mid = (lo + hi) / 2.0
                    if (classifier(mid) == current) {
                        if (forward) lo = mid else hi = mid
                    } else {
                        if (forward) hi = mid else lo = mid
                    }
                }
                return if (forward) hi else lo
            }
            a = b
        }
        return b
    }

    private val nakTyajya = intArrayOf(
        50,24,30,40,14,21,30,20,32,30,20,18,21,20,14,14,10,14,56,24,20,10,10,18,16,24,30
    )

    private fun calculateVarjyamWindows(
        localMidnightJd: Double,
        sunriseJd: Double
    ): List<Pair<Double, Double>> {
        val result = mutableListOf<Pair<Double, Double>>()
        val seen = mutableSetOf<Int>()
        var probe = localMidnightJd + 0.1

        while (probe < sunriseJd + 1.25) {
            val nak = nakshatraIndexAt(probe)
            if (seen.add(nak)) {
                val start = findBoundary(
                    probe, { x -> nakshatraIndexAt(x) }, nak, false
                )
                val end = findBoundary(
                    probe, { x -> nakshatraIndexAt(x) }, nak, true
                )
                val duration = end - start
                val vStart = start + duration * nakTyajya[nak] / 60.0
                val vEnd = vStart + duration * 4.0 / 60.0

                val nextSunrise = sunriseJd + 1.0
                val s = max(vStart, sunriseJd)
                val e = min(vEnd, nextSunrise)
                if (e > s) {
                    result.add(
                        Pair(
                            (s - localMidnightJd) * 1440.0,
                            (e - localMidnightJd) * 1440.0
                        )
                    )
                }
            }
            probe += 0.6
        }
        return result
    }

    private val durmuhurtaParts = mapOf(
        Calendar.SUNDAY to listOf(9, 12),
        Calendar.MONDAY to listOf(4, 7),
        Calendar.TUESDAY to listOf(8),
        Calendar.WEDNESDAY to listOf(6, 12),
        Calendar.THURSDAY to listOf(4, 9),
        Calendar.FRIDAY to listOf(1, 2),
        Calendar.SATURDAY to listOf(14)
    )

    private fun calculateDurmuhurthamWindows(
        sunrise: Double,
        sunset: Double,
        dow: Int
    ): List<Pair<Double, Double>> {
        val unit = (sunset - sunrise) / 15.0
        return durmuhurtaParts[dow].orEmpty().map { part ->
            Pair(
                sunrise + (part - 1) * unit,
                sunrise + part * unit
            )
        }
    }

    // ---------------- Vimshottari Dasha ----------------

    private val dashaLords = listOf(
        "కేతువు" to 7.0, "శుక్రుడు" to 20.0, "సూర్యుడు" to 6.0, "చంద్రుడు" to 10.0,
        "కుజుడు" to 7.0, "రాహువు" to 18.0, "గురువు" to 16.0, "శని" to 19.0, "బుధుడు" to 17.0
    ) // total = 120 years

    /**
     * Vimshottari Mahadasha timeline starting from the Moon's nakshatra position
     * at the given moment (typically birth). Returns 9 sequential periods
     * (one full 120-year cycle), each with years-from-reference start/end.
     */
    fun vimshottariDasha(moonLongitude: Double): List<DashaPeriod> {
        val nakSpan = 360.0 / 27.0
        val nakIndex = floor(moonLongitude / nakSpan).toInt().coerceIn(0, 26)
        val startLordIdx = nakIndex % 9
        val posInNak = moonLongitude % nakSpan
        val fractionElapsed = posInNak / nakSpan

        val periods = mutableListOf<DashaPeriod>()
        var cursor = 0.0

        // First (partial) dasha: remaining balance of the starting lord
        val firstDuration = dashaLords[startLordIdx].second
        val firstBalance = firstDuration * (1.0 - fractionElapsed)
        periods.add(DashaPeriod(dashaLords[startLordIdx].first, cursor, cursor + firstBalance))
        cursor += firstBalance

        // Remaining 8 full mahadashas in cyclic order
        for (i in 1..8) {
            val idx = (startLordIdx + i) % 9
            val duration = dashaLords[idx].second
            periods.add(DashaPeriod(dashaLords[idx].first, cursor, cursor + duration))
            cursor += duration
        }
        return periods
    }

    /**
     * Hierarchical Vimshottari tree: Mahadasha → Antardasha → Pratyantardasha
     * → Sookshma → Prana (5 levels).
     */
    fun vimshottariDashaTree(
        moonLongitude: Double,
        birthJd: Double,
        maxLevel: Int = 5
    ): List<VimshottariDashaNode> {
        val nakSpan = 360.0 / 27.0
        val nakIndex = floor(moonLongitude / nakSpan).toInt().coerceIn(0, 26)
        val startLordIdx = nakIndex % 9
        val posInNak = moonLongitude % nakSpan
        val fractionElapsed = posInNak / nakSpan

        val yearDays = 365.2425
        val firstLordYears = dashaLords[startLordIdx].second
        val elapsedYears = firstLordYears * fractionElapsed
        val cycleStartJd = birthJd - elapsedYears * yearDays

        val roots = mutableListOf<VimshottariDashaNode>()
        var cursor = cycleStartJd

        for (i in 0 until 9) {
            val lordIdx = (startLordIdx + i) % 9
            val end = cursor + dashaLords[lordIdx].second * yearDays

            if (end > birthJd) {
                roots += buildDashaNode(
                    level = 1,
                    lordIdx = lordIdx,
                    theoreticalStartJd = cursor,
                    theoreticalEndJd = end,
                    visibleFromJd = max(cursor, birthJd),
                    maxLevel = maxLevel,
                    birthJd = birthJd
                )
            }
            cursor = end
        }
        return roots
    }

    private fun buildDashaNode(
        level: Int,
        lordIdx: Int,
        theoreticalStartJd: Double,
        theoreticalEndJd: Double,
        visibleFromJd: Double,
        maxLevel: Int,
        birthJd: Double
    ): VimshottariDashaNode {
        val children =
            if (level < maxLevel) {
                buildDashaChildren(
                    parentLordIdx = lordIdx,
                    parentStartJd = theoreticalStartJd,
                    parentEndJd = theoreticalEndJd,
                    level = level + 1,
                    maxLevel = maxLevel,
                    birthJd = birthJd
                )
            } else emptyList()

        return VimshottariDashaNode(
            level = level,
            lordTelugu = dashaLords[lordIdx].first,
            startJd = max(theoreticalStartJd, visibleFromJd),
            endJd = theoreticalEndJd,
            children = children
        )
    }

    private fun buildDashaChildren(
        parentLordIdx: Int,
        parentStartJd: Double,
        parentEndJd: Double,
        level: Int,
        maxLevel: Int,
        birthJd: Double
    ): List<VimshottariDashaNode> {
        val result = mutableListOf<VimshottariDashaNode>()
        val parentSpan = parentEndJd - parentStartJd
        var cursor = parentStartJd

        for (i in 0 until 9) {
            val childLordIdx = (parentLordIdx + i) % 9
            val childSpan = parentSpan * (dashaLords[childLordIdx].second / 120.0)
            val end = cursor + childSpan

            if (end > birthJd) {
                result += buildDashaNode(
                    level = level,
                    lordIdx = childLordIdx,
                    theoreticalStartJd = cursor,
                    theoreticalEndJd = end,
                    visibleFromJd = max(cursor, birthJd),
                    maxLevel = maxLevel,
                    birthJd = birthJd
                )
            }
            cursor = end
        }
        return result
    }

    fun formatJdLocal(jd: Double, tz: Double = 5.5): String {
        val utcMillis = ((jd - 2440587.5) * 86400000.0).roundToLong()
        val localMillis = utcMillis + (tz * 3600000.0).roundToLong()
        val sdf = java.text.SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(localMillis))
    }

    fun minutesToHHMM(minutes: Double): String {
        var m = minutes.roundToInt()
        val day = Math.floorDiv(m, 1440)
        m = Math.floorMod(m, 1440)
        val h = m / 60
        val min = m % 60
        return String.format("%02d:%02d", h, min) + if (day != 0) " (+$day)" else ""
    }

    /**
     * Sripati Bhava cusps.
     *
     * The four quadrant points are Ascendant, IC, Descendant and MC.
     * Each quadrant is divided into three equal arcs.
     *
     * Returned array uses index 1..12 for houses; index 0 is unused.
     */
    fun sripatiHouseCusps(jdUT: Double, lat: Double, lon: Double): DoubleArray {
        val cuspsFromSwiss = DoubleArray(13)
        val ascmc = DoubleArray(10)
        val flags = SweConst.SEFLG_SIDEREAL or SweConst.SEFLG_MOSEPH

        SwissEphManager.swe.swe_houses(
            jdUT,
            flags,
            lat,
            lon,
            'P'.code,
            cuspsFromSwiss,
            ascmc
        )

        val asc = norm360(ascmc[0])
        val mc = norm360(ascmc[1])
        val dsc = norm360(asc + 180.0)
        val ic = norm360(mc + 180.0)

        val cusps = DoubleArray(13)

        cusps[1] = asc
        cusps[4] = ic
        cusps[7] = dsc
        cusps[10] = mc

        val q14 = forwardArc(asc, ic)
        val q47 = forwardArc(ic, dsc)
        val q710 = forwardArc(dsc, mc)
        val q101 = forwardArc(mc, asc)

        cusps[2] = norm360(asc + q14 / 3.0)
        cusps[3] = norm360(asc + q14 * 2.0 / 3.0)

        cusps[5] = norm360(ic + q47 / 3.0)
        cusps[6] = norm360(ic + q47 * 2.0 / 3.0)

        cusps[8] = norm360(dsc + q710 / 3.0)
        cusps[9] = norm360(dsc + q710 * 2.0 / 3.0)

        cusps[11] = norm360(mc + q101 / 3.0)
        cusps[12] = norm360(mc + q101 * 2.0 / 3.0)

        return cusps
    }

    /**
     * Returns the Sripati house number 1..12 for a planetary longitude.
     */
    fun sripatiHouseNumber(planetLongitude: Double, cusps: DoubleArray): Int {
        if (cusps.size < 13) return 1

        val p = norm360(planetLongitude)

        for (house in 1..12) {
            val start = cusps[house]
            val end = if (house == 12) cusps[1] else cusps[house + 1]

            val span = forwardArc(start, end)
            val pos = forwardArc(start, p)

            if (pos < span || abs(pos - span) < 1e-8) {
                return house
            }
        }

        return 1
    }

    private fun forwardArc(start: Double, end: Double): Double {
        var d = norm360(end) - norm360(start)
        if (d < 0) d += 360.0
        return d
    }

}
