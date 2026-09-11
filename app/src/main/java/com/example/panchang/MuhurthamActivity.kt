package com.example.panchang

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.util.*

class MuhurthamActivity : AppCompatActivity() {

    private val choghadiyaTelugu = mapOf(
        "Udveg" to "ఉద్వేగ్ (అశుభం)", "Chal" to "చల (మిశ్రమం)", "Labh" to "లాభ (శుభం)",
        "Amrit" to "అమృత (శుభం)", "Kaal" to "కాల (అశుభం)", "Shubh" to "శుభ (శుభం)",
        "Rog" to "రోగ్ (అశుభం)"
    )

    private val dayTable = mapOf(
        1 to listOf("Udveg","Chal","Labh","Amrit","Kaal","Shubh","Rog","Udveg"),
        2 to listOf("Amrit","Kaal","Shubh","Rog","Udveg","Chal","Labh","Amrit"),
        3 to listOf("Rog","Udveg","Chal","Labh","Amrit","Kaal","Shubh","Rog"),
        4 to listOf("Labh","Amrit","Kaal","Shubh","Rog","Udveg","Chal","Labh"),
        5 to listOf("Shubh","Rog","Udveg","Chal","Labh","Amrit","Kaal","Shubh"),
        6 to listOf("Chal","Labh","Amrit","Kaal","Shubh","Rog","Udveg","Chal"),
        7 to listOf("Kaal","Shubh","Rog","Udveg","Chal","Labh","Amrit","Kaal")
    )
    private val nightTable = mapOf(
        1 to listOf("Shubh","Amrit","Chal","Rog","Kaal","Labh","Udveg","Shubh"),
        2 to listOf("Chal","Rog","Kaal","Labh","Udveg","Shubh","Amrit","Chal"),
        3 to listOf("Kaal","Labh","Udveg","Shubh","Amrit","Chal","Rog","Kaal"),
        4 to listOf("Udveg","Shubh","Amrit","Chal","Rog","Kaal","Labh","Udveg"),
        5 to listOf("Amrit","Chal","Rog","Kaal","Labh","Udveg","Shubh","Amrit"),
        6 to listOf("Rog","Kaal","Labh","Udveg","Shubh","Amrit","Chal","Rog"),
        7 to listOf("Labh","Udveg","Shubh","Amrit","Chal","Rog","Kaal","Labh")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        val tb = findViewById<Toolbar>(R.id.toolbar)
        tb.title = getString(R.string.feat_muhurtham)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = getSharedPreferences("panchang_prefs", MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 17.3850f).toDouble()
        val lon = prefs.getFloat("lon", 78.4867f).toDouble()
        val tz = 5.5

        val cal = Calendar.getInstance()
        val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sunday
        val (sunrise, sunset) = PanchangCalculator.sunriseSunset(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), lat, lon, tz
        )
        // approximate next sunrise as sunset + (sunrise duration of today), i.e. 24h - daylen
        val dayLen = sunset - sunrise
        val nightLen = 1440 - dayLen

        val container = findViewById<LinearLayout>(R.id.container)

        fun addHeader(text: String) {
            val tv = TextView(this).apply {
                this.text = text
                textSize = 16f
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextColor(resources.getColor(R.color.text_red, theme))
                setPadding(8, 24, 8, 12)
            }
            container.addView(tv)
        }
        fun addRow(range: String, name: String) {
            val tv = TextView(this).apply {
                text = "$range   →   $name"
                textSize = 14f
                setTextColor(resources.getColor(R.color.black, theme))
                setBackgroundColor(resources.getColor(R.color.gold_card, theme))
                setPadding(20, 18, 20, 18)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 2
                layoutParams = lp
            }
            container.addView(tv)
        }

        // Abhijit Muhurtam - roughly 24 minutes centered at solar noon
        val solarNoon = (sunrise + sunset) / 2.0
        addHeader(getString(R.string.label_abhijit))
        addRow(
            "${PanchangCalculator.minutesToHHMM(solarNoon - 12)} - ${PanchangCalculator.minutesToHHMM(solarNoon + 12)}",
            "శుభం"
        )

        addHeader("పగటి చౌఘడియలు (Choghadiya)")
        val dayPart = dayLen / 8.0
        dayTable[dow]?.forEachIndexed { i, key ->
            val start = sunrise + dayPart * i
            val end = start + dayPart
            addRow(
                "${PanchangCalculator.minutesToHHMM(start)} - ${PanchangCalculator.minutesToHHMM(end)}",
                choghadiyaTelugu[key] ?: key
            )
        }

        addHeader("రాత్రి చౌఘడియలు (Choghadiya)")
        val nightPart = nightLen / 8.0
        nightTable[dow]?.forEachIndexed { i, key ->
            val start = sunset + nightPart * i
            val end = start + nightPart
            addRow(
                "${PanchangCalculator.minutesToHHMM(start)} - ${PanchangCalculator.minutesToHHMM(end)}",
                choghadiyaTelugu[key] ?: key
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
