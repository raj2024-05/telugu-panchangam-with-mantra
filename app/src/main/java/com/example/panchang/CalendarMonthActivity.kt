package com.example.panchang

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CalendarMonthActivity : AppCompatActivity() {

    private var year = 0
    private var month = 0 // 1..12
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_month)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cal = Calendar.getInstance()
        year = cal.get(Calendar.YEAR)
        month = cal.get(Calendar.MONTH) + 1

        rv = findViewById(R.id.rvDays)
        rv.layoutManager = LinearLayoutManager(this)

        findViewById<android.widget.Button>(R.id.btnPrevMonth).setOnClickListener {
            month -= 1; if (month == 0) { month = 12; year -= 1 }; render()
        }
        findViewById<android.widget.Button>(R.id.btnNextMonth).setOnClickListener {
            month += 1; if (month == 13) { month = 1; year += 1 }; render()
        }
        render()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    data class DayRow(val day: Int, val tithi: String, val paksham: String, val nak: String, val vara: String)

    private fun render() {
        val monthNamesEn = arrayOf("జనవరి","ఫిబ్రవరి","మార్చి","ఏప్రిల్","మే","జూన్","జూలై","ఆగస్టు","సెప్టెంబర్","అక్టోబర్","నవంబర్","డిసెంబర్")
        findViewById<TextView>(R.id.tvMonthYear).text = "${monthNamesEn[month - 1]} $year"

        val prefs = getSharedPreferences("panchang_prefs", MODE_PRIVATE)
        val lat = prefs.getFloat("lat", 17.3850f).toDouble()
        val lon = prefs.getFloat("lon", 78.4867f).toDouble()
        val tz = 5.5

        val cal = GregorianCalendar(year, month - 1, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val rows = (1..daysInMonth).map { d ->
            val r = PanchangCalculator.compute(year, month, d, lat, lon, tz)
            DayRow(d, r.tithiName, r.paksham, r.nakshatraName, r.varaName)
        }
        rv.adapter = DayAdapter(rows)
    }

    class DayAdapter(private val rows: List<DayRow>) : RecyclerView.Adapter<DayAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val num: TextView = v.findViewById(R.id.tvDayNum)
            val tithi: TextView = v.findViewById(R.id.tvDayTithi)
            val nak: TextView = v.findViewById(R.id.tvDayNakshatra)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.row_calendar_day, parent, false)
            return VH(v)
        }
        override fun onBindViewHolder(holder: VH, position: Int) {
            val row = rows[position]
            holder.num.text = String.format("%02d", row.day)
            holder.tithi.text = "${row.tithi}, ${row.paksham}"
            holder.nak.text = "${row.nak} • ${row.vara}"
        }
        override fun getItemCount() = rows.size
    }
}
