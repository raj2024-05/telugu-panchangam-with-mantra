package com.example.panchang

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val prefs = getSharedPreferences("panchang_prefs", MODE_PRIVATE)
        val etLat = findViewById<EditText>(R.id.etLat)
        val etLon = findViewById<EditText>(R.id.etLon)
        etLat.setText(prefs.getFloat("lat", 17.3850f).toString())
        etLon.setText(prefs.getFloat("lon", 78.4867f).toString())

        findViewById<android.widget.Button>(R.id.btnSave).setOnClickListener {
            val lat = etLat.text.toString().toFloatOrNull()
            val lon = etLon.text.toString().toFloatOrNull()
            if (lat == null || lon == null) {
                Toast.makeText(this, "సరైన సంఖ్యలను నమోదు చేయండి", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            prefs.edit().putFloat("lat", lat).putFloat("lon", lon).apply()
            Toast.makeText(this, "సేవ్ చేయబడింది", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
