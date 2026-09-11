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

class VratActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vrat)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = "వ్రత సేకరణ"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        ReminderScheduler.createChannel(this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1002)
        }
        findViewById<Button>(R.id.btnAddSloka).apply {
            text = "＋ కొత్త వ్రతం జోడించండి"
            setOnClickListener { showEditDialog(null) }
        }
        renderItems()
    }

    private fun renderItems() {
        val container = findViewById<LinearLayout>(R.id.container)
        container.removeAllViews()
        val items = UserVratStore.load(this)
        if (items.isEmpty()) container.addView(TextView(this).apply {
            text = "ఇక్కడ వ్రత వివరాలు లేవు.\n\n‘కొత్త వ్రతం జోడించండి’ నొక్కి మీ వ్రతాన్ని నమోదు చేయండి."
            textSize = 16f; setPadding(20, 30, 20, 30)
        })
        items.forEach { item ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18,16,12,16); setBackgroundColor(resources.getColor(R.color.gold_card, theme)) }
            row.addView(TextView(this).apply {
                text = "🛕  ${item.title}"; textSize = 17f; setTextColor(resources.getColor(R.color.text_red, theme)); setTypeface(typeface, android.graphics.Typeface.BOLD)
                setOnClickListener { showDetails(item) }
            })
            row.addView(TextView(this).apply { text = if (item.notifyEnabled) "🔔 నోటిఫికేషన్: %02d:%02d".format(item.notifyHour,item.notifyMinute) else "🔕 నోటిఫికేషన్ లేదు"; textSize=13f; setPadding(0,8,0,8) })
            val buttons = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL }
            fun button(label:String, action:()->Unit)=Button(this@VratActivity).apply { text=label; setOnClickListener{action()}; minHeight=0; minWidth=0; layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{marginEnd=4} }
            buttons.addView(button("చూడండి"){showDetails(item)})
            buttons.addView(button("మార్చు"){showEditDialog(item)})
            buttons.addView(button(if(item.notifyEnabled) "నోటిఫికేషన్ తొలగించు" else "నోటిఫికేషన్"){toggleNotification(item)})
            buttons.addView(button("తొలగించు"){confirmDelete(item)})
            row.addView(buttons)
            container.addView(row, LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=10})
        }
    }

    private fun showDetails(item: UserVrat) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(12, 4, 12, 4) }
        val reading = TextView(this).apply {
            text = item.details
            textSize = ReadingSettings.fontSize(this@VratActivity)
            setTextColor(ReadingSettings.textColor(this@VratActivity))
            setPadding(24, 18, 24, 30)
            setBackgroundColor(ReadingSettings.backgroundColor(this@VratActivity))
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
            ReadingSettings.setFontSize(this@VratActivity, ReadingSettings.fontSize(this@VratActivity) - 1f)
            reading.textSize = ReadingSettings.fontSize(this@VratActivity)
        })
        controls.addView(control("A+") {
            ReadingSettings.setFontSize(this@VratActivity, ReadingSettings.fontSize(this@VratActivity) + 1f)
            reading.textSize = ReadingSettings.fontSize(this@VratActivity)
        })
        controls.addView(control("థీమ్") {
            AlertDialog.Builder(this).setTitle("చదివే నేపథ్య థీమ్")
                .setSingleChoiceItems(ReadingSettings.themeNames(), ReadingSettings.themeIndex(this)) { d, which ->
                    ReadingSettings.setTheme(this@VratActivity, which)
                    reading.setBackgroundColor(ReadingSettings.backgroundColor(this@VratActivity))
                    reading.setTextColor(ReadingSettings.textColor(this@VratActivity))
                    d.dismiss()
                }.show()
        })
        box.addView(controls)
        box.addView(scroll)
        AlertDialog.Builder(this).setTitle(item.title).setView(box).setPositiveButton("మూసివేయి", null).show()
    }

    private fun showEditDialog(item: UserVrat?) {
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,8,24,0)}
        val title=EditText(this).apply{hint="వ్రతం పేరు";setText(item?.title ?: "")}
        val details=EditText(this).apply{hint="వ్రత వివరాలు / విధానం / తేదీ నియమాలు";minLines=10;gravity=Gravity.TOP;inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE;setText(item?.details ?: "")}
        box.addView(title);box.addView(details)
        AlertDialog.Builder(this).setTitle(if(item==null)"కొత్త వ్రతం" else "వ్రతం మార్చండి").setView(box).setNegativeButton("రద్దు",null).setPositiveButton("సేవ్"){_,_->
            val t=title.text.toString().trim();val d=details.text.toString().trim();if(t.isEmpty()||d.isEmpty())return@setPositiveButton
            if(item==null) {
                UserVratStore.add(this,t,d)
            } else {
                val updated = item.copy(title=t, details=d)
                if (item.notifyEnabled) {
                    ReminderScheduler.cancel(this, item.id)
                    ReminderScheduler.schedule(this, updated.id, updated.title, updated.details, updated.notifyHour, updated.notifyMinute)
                }
                UserVratStore.update(this, updated)
            }
            renderItems()
        }.show()
    }

    private fun toggleNotification(item: UserVrat) {
        if(item.notifyEnabled){ReminderScheduler.cancel(this,item.id);UserVratStore.update(this,item.copy(notifyEnabled=false));renderItems();return}
        val now=java.util.Calendar.getInstance()
        TimePickerDialog(this,{_,h,m->val u=item.copy(notifyEnabled=true,notifyHour=h,notifyMinute=m);UserVratStore.update(this,u);ReminderScheduler.schedule(this,u.id,u.title,u.details,h,m);renderItems()},now.get(java.util.Calendar.HOUR_OF_DAY),now.get(java.util.Calendar.MINUTE),true).show()
    }

    private fun confirmDelete(item: UserVrat){AlertDialog.Builder(this).setTitle("వ్రతం తొలగించాలా?").setMessage(item.title).setNegativeButton("రద్దు",null).setPositiveButton("తొలగించు"){_,_->ReminderScheduler.cancel(this,item.id);UserVratStore.delete(this,item.id);renderItems()}.show()}
    override fun onSupportNavigateUp():Boolean{finish();return true}
}
