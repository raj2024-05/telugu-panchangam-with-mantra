package com.example.panchang

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.util.*

class PanchangDetailActivity : AppCompatActivity() {

    private var dayOffset = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panchang_detail)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        render()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }

    private fun render() {
        val container = findViewById<android.widget.LinearLayout>(R.id.container)
        container.removeAllViews()

        val prefs = getSharedPreferences("panchang_prefs", MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 17.3850f).toDouble()
        val lon = prefs.getFloat("lon", 78.4867f).toDouble()
        val tz = 5.5

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, dayOffset)

        val result = PanchangCalculator.compute(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
            lat, lon, tz
        )

        val dateTv = TextView(this).apply {
            text = DateFormat.format("EEEE, dd MMMM yyyy", cal).toString()
            textSize = 17f
            setTextColor(resources.getColor(R.color.text_red, theme))
            setPadding(8, 8, 8, 20)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        container.addView(dateTv)

        fun addRow(label: String, value: String) {
            val row = LayoutInflater.from(this).inflate(R.layout.row_label_value, container, false)
            row.findViewById<TextView>(R.id.tvLabel).text = label
            row.findViewById<TextView>(R.id.tvValue).text = value
            container.addView(row)
        }

        addRow(getString(R.string.label_tithi), "${result.tithiName} (${result.paksham})  →  ${PanchangCalculator.minutesToHHMM(result.tithiEndMinutes)} వరకు")
        addRow(getString(R.string.label_nakshatram), "${result.nakshatraName}  →  ${PanchangCalculator.minutesToHHMM(result.nakshatraEndMinutes)} వరకు")
        addRow(getString(R.string.label_yogam), "${result.yogaName}  →  ${PanchangCalculator.minutesToHHMM(result.yogaEndMinutes)} వరకు")
        addRow(getString(R.string.label_karanam), "${result.karanaName}  →  ${PanchangCalculator.minutesToHHMM(result.karanaEndMinutes)} వరకు")
        addRow(getString(R.string.label_vaaram), result.varaName)
        addRow(getString(R.string.label_masam), result.masaName)
        addRow(getString(R.string.label_sunrise), PanchangCalculator.minutesToHHMM(result.sunriseMinutes))
        addRow(getString(R.string.label_sunset), PanchangCalculator.minutesToHHMM(result.sunsetMinutes))
        addRow(getString(R.string.label_rahukalam),
            "${PanchangCalculator.minutesToHHMM(result.rahuKalam.first)} - ${PanchangCalculator.minutesToHHMM(result.rahuKalam.second)}")
        addRow(getString(R.string.label_yamagandam),
            "${PanchangCalculator.minutesToHHMM(result.yamagandam.first)} - ${PanchangCalculator.minutesToHHMM(result.yamagandam.second)}")
        addRow(getString(R.string.label_gulikakalam),
            "${PanchangCalculator.minutesToHHMM(result.gulikaKalam.first)} - ${PanchangCalculator.minutesToHHMM(result.gulikaKalam.second)}")
        val varjyam = result.varjyamWindows.ifEmpty { emptyList() }
        addRow("వర్జ్యం", if (varjyam.isEmpty()) "లేదు" else varjyam.joinToString("\n") { "${PanchangCalculator.minutesToHHMM(it.first)} - ${PanchangCalculator.minutesToHHMM(it.second)}" })
        val durmuhurtham = result.durmuhurthamWindows.ifEmpty { emptyList() }
        addRow(getString(R.string.label_durmuhurtam), if (durmuhurtham.isEmpty()) "లేదు" else durmuhurtham.joinToString("\n") { "${PanchangCalculator.minutesToHHMM(it.first)} - ${PanchangCalculator.minutesToHHMM(it.second)}" })
        addRow("సూర్య రాశి", result.sunRashi)
        addRow("చంద్ర రాశి", result.moonRashi)

        val navRow = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 0)
        }
        val prevBtn = android.widget.Button(this).apply {
            text = "◀ మునుపటి రోజు"
            setOnClickListener { dayOffset -= 1; render() }
        }
        val nextBtn = android.widget.Button(this).apply {
            text = "తర్వాతి రోజు ▶"
            setOnClickListener { dayOffset += 1; render() }
        }
        val lp = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        navRow.addView(prevBtn, lp)
        navRow.addView(nextBtn, lp)
        container.addView(navRow)
    }
}
