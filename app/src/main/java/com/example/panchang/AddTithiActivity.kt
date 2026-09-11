package com.example.panchang

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * Lets the user save a personal tithi (e.g. birthday, anniversary) tied to a
 * Gregorian date. Stored locally as simple delimited strings in SharedPreferences.
 */
class AddTithiActivity : AppCompatActivity() {

    private val PREF = "custom_tithi_prefs"
    private val KEY = "entries"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_tithi)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etYear = findViewById<EditText>(R.id.etYear)
        val etMonth = findViewById<EditText>(R.id.etMonth)
        val etDay = findViewById<EditText>(R.id.etDay)

        findViewById<android.widget.Button>(R.id.btnAdd).setOnClickListener {
            val title = etTitle.text.toString().trim()
            val y = etYear.text.toString().toIntOrNull()
            val m = etMonth.text.toString().toIntOrNull()
            val d = etDay.text.toString().toIntOrNull()
            if (title.isEmpty() || y == null || m == null || d == null) {
                Toast.makeText(this, "అన్ని వివరాలను నమోదు చేయండి", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val prefs = getSharedPreferences(PREF, MODE_PRIVATE)
            val existing = prefs.getStringSet(KEY, mutableSetOf())!!.toMutableSet()
            existing.add("$title||$y-$m-$d")
            prefs.edit().putStringSet(KEY, existing).apply()

            etTitle.text.clear(); etYear.text.clear(); etMonth.text.clear(); etDay.text.clear()
            Toast.makeText(this, "జోడించబడింది", Toast.LENGTH_SHORT).show()
            renderList()
        }

        renderList()
    }

    private fun renderList() {
        val container = findViewById<LinearLayout>(R.id.savedContainer)
        container.removeAllViews()
        val prefs = getSharedPreferences(PREF, MODE_PRIVATE)
        val entries = prefs.getStringSet(KEY, mutableSetOf())!!
        if (entries.isEmpty()) {
            container.addView(TextView(this).apply { text = "ఇంకా తిథులు జోడించలేదు." })
            return
        }
        for (entry in entries) {
            val parts = entry.split("||")
            val tv = TextView(this).apply {
                text = "🔸 ${parts.getOrElse(0){"?"}}  —  ${parts.getOrElse(1){"?"}}"
                textSize = 14f
                setBackgroundColor(resources.getColor(R.color.gold_card, theme))
                setPadding(20, 16, 20, 16)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 4
                layoutParams = lp
            }
            container.addView(tv)
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
