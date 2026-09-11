package com.example.panchang

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlin.math.floor

/**
 * Displays any one of the 16 classical Shodasha Varga divisional charts
 * (D1..D60), plus the Rashi (D1) and Bhava (house) charts, for a given
 * birth date/time/place. Selection is via the dropdown (Spinner) - this
 * covers "రాశి, భావ, నవాంశ మరియు 16 షోడశ చక్రాలు" in one screen.
 */
class ShodashaVargaActivity : AppCompatActivity() {

    private val chartOptions = listOf(VargaChart.BHAVA) + VargaChart.values().filter { it != VargaChart.BHAVA }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shodasha_varga)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val spinner = findViewById<Spinner>(R.id.spinnerChart)
        val labels = chartOptions.map { it.teluguName }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)

        val etYear = findViewById<EditText>(R.id.etYear)
        val etMonth = findViewById<EditText>(R.id.etMonth)
        val etDay = findViewById<EditText>(R.id.etDay)
        val etHour = findViewById<EditText>(R.id.etHour)
        val etMinute = findViewById<EditText>(R.id.etMinute)
        val etLat = findViewById<EditText>(R.id.etLat)
        val etLon = findViewById<EditText>(R.id.etLon)
        val resultContainer = findViewById<LinearLayout>(R.id.resultContainer)

        findViewById<android.widget.Button>(R.id.btnCompute).setOnClickListener {
            try {
                val year = etYear.text.toString().toInt()
                val month = etMonth.text.toString().toInt()
                val day = etDay.text.toString().toInt()
                val hour = etHour.text.toString().toIntOrNull() ?: 0
                val minute = etMinute.text.toString().toIntOrNull() ?: 0
                val lat = etLat.text.toString().toDoubleOrNull() ?: 17.385
                val lon = etLon.text.toString().toDoubleOrNull() ?: 78.4867
                val tz = 5.5

                val hourUTC = (hour + minute / 60.0) - tz
                val jd = PanchangCalculator.julianDay(year, month, day, hourUTC)

                val grahas = PanchangCalculator.navagrahaLongitudes(jd)
                val ascLong = PanchangCalculator.ascendantLongitude(jd, lat, lon)
                val selectedChart = chartOptions[spinner.selectedItemPosition]

                resultContainer.removeAllViews()

                fun addHeader(text: String) {
                    val tv = TextView(this).apply {
                        this.text = text
                        textSize = 16f
                        setTypeface(typeface, android.graphics.Typeface.BOLD)
                        setTextColor(resources.getColor(R.color.text_red, theme))
                        setPadding(4, 16, 4, 8)
                    }
                    resultContainer.addView(tv)
                }
                fun addLine(label: String, value: String) {
                    val tv = TextView(this).apply {
                        text = "$label:  $value"
                        textSize = 14f
                        setTextColor(resources.getColor(R.color.black, theme))
                        setBackgroundColor(resources.getColor(R.color.gold_card, theme))
                        setPadding(20, 16, 20, 16)
                        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        lp.bottomMargin = 3
                        layoutParams = lp
                    }
                    resultContainer.addView(tv)
                }

                addHeader(selectedChart.teluguName)

                if (selectedChart == VargaChart.BHAVA) {
                    val cusps = PanchangCalculator.houseCusps(jd, lat, lon)
                    addLine("లగ్నం", "1వ భావం (Lagna)")
                    for ((graha, longitude) in grahas) {
                        val house = ShodashaVargaCalculator.bhavaHouse(longitude, cusps)
                        addLine(graha, "${house}వ భావం")
                    }
                } else {
                    val ascSignIdx = ShodashaVargaCalculator.signForVarga(selectedChart, ascLong)
                    addLine("లగ్నం", PanchangNames.rashiNames[ascSignIdx])
                    for ((graha, longitude) in grahas) {
                        val signIdx = ShodashaVargaCalculator.signForVarga(selectedChart, longitude)
                        addLine(graha, PanchangNames.rashiNames[signIdx])
                    }
                }

                addHeader("గమనిక")
                addLine("ఆధారం", "సైడీరియల్ రేఖాంశాలు (Swiss Ephemeris, లాహిరి అయనాంశ) ఆధారంగా")

            } catch (e: Exception) {
                resultContainer.removeAllViews()
                val tv = TextView(this).apply {
                    text = "దయచేసి అన్ని వివరాలను సరిగ్గా నమోదు చేయండి. (${e.message ?: ""})"
                    setTextColor(resources.getColor(R.color.text_red, theme))
                }
                resultContainer.addView(tv)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
