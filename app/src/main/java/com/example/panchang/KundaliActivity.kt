package com.example.panchang

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.roundToInt

data class CustomerRecord(
    val name: String,
    val phone: String,
    val place: String,
    val date: String,
    val time: String,
    val latitude: Double,
    val longitude: Double
)

class KundaliActivity : AppCompatActivity() {

    private var lastGrahas: LinkedHashMap<String, Double>? = null
    private var lastAscLong: Double? = null
    private var lastJd: Double? = null
    private var lastLat: Double = 17.385
    private var lastLon: Double = 78.4867
    private var lastSripatiCusps: DoubleArray? = null

    private lateinit var resultContainer: LinearLayout
    private lateinit var vargaContainer: LinearLayout
    private lateinit var spVarga: Spinner
    private lateinit var spCustomer: Spinner
    private lateinit var actPlace: AutoCompleteTextView

    private lateinit var etCustomerName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etBirthTime: EditText
    private lateinit var etYear: EditText
    private lateinit var etMonth: EditText
    private lateinit var etDay: EditText
    private lateinit var etHour: EditText
    private lateinit var etMinute: EditText
    private lateinit var etLat: EditText
    private lateinit var etLon: EditText

    private val prefs by lazy {
        getSharedPreferences("kundali_customers", MODE_PRIVATE)
    }

