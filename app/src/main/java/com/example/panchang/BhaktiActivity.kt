package com.example.panchang

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class BhaktiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bhakti)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "మంత్రాలు"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ReminderScheduler.createChannel(this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        findViewById<Button>(R.id.btnAddSloka).apply {
            text = "＋ కొత్త మంత్రం జోడించండి"
            setOnClickListener { showEditDialog(null) }
        }
        renderItems()
    }

    private fun renderItems() {
        val container = findViewById<LinearLayout>(R.id.container)
        container.removeAllViews()
        val items = UserSlokaStore.load(this)
        if (items.isEmpty()) {
            container.addView(TextView(this).apply {
                text = "ఇక్కడ మంత్రాలు లేవు.\n\n‘కొత్త మంత్రం జోడించండి’ నొక్కి మీ మంత్రాన్ని నమోదు చేయండి."
                textSize = 16f
                setPadding(20, 30, 20, 30)
            })
        }
        items.forEach { addItemRow(container, it) }
    }

    private fun addItemRow(container: LinearLayout, item: UserSloka) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 12, 16)
            setBackgroundColor(resources.getColor(R.color.gold_card, theme))
        }
        val title = TextView(this).apply {
            text = "🪔  ${item.title}"
            textSize = 17f
            setTextColor(resources.getColor(R.color.text_red, theme))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setOnClickListener { showText(item.title, item.text) }
        }
        row.addView(title)
        val status = TextView(this).apply {
            text = if (item.notifyEnabled) "🔔 నోటిఫికేషన్: %02d:%02d".format(item.notifyHour, item.notifyMinute) else "🔕 నోటిఫికేషన్ లేదు"
            textSize = 13f
            setPadding(0, 8, 0, 8)
        }
        row.addView(status)
        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        fun button(label: String, action: () -> Unit): Button = Button(this@BhaktiActivity).apply {
            text = label
            setOnClickListener { action() }
            minHeight = 0
            minWidth = 0
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 4 }
        }
        buttons.addView(button("చదవండి") { showText(item.title, item.text) })
        buttons.addView(button("మార్చు") { showEditDialog(item) })
        buttons.addView(button(if (item.notifyEnabled) "నోటిఫికేషన్ తొలగించు" else "నోటిఫికేషన్") { toggleNotification(item) })
        buttons.addView(button("తొలగించు") { confirmDelete(item) })
        row.addView(buttons)
        container.addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 10 })
    }

    private fun showText(title: String, text: String) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(12, 4, 12, 4) }
        val reading = TextView(this).apply {
            this.text = text
            textSize = ReadingSettings.fontSize(this@BhaktiActivity)
            setTextColor(ReadingSettings.textColor(this@BhaktiActivity))
            setPadding(24, 18, 24, 30)
            setBackgroundColor(ReadingSettings.backgroundColor(this@BhaktiActivity))
            setLineSpacing(0f, 1.25f)
        }
        val scroll = ScrollView(this).apply {
            addView(reading)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }
        fun control(label: String, action: () -> Unit): Button = Button(this).apply {
            text = label
            minHeight = 0
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = 4 }
        }
        controls.addView(control("A−") {
            ReadingSettings.setFontSize(this@BhaktiActivity, ReadingSettings.fontSize(this@BhaktiActivity) - 1f)
            reading.textSize = ReadingSettings.fontSize(this@BhaktiActivity)
        })
        controls.addView(control("A+") {
            ReadingSettings.setFontSize(this@BhaktiActivity, ReadingSettings.fontSize(this@BhaktiActivity) + 1f)
            reading.textSize = ReadingSettings.fontSize(this@BhaktiActivity)
        })
        controls.addView(control("థీమ్") {
            AlertDialog.Builder(this).setTitle("చదివే నేపథ్య థీమ్")
                .setSingleChoiceItems(ReadingSettings.themeNames(), ReadingSettings.themeIndex(this)) { d, which ->
                    ReadingSettings.setTheme(this@BhaktiActivity, which)
                    reading.setBackgroundColor(ReadingSettings.backgroundColor(this@BhaktiActivity))
                    reading.setTextColor(ReadingSettings.textColor(this@BhaktiActivity))
                    d.dismiss()
                }.show()
        })
        box.addView(controls)
        box.addView(scroll)
        AlertDialog.Builder(this).setTitle(title).setView(box).setPositiveButton("మూసివేయి", null).show()
    }

    private fun showEditDialog(item: UserSloka?) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 8, 24, 0) }
        val title = EditText(this).apply { hint = "మంత్రం పేరు"; setText(item?.title ?: "") }
        val text = EditText(this).apply {
            hint = "మంత్ర పాఠ్యం"; minLines = 10; gravity = Gravity.TOP
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setText(item?.text ?: "")
        }
        box.addView(title); box.addView(text)
        AlertDialog.Builder(this)
            .setTitle(if (item == null) "కొత్త మంత్రం" else "మంత్రం మార్చండి")
            .setView(box).setNegativeButton("రద్దు", null)
            .setPositiveButton("సేవ్") { _, _ ->
                val t = title.text.toString().trim(); val body = text.text.toString().trim()
                if (t.isEmpty() || body.isEmpty()) return@setPositiveButton
                if (item == null) {
                    UserSlokaStore.add(this, t, body)
                } else {
                    val updated = item.copy(title = t, text = body)
                    if (item.notifyEnabled) {
                        ReminderScheduler.cancel(this, item.id)
                        ReminderScheduler.schedule(this, updated.id, updated.title, updated.text, updated.notifyHour, updated.notifyMinute)
                    }
                    UserSlokaStore.update(this, updated)
                }
                renderItems()
            }.show()
    }

    private fun toggleNotification(item: UserSloka) {
        if (item.notifyEnabled) {
            ReminderScheduler.cancel(this, item.id)
            UserSlokaStore.update(this, item.copy(notifyEnabled = false))
            renderItems()
            return
        }
        val now = java.util.Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val updated = item.copy(notifyEnabled = true, notifyHour = hour, notifyMinute = minute)
            UserSlokaStore.update(this, updated)
            ReminderScheduler.schedule(this, updated.id, updated.title, updated.text, hour, minute)
            renderItems()
        }, now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE), true).show()
    }

    private fun confirmDelete(item: UserSloka) {
        AlertDialog.Builder(this).setTitle("మంత్రం తొలగించాలా?")
            .setMessage(item.title).setNegativeButton("రద్దు", null)
            .setPositiveButton("తొలగించు") { _, _ ->
                ReminderScheduler.cancel(this, item.id)
                UserSlokaStore.delete(this, item.id)
                renderItems()
            }.show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
