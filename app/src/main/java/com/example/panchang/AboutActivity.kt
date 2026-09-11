package com.example.panchang

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        val tb = findViewById<Toolbar>(R.id.toolbar)
        tb.title = getString(R.string.feat_about)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val info = """
            పంచాంగం అంటే ఐదు అంగాలు: తిథి, వారం, నక్షత్రం, యోగం, కరణం.
            ఇవి హిందూ కాలగణనలో రోజువారీ శుభాశుభాలను నిర్ణయించడానికి ఉపయోగిస్తారు.

            • తిథి: సూర్య-చంద్రుల మధ్య కోణీయ దూరం ఆధారంగా (12° కి ఒకటి) లెక్కించబడుతుంది.
            • నక్షత్రం: చంద్రుడు ఉన్న 27 నక్షత్రాలలో ఒకటి.
            • యోగం: సూర్య-చంద్రుల మొత్తం రేఖాంశం ఆధారంగా 27 యోగాలు.
            • కరణం: తిథిలో సగభాగం.
            • చౌఘడియ: పగలు/రాత్రిని 8 భాగాలుగా విభజించి శుభాశుభ కాలాలను తెలిపేది.
            • రాహుకాలం, యమగండం, గుళికకాలం: వారాన్ని బట్టి మారే అశుభ సమయాలు.

            గమనిక: ఈ యాప్‌లోని లెక్కలు ఖగోళశాస్త్ర సూత్రాల ఆధారంగా అంచనా వేయబడ్డాయి.
            అత్యంత కచ్చితమైన ఫలితాల కోసం ప్రామాణిక పంచాంగంతో సరిపోల్చుకోగలరు.
        """.trimIndent()

        val tv = TextView(this).apply {
            text = info
            textSize = 15f
            setLineSpacing(8f, 1.1f)
            setTextColor(resources.getColor(R.color.black, theme))
        }
        findViewById<LinearLayout>(R.id.container).addView(tv)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