    private val customers = mutableListOf<CustomerRecord>()
    private val placeExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var placeSearchRunnable: Runnable? = null
    private var suppressPlaceSearch = false
    private val placeMap = LinkedHashMap<String, BirthPlace>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kundali)

        val tb = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(tb)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etCustomerName = findViewById(R.id.etCustomerName)
        etPhone = findViewById(R.id.etPhone)
        actPlace = findViewById(R.id.actPlace)
        etBirthDate = findViewById(R.id.etBirthDate)
        etBirthTime = findViewById(R.id.etBirthTime)

        etYear = findViewById(R.id.etYear)
        etMonth = findViewById(R.id.etMonth)
        etDay = findViewById(R.id.etDay)
        etHour = findViewById(R.id.etHour)
        etMinute = findViewById(R.id.etMinute)
        etLat = findViewById(R.id.etLat)
        etLon = findViewById(R.id.etLon)

        resultContainer = findViewById(R.id.resultContainer)
        vargaContainer = findViewById(R.id.vargaContainer)
        spVarga = findViewById(R.id.spVarga)
        spCustomer = findViewById(R.id.spCustomer)

        setupDatePicker()
        setupTimePicker()
        setupPlaceSearch()
        setupCustomers()
        setupCharts()

        findViewById<Button>(R.id.btnSaveCustomer).setOnClickListener {
            saveCustomer()
        }

        findViewById<Button>(R.id.btnOpenCustomer).setOnClickListener {
            val position = spCustomer.selectedItemPosition
            if (position > 0 && position - 1 < customers.size) {
                fillCustomer(customers[position - 1])
            } else {
                toast("ముందుగా కస్టమర్‌ను ఎంచుకోండి")
            }
        }

        findViewById<Button>(R.id.btnDeleteCustomer).setOnClickListener {
            deleteSelectedCustomer()
        }

        findViewById<Button>(R.id.btnCompute).setOnClickListener {
            calculateKundali()
        }

        setDefaultBirthDateTime()
    }

    private fun setupDatePicker() {
        etBirthDate.setOnClickListener {
            val cal = GregorianCalendar()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    etBirthDate.setText(
                        String.format("%04d-%02d-%02d", year, month + 1, day)
                    )
                    syncDateParts()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker() {
        etBirthTime.setOnClickListener {
            val cal = GregorianCalendar()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    etBirthTime.setText(
                        String.format("%02d:%02d", hour, minute)
                    )
                    syncTimeParts()
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setDefaultBirthDateTime() {
        val cal = GregorianCalendar()
        etBirthDate.setText(
            String.format(
                "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        )
        etBirthTime.setText(
            String.format(
                "%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE)
            )
        )
        syncDateParts()
        syncTimeParts()
    }

    private fun syncDateParts() {
        val p = etBirthDate.text.toString().split("-")
        if (p.size == 3) {
            etYear.setText(p[0])
            etMonth.setText(p[1])
            etDay.setText(p[2])
        }
    }

    private fun syncTimeParts() {
        val p = etBirthTime.text.toString().split(":")
        if (p.size == 2) {
            etHour.setText(p[0])
            etMinute.setText(p[1])
        }
    }

    private fun setupPlaceSearch() {
        updatePlaceAdapter(IndianPlaces.all)

        actPlace.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position).toString()
            val info = placeMap[selected]
            if (info != null) {
                applyPlace(info)
            }
        }

        actPlace.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (suppressPlaceSearch) return
                val query = s?.toString()?.trim().orEmpty()

                placeSearchRunnable?.let { mainHandler.removeCallbacks(it) }

                if (query.length < 1) {
                    updatePlaceAdapter(IndianPlaces.all)
                    return
                }

                placeSearchRunnable = Runnable {
                    searchPlaces(query)
                }
                mainHandler.postDelayed(placeSearchRunnable!!, 400)
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
    }

    private fun searchPlaces(query: String) {
        val local = IndianPlaces.all.filter {
            it.name.contains(query, ignoreCase = true)
        }

        updatePlaceAdapter(local)

        placeExecutor.execute {
            try {
                if (!android.location.Geocoder.isPresent()) return@execute

                val geocoder = android.location.Geocoder(this, Locale.getDefault())
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(query, 8).orEmpty()

                val geoPlaces = results.mapNotNull { a ->
                    val line = a.getAddressLine(0) ?: return@mapNotNull null
                    if (a.hasLatitude() && a.hasLongitude()) {
                        BirthPlace(line, a.latitude, a.longitude)
                    } else null
                }

                mainHandler.post {
                    val merged = (local + geoPlaces)
                        .distinctBy { it.name }
                        .take(12)
                    updatePlaceAdapter(merged)
                }
            } catch (_: Exception) {
                // Local list remains available when geocoding is unavailable/offline.
            }
        }
    }

    private fun updatePlaceAdapter(places: List<BirthPlace>) {
        placeMap.clear()
        val names = places.map {
            placeMap[it.name] = it
            it.name
        }

        actPlace.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                names
            )
        )
        actPlace.threshold = 1
        if (actPlace.hasFocus() && names.isNotEmpty()) {
            actPlace.post { actPlace.showDropDown() }
        }
    }

    private fun applyPlace(place: BirthPlace) {
        suppressPlaceSearch = true
        actPlace.setText(place.name, false)
        suppressPlaceSearch = false

        etLat.setText(String.format(Locale.US, "%.6f", place.latitude))
        etLon.setText(String.format(Locale.US, "%.6f", place.longitude))
    }

    private fun setupCustomers() {
        loadCustomers()
        refreshCustomerSpinner()

        spCustomer.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position > 0 && position - 1 < customers.size) {
                        fillCustomer(customers[position - 1])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun loadCustomers() {
        customers.clear()
        val raw = prefs.getString("customers", "[]") ?: "[]"
        try {
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                customers.add(
                    CustomerRecord(
                        o.optString("name"),
                        o.optString("phone"),
                        o.optString("place"),
                        o.optString("date"),
                        o.optString("time"),
                        o.optDouble("lat", 17.385),
                        o.optDouble("lon", 78.4867)
                    )
                )
            }
        } catch (_: Exception) {
            customers.clear()
        }
    }

    private fun persistCustomers() {
        val array = JSONArray()
        customers.forEach { c ->
            array.put(
                JSONObject().apply {
                    put("name", c.name)
                    put("phone", c.phone)
                    put("place", c.place)
                    put("date", c.date)
                    put("time", c.time)
                    put("lat", c.latitude)
                    put("lon", c.longitude)
                }
            )
        }
        prefs.edit().putString("customers", array.toString()).apply()
    }

    private fun refreshCustomerSpinner(selectIndex: Int = 0) {
        val labels = mutableListOf("కొత్త కస్టమర్ / ఎంచుకోండి")
        labels.addAll(customers.map { it.name })

        spCustomer.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )

        if (selectIndex in labels.indices) {
            spCustomer.setSelection(selectIndex)
        }
    }

    private fun saveCustomer() {
        val name = etCustomerName.text.toString().trim()
        if (name.isEmpty()) {
            toast("కస్టమర్ పేరు నమోదు చేయండి")
            return
        }

        syncDateParts()
        syncTimeParts()

        val record = CustomerRecord(
            name = name,
            phone = etPhone.text.toString().trim(),
            place = actPlace.text.toString().trim(),
            date = etBirthDate.text.toString().trim(),
            time = etBirthTime.text.toString().trim(),
            latitude = etLat.text.toString().toDoubleOrNull() ?: 17.385,
            longitude = etLon.text.toString().toDoubleOrNull() ?: 78.4867
        )

        val existing = customers.indexOfFirst {
            it.name.equals(name, ignoreCase = true)
        }

        if (existing >= 0) {
            customers[existing] = record
            toast("కస్టమర్ వివరాలు నవీకరించబడ్డాయి")
            persistCustomers()
            refreshCustomerSpinner(existing + 1)
        } else {
            customers.add(record)
            persistCustomers()
            refreshCustomerSpinner(customers.size)
            toast("కస్టమర్ వివరాలు సేవ్ అయ్యాయి")
        }
    }

    private fun fillCustomer(c: CustomerRecord) {
        etCustomerName.setText(c.name)
        etPhone.setText(c.phone)
        actPlace.setText(c.place, false)
        etBirthDate.setText(c.date)
        etBirthTime.setText(c.time)
        syncDateParts()
        syncTimeParts()
        etLat.setText(String.format(Locale.US, "%.6f", c.latitude))
        etLon.setText(String.format(Locale.US, "%.6f", c.longitude))
    }

    private fun deleteSelectedCustomer() {
        val position = spCustomer.selectedItemPosition
        if (position <= 0 || position - 1 >= customers.size) {
            toast("తొలగించడానికి కస్టమర్‌ను ఎంచుకోండి")
            return
        }

        val index = position - 1
        val customer = customers[index]

        AlertDialog.Builder(this)
            .setTitle("కస్టమర్ తొలగింపు")
            .setMessage("${customer.name} వివరాలను తొలగించాలా?")
            .setNegativeButton("వద్దు", null)
            .setPositiveButton("అవును") { _, _ ->
                customers.removeAt(index)
                persistCustomers()
                refreshCustomerSpinner()
                clearCustomerFields()
                toast("కస్టమర్ తొలగించబడింది")
            }
            .show()
    }

    private fun clearCustomerFields() {
        etCustomerName.text.clear()
        etPhone.text.clear()
        actPlace.text.clear()
        etLat.setText("17.385000")
        etLon.setText("78.486700")
        setDefaultBirthDateTime()
    }

    private fun setupCharts() {
        val labels = VargaKind.values().map { it.label }

        spVarga.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )

        spVarga.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    renderVarga(VargaKind.values()[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
    }

    private fun calculateKundali() {
        try {
            syncDateParts()
            syncTimeParts()

            val year = etYear.text.toString().toInt()
            val month = etMonth.text.toString().toInt()
            val day = etDay.text.toString().toInt()
            val hour = etHour.text.toString().toIntOrNull() ?: 0
            val minute = etMinute.text.toString().toIntOrNull() ?: 0
            val lat = etLat.text.toString().toDoubleOrNull() ?: 17.385
            val lon = etLon.text.toString().toDoubleOrNull() ?: 78.4867
            val tz = 5.5

            val hourUTC = (hour + minute / 60.0) - tz
            val jd = PanchangCalculator.julianDay(
                year, month, day, hourUTC
            )

            val grahas = PanchangCalculator.navagrahaLongitudes(jd)
            val moonLong = grahas["చంద్రుడు"]!!
            val ascLong = PanchangCalculator.ascendantLongitude(jd, lat, lon)

            lastGrahas = grahas
            lastAscLong = ascLong
            lastJd = jd
            lastLat = lat
            lastLon = lon
            lastSripatiCusps =
                PanchangCalculator.sripatiHouseCusps(jd, lat, lon)

            val ascRashiIdx =
                floor(ascLong / 30.0).toInt().coerceIn(0, 11)

            val moonRashiIdx =
                floor(moonLong / 30.0).toInt().coerceIn(0, 11)

            val nakIdx =
                floor(moonLong / (360.0 / 27.0))
                    .toInt().coerceIn(0, 26)

            val pada =
                (floor(
                    (moonLong % (360.0 / 27.0)) /
                        (360.0 / 108.0)
                ).toInt() + 1).coerceIn(1, 4)

            resultContainer.removeAllViews()

            addHeader(resultContainer, "ముఖ్యాంశాలు")
            addLine(
                resultContainer,
                "లగ్నం",
                PanchangNames.rashiNames[ascRashiIdx]
            )
            addLine(
                resultContainer,
                "చంద్ర రాశి",
                PanchangNames.rashiNames[moonRashiIdx]
            )
            addLine(
                resultContainer,
                "జన్మ నక్షత్రం",
                "${PanchangNames.nakshatraNames[nakIdx]}, పాదం $pada"
            )

            addHeader(
                resultContainer,
                "నవగ్రహ స్థానాలు (సైడీరియల్ - లాహిరి)"
            )

            for ((graha, longitude) in grahas) {
                val rashiIdx =
                    floor(longitude / 30.0)
                        .toInt().coerceIn(0, 11)
                val degInSign = longitude % 30.0
                val deg = degInSign.toInt()
                val min =
                    ((degInSign - deg) * 60).roundToInt()

                addLine(
                    resultContainer,
                    graha,
                    "${PanchangNames.rashiNames[rashiIdx]} ${deg}°${min}'"
                )
            }

            addHeader(
                resultContainer,
                "వింశోత్తరి దశ"
            )

            val dashas =
                PanchangCalculator.vimshottariDasha(moonLong)

            val birthCal =
                GregorianCalendar(year, month - 1, day)

            for (d in dashas) {
                val startCal =
                    birthCal.clone() as GregorianCalendar
                startCal.add(
                    Calendar.DAY_OF_YEAR,
                    (d.startYearsFromNow * 365.25).toInt()
                )

                val endCal =
                    birthCal.clone() as GregorianCalendar
                endCal.add(
                    Calendar.DAY_OF_YEAR,
                    (d.endYearsFromNow * 365.25).toInt()
                )

                val fmt = { c: GregorianCalendar ->
                    String.format(
                        "%04d-%02d-%02d",
                        c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH) + 1,
                        c.get(Calendar.DAY_OF_MONTH)
                    )
                }

                addLine(
                    resultContainer,
                    "${d.lordTelugu} దశ",
                    "${fmt(startCal)} → ${fmt(endCal)}"
                )
            }

            addHeader(resultContainer, "గమనిక")
            addLine(
                resultContainer,
                "ఖచ్చితత్వం",
                "Swiss Ephemeris (Moshier) + Lahiri ayanamsa"
            )

            renderVarga(
                VargaKind.values()[spVarga.selectedItemPosition]
            )

        } catch (e: Exception) {
            resultContainer.removeAllViews()
            addLine(
                resultContainer,
                "దోషం",
                "దయచేసి తేదీ, సమయం, స్థలం వివరాలను సరిగ్గా నమోదు చేయండి. ${e.message ?: ""}"
            )
        }
    }

    private fun renderVarga(kind: VargaKind) {
        val grahas = lastGrahas ?: return
        val ascLong = lastAscLong ?: return

        vargaContainer.removeAllViews()
        addHeader(vargaContainer, kind.label)

        when (kind) {
            VargaKind.SRIPATI -> {
                val cusps = lastSripatiCusps ?: return

                val houseMap = LinkedHashMap<String, Int>()
                houseMap["లగ్నం"] = 1

                for ((graha, longitude) in grahas) {
                    houseMap[graha] =
                        PanchangCalculator.sripatiHouseNumber(
                            longitude,
                            cusps
                        )
                }

                val view = SripatiHouseChartView(
                    this,
                    houseMap,
                    cusps
                )

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(390)
                )
                lp.topMargin = dpToPx(8)
                lp.bottomMargin = dpToPx(12)
                vargaContainer.addView(view, lp)

                addNote(
                    vargaContainer,
                    "శ్రీపతి పద్ధతిలో 1-4, 4-7, 7-10, 10-1 చతుర్భాగాలను మూడు సమాన భాగాలుగా విభజించి భావ కస్పులు లెక్కించబడ్డాయి."
                )
            }

            VargaKind.BHAVA -> {
                val houseMap = LinkedHashMap<String, Int>()
                houseMap["లగ్నం"] = 1

                for ((graha, longitude) in grahas) {
                    houseMap[graha] =
                        VargaCalculator.bhavaNumber(
                            longitude,
                            ascLong
                        )
                }

                val view = EqualHouseChartView(
                    this,
                    houseMap
                )

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(390)
                )
                vargaContainer.addView(view, lp)

                addNote(
                    vargaContainer,
                    "ఇది లగ్న డిగ్రీ ఆధారంగా 12 సమాన భావాల పద్ధతి."
                )
            }

            else -> {
                val planetSigns = LinkedHashMap<String, Int>()

                planetSigns["లగ్నం"] =
                    VargaCalculator.vargaSign(
                        ascLong,
                        kind
                    )

                for ((graha, longitude) in grahas) {
                    planetSigns[graha] =
                        VargaCalculator.vargaSign(
                            longitude,
                            kind
                        )
                }

                val chart = SouthIndianChartView(
                    this,
                    planetSigns
                )

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(390)
                )
                lp.topMargin = dpToPx(8)
                lp.bottomMargin = dpToPx(12)

                vargaContainer.addView(chart, lp)

                if (kind != VargaKind.D1) {
                    addNote(
                        vargaContainer,
                        "ప్రస్తుత VargaCalculatorలో అమలు చేసిన పరాశర నియమాల ప్రకారం లెక్కించబడింది."
                    )
                }
            }
        }
    }

    private fun addHeader(container: LinearLayout, text: String) {
        val tv = TextView(this).apply {
            this.text = text
            textSize = 16f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(resources.getColor(R.color.text_red, theme))
            setPadding(4, 20, 4, 8)
        }
        container.addView(tv)
    }

    private fun addLine(
        container: LinearLayout,
        label: String,
        value: String
    ) {
        val tv = TextView(this).apply {
            text = "$label:  $value"
            textSize = 14f
            setTextColor(resources.getColor(R.color.black, theme))
            setBackgroundColor(resources.getColor(R.color.gold_card, theme))
            setPadding(20, 16, 20, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 3
            }
        }
        container.addView(tv)
    }

    private fun addNote(container: LinearLayout, text: String) {
        val tv = TextView(this).apply {
            this.text = "ℹ️ $text"
            textSize = 12f
            setTextColor(resources.getColor(R.color.maroon_dark, theme))
            setPadding(4, 10, 4, 4)
        }
        container.addView(tv)
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).roundToInt()

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        placeSearchRunnable?.let { mainHandler.removeCallbacks(it) }
        placeExecutor.shutdownNow()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/* ---------- South Indian sign chart ---------- */

