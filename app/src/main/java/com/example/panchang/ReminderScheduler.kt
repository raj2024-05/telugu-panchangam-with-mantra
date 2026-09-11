package com.example.panchang

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {
    private const val CHANNEL_ID = "panchang_reminders"
    private const val EXTRA_ID = "reminder_id"
    private const val EXTRA_TITLE = "reminder_title"
    private const val EXTRA_TEXT = "reminder_text"

    private fun requestCode(id: String): Int = id.hashCode() and 0x7fffffff

    fun schedule(context: Context, id: String, title: String, text: String, hour: Int, minute: Int) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_TEXT, text)
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode(id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val now = Calendar.getInstance()
        val first = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarm.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            first.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pi
        )
    }

    fun cancel(context: Context, id: String) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, requestCode(id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pi)
        pi.cancel()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "మంత్రాలు / వ్రతాల నోటిఫికేషన్లు", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    fun channelId() = CHANNEL_ID
}

class ReminderReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.createChannel(context)
        val title = intent.getStringExtra("reminder_title") ?: "తెలుగు పంచాంగం"
        val text = intent.getStringExtra("reminder_text") ?: "మీరు సేవ్ చేసిన అంశాన్ని గుర్తు చేస్తున్నాము."
        val notification = androidx.core.app.NotificationCompat.Builder(context, ReminderScheduler.channelId())
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text.take(120))
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        val manager = androidx.core.app.NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT < 33 || androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            manager.notify((intent.getStringExtra("reminder_id") ?: title).hashCode(), notification)
        }
    }
}
