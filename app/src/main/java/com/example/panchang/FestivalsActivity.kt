package com.example.panchang

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import android.widget.TextView
import android.view.Gravity

class FestivalsActivity : AppCompatActivity() {

    private val festivals = listOf(
        "మకర సంక్రాంతి" to "జనవరి",
        "మహా శివరాత్రి" to "ఫిబ్రవరి/మార్చి",
        "ఉగాది" to "మార్చి/ఏప్రిల్",
        "శ్రీరామ నవమి" to "మార్చి/ఏప్రిల్",
        "హనుమాన్ జయంతి" to "ఏప్రిల్",
        "ఆషాఢ ఏకాదశి" to "జూలై",
        "వరలక్ష్మి వ్రతం" to "ఆగస్టు",
        "శ్రావణ పూర్ణిమ (రాఖీ)" to "ఆగస్టు",
        "శ్రీకృష్ణ జన్మాష్టమి" to "ఆగస్టు",
        "వినాయక చవితి" to "సెప్టెంబర్",
        "దసరా / దుర్గా పూజ" to "అక్టోబర్",
        "దీపావళి" to "అక్టోబర్/నవంబర్",
        "కార్తీక పూర్ణిమ" to "నవంబర్",
        "భోగి, సంక్రాంతి, కనుమ" to "జనవరి"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_list)
        val tb = findViewById<Toolbar>(R.id.toolbar)
        tb.title = getString(R.string.feat_utsavamulu)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val container = findViewById<LinearLayout>(R.id.container)
        for ((name, month) in festivals) {
            val card = CardView(this).apply {
                radius = 8f
                setCardBackgroundColor(resources.getColor(R.color.gold_card, theme))
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 8
                layoutParams = lp
            }
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 20, 20, 20)
            }
            val tvName = TextView(this).apply {
                text = name
                textSize = 15f
                setTextColor(resources.getColor(R.color.text_red, theme))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvMonth = TextView(this).apply {
                text = month
                textSize = 13f
                gravity = Gravity.END
                setTextColor(resources.getColor(R.color.maroon_dark, theme))
            }
            row.addView(tvName)
            row.addView(tvMonth)
            card.addView(row)
            container.addView(card)
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
