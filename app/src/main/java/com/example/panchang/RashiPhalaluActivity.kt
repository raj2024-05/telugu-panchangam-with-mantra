package com.example.panchang

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class RashiPhalaluActivity : AppCompatActivity() {

    // Generic placeholder guidance text per rashi - replace with a live API for production use.
    private val phalalu = mapOf(
        "మేషం" to "నేటి రోజు వృత్తిపరంగా మంచి అవకాశాలు కలిగి ఉంటుంది. ఆరోగ్యం పట్ల జాగ్రత్త వహించండి.",
        "వృషభం" to "ఆర్థిక పరిస్థితులు మెరుగవుతాయి. కుటుంబ సభ్యులతో సమయం గడపండి.",
        "మిథునం" to "కొత్త ప్రణాళికలు మొదలుపెట్టడానికి అనుకూల సమయం. ప్రయాణాలు ఫలప్రదం.",
        "కర్కాటకం" to "మానసిక ప్రశాంతత కోసం ధ్యానం చేయండి. స్నేహితుల సహకారం లభిస్తుంది.",
        "సింహం" to "నాయకత్వ లక్షణాలు ప్రస్ఫుటమయ్యే రోజు. ఉద్యోగంలో గుర్తింపు లభించవచ్చు.",
        "కన్య" to "వివరాలపై శ్రద్ధ అవసరం. ఆర్థిక లావాదేవీలలో జాగ్రత్త వహించండి.",
        "తుల" to "సంబంధాలలో సమతుల్యత పాటించండి. కళా రంగంలో అభివృద్ధి.",
        "వృశ్చికం" to "దీర్ఘకాలిక లక్ష్యాలపై దృష్టి పెట్టండి. ఆరోగ్యం సాధారణం.",
        "ధనుస్సు" to "ప్రయాణాలు, విద్యాపరమైన విషయాలు అనుకూలంగా ఉంటాయి.",
        "మకరం" to "పట్టుదలతో పనిచేస్తే ఫలితాలు దక్కుతాయి. ఖర్చులు నియంత్రించండి.",
        "కుంభం" to "సృజనాత్మక ఆలోచనలు మంచి ఫలితాలనిస్తాయి. బృంద కార్యకలాపాలు అనుకూలం.",
        "మీనం" to "ఆధ్యాత్మిక అంశాలపై ఆసక్తి పెరుగుతుంది. కుటుంబ శ్రేయస్సు."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        val tb = findViewById<Toolbar>(R.id.toolbar)
        tb.title = getString(R.string.feat_rashiphalalu)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val container = findViewById<LinearLayout>(R.id.container)
        for (rashi in PanchangNames.rashiNames) {
            val block = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 20, 24, 20)
                setBackgroundColor(resources.getColor(R.color.gold_card, theme))
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 8
                layoutParams = lp
            }
            val title = TextView(this).apply {
                text = "🍩  $rashi"
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_red, theme))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            val desc = TextView(this).apply {
                text = phalalu[rashi] ?: ""
                textSize = 13f
                setTextColor(resources.getColor(R.color.black, theme))
                setPadding(0, 6, 0, 0)
            }
            block.addView(title)
            block.addView(desc)
            container.addView(block)
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