class SouthIndianChartView(
    context: Context,
    private val planetSigns: Map<String, Int>
) : View(context) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(12f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(15f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val positions = arrayOf(
        Pair(1, 0), Pair(2, 0), Pair(3, 0), Pair(3, 1),
        Pair(3, 2), Pair(3, 3), Pair(2, 3), Pair(1, 3),
        Pair(0, 3), Pair(0, 2), Pair(0, 1), Pair(0, 0)
    )

    private val signs = arrayOf(
        "మేషం", "వృషభం", "మిథునం", "కర్కాటకం",
        "సింహం", "కన్య", "తుల", "వృశ్చికం",
        "ధనుస్సు", "మకరం", "కుంభం", "మీనం"
    )

    private val shortGrahas = mapOf(
        "సూర్యుడు" to "సూ",
        "చంద్రుడు" to "చం",
        "కుజుడు" to "కు",
        "బుధుడు" to "బు",
        "గురువు" to "గు",
        "గురుడు" to "గు",
        "శుక్రుడు" to "శు",
        "శని" to "శ",
        "రాహువు" to "రా",
        "కేతువు" to "కే",
        "రాహు" to "రా",
        "కేతు" to "కే"
    )

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        val cell = minOf(width.toFloat(), height.toFloat()) / 4f
        val left = (width - cell * 4f) / 2f
        val top = (height - cell * 4f) / 2f

        for (sign in 0..11) {
            val p = positions[sign]
            val l = left + p.first * cell
            val t = top + p.second * cell

            canvas.drawRect(
                l, t, l + cell, t + cell, borderPaint
            )

            canvas.drawText(
                signs[sign],
                l + dp(5f),
                t + dp(18f),
                signPaint
            )

            val names = planetSigns.filterValues { it == sign }.keys

            var y = t + dp(43f)

            for (name in names) {
                val text = if (name == "లగ్నం") {
                    "ల"
                } else {
                    shortGrahas[name] ?: name.take(2)
                }

                canvas.drawText(
                    text,
                    l + dp(8f),
                    y,
                    planetPaint
                )
                y += dp(22f)
            }
        }
    }

    private fun dp(v: Float) =
        v * resources.displayMetrics.density
}

