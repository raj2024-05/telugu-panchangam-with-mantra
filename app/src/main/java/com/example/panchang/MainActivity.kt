package com.example.panchang

import android.os.Bundle
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("panchang_prefs", MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 17.3850f).toDouble()   // default Hyderabad
        val lon = prefs.getFloat("lon", 78.4867f).toDouble()
        val tz = 5.5

        val now = Calendar.getInstance()
        val result = PanchangCalculator.compute(
            now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
            lat, lon, tz
        )

        findViewById<TextView>(R.id.tvDate).text =
            DateFormat.format("hh:mm a, dd MMM yyyy", now).toString()
        findViewById<TextView>(R.id.tvTithiPaksham).text =
            "${result.tithiName}, ${result.paksham}, ${result.masaName}"
        findViewById<TextView>(R.id.tvNakshatram).text =
            "${result.nakshatraName}, ${result.varaName}"
        findViewById<TextView>(R.id.tvRahukalam).text =
            "రాహు కాళం ${PanchangCalculator.minutesToHHMM(result.rahuKalam.first)} - " +
            PanchangCalculator.minutesToHHMM(result.rahuKalam.second)

        val features = listOf(
            FeatureItem("📅", getString(R.string.feat_telugu_panchangam), getString(R.string.feat_telugu_panchangam_desc), CalendarMonthActivity::class.java),
            FeatureItem("📜", getString(R.string.feat_rojuvari_panchangam), getString(R.string.feat_rojuvari_panchangam_desc), PanchangDetailActivity::class.java),
            FeatureItem("🎉", getString(R.string.feat_utsavamulu), getString(R.string.feat_utsavamulu_desc), FestivalsActivity::class.java),
            FeatureItem("🕉️", getString(R.string.feat_muhurtham), getString(R.string.feat_muhurtham_desc), MuhurthamActivity::class.java),
            FeatureItem("🔯", getString(R.string.feat_kundali), getString(R.string.feat_kundali_desc), KundaliActivity::class.java),
            FeatureItem("✨", getString(R.string.feat_jyotisham), getString(R.string.feat_jyotisham_desc), KundaliActivity::class.java),
            FeatureItem("🔱", getString(R.string.feat_shodasha_varga), getString(R.string.feat_shodasha_varga_desc), ShodashaVargaActivity::class.java),
            FeatureItem("🍩", getString(R.string.feat_rashiphalalu), getString(R.string.feat_rashiphalalu_desc), RashiPhalaluActivity::class.java),
            FeatureItem("🪔", getString(R.string.feat_bhakti), getString(R.string.feat_bhakti_desc), BhaktiActivity::class.java),
            FeatureItem("🛕", getString(R.string.feat_vrata), getString(R.string.feat_vrata_desc), VratActivity::class.java),
            FeatureItem("➕", getString(R.string.feat_add_tithi), getString(R.string.feat_add_tithi_desc), AddTithiActivity::class.java),
            FeatureItem("⏳", getString(R.string.feat_vedic_time), getString(R.string.feat_vedic_time_desc), PanchangDetailActivity::class.java),
            FeatureItem("⚙️", getString(R.string.feat_settings), getString(R.string.feat_settings_desc), SettingsActivity::class.java),
            FeatureItem("🗓️", getString(R.string.feat_regional), getString(R.string.feat_regional_desc), FestivalsActivity::class.java),
            FeatureItem("ℹ️", getString(R.string.feat_about), getString(R.string.feat_about_desc), AboutActivity::class.java)
        )

        val rv = findViewById<RecyclerView>(R.id.rvFeatures)
        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = FeatureAdapter(features)
    }
}