/* ---------- Equal house chart ---------- */

class EqualHouseChartView(
    context: Context,
    private val houses: Map<String, Int>
) : View(context) {

    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(12f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val planet = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(15f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val positions = arrayOf(
        Pair(1, 0), Pair(2, 0), Pair(3, 0), Pair(3, 1),
        Pair(3, 2), Pair(3, 3), Pair(2, 3), Pair(1, 3),
        Pair(0, 3), Pair(0, 2), Pair(0, 1), Pair(0, 0)
    )

    private val short = mapOf(
        "లగ్నం" to "ల",
        "సూర్యుడు" to "సూ",
        "చంద్రుడు" to "చం",
        "కుజుడు" to "కు",
        "బుధుడు" to "బు",
        "గురువు" to "గు",
        "శుక్రుడు" to "శు",
        "శని" to "శ",
        "రాహువు" to "రా",
        "కేతువు" to "కే"
    )

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        val cell = minOf(width.toFloat(), height.toFloat()) / 4f
        val left = (width - cell * 4f) / 2f
        val top = (height - cell * 4f) / 2f

        for (house in 1..12) {
            val p = positions[house - 1]
            val l = left + p.first * cell
            val t = top + p.second * cell

            canvas.drawRect(
                l, t, l + cell, t + cell, border
            )

            canvas.drawText(
                "భావం $house",
                l + dp(5f),
                t + dp(18f),
                title
            )

            var y = t + dp(43f)
            houses.filterValues { it == house }.keys.forEach { name ->
                canvas.drawText(
                    short[name] ?: name.take(2),
                    l + dp(8f),
                    y,
                    planet
                )
                y += dp(22f)
            }
        }
    }

    private fun dp(v: Float) =
        v * resources.displayMetrics.density
}

/* ---------- Sripati house chart ---------- */

class SripatiHouseChartView(
    context: Context,
    private val houses: Map<String, Int>,
    private val cusps: DoubleArray
) : View(context) {

    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }

    private val housePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(11f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = dp(15f)
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    private val positions = arrayOf(
        Pair(1, 0), Pair(2, 0), Pair(3, 0), Pair(3, 1),
        Pair(3, 2), Pair(3, 3), Pair(2, 3), Pair(1, 3),
        Pair(0, 3), Pair(0, 2), Pair(0, 1), Pair(0, 0)
    )

    private val signs = arrayOf(
        "మేషం", "వృషభం", "మిథునం", "కర్కాటకం",
        "సింహం", "కన్య", "తుల", "వృశ్చికం",
        "ధనుస్సు", "మకరం", "కుంభం", "మీనం"
    )

    private val short = mapOf(
        "లగ్నం" to "ల",
        "సూర్యుడు" to "సూ",
        "చంద్రుడు" to "చం",
        "కుజుడు" to "కు",
        "బుధుడు" to "బు",
        "గురువు" to "గు",
        "శుక్రుడు" to "శు",
        "శని" to "శ",
        "రాహువు" to "రా",
        "కేతువు" to "కే"
    )

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)

        val cell = minOf(width.toFloat(), height.toFloat()) / 4f
        val left = (width - cell * 4f) / 2f
        val top = (height - cell * 4f) / 2f

        for (house in 1..12) {
            val p = positions[house - 1]
            val l = left + p.first * cell
            val t = top + p.second * cell

            canvas.drawRect(
                l, t, l + cell, t + cell, border
            )

            val cusp = if (cusps.size >= 13) cusps[house] else 0.0
            val sign = floor(cusp / 30.0)
                .toInt().coerceIn(0, 11)
            val deg = (cusp % 30.0).toInt()

            canvas.drawText(
                "భావం $house • ${signs[sign]} $deg°",
                l + dp(4f),
                t + dp(17f),
                housePaint
            )

            var y = t + dp(43f)
            houses.filterValues { it == house }.keys.forEach { name ->
                canvas.drawText(
                    short[name] ?: name.take(2),
                    l + dp(8f),
                    y,
                    planetPaint
                )
                y += dp(22f)
            }
        }
    }

    private fun dp(v: Float) =
        v * resources.displayMetrics.density
}
