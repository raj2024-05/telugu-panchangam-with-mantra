package com.example.panchang

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class UserVrat(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val details: String,
    val notifyEnabled: Boolean = false,
    val notifyHour: Int = 6,
    val notifyMinute: Int = 0
)

object UserVratStore {
    private const val PREFS = "user_vratas"
    private const val KEY = "items"

    fun load(context: Context): MutableList<UserVrat> {
        val result = mutableListOf<UserVrat>()
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return result
        runCatching {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                result += UserVrat(
                    id = o.optString("id", UUID.randomUUID().toString()),
                    title = o.optString("title"),
                    details = o.optString("details"),
                    notifyEnabled = o.optBoolean("notifyEnabled", false),
                    notifyHour = o.optInt("notifyHour", 6),
                    notifyMinute = o.optInt("notifyMinute", 0)
                )
            }
        }
        return result
    }

    fun save(context: Context, items: List<UserVrat>) {
        val arr = JSONArray()
        items.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id)
                put("title", it.title)
                put("details", it.details)
                put("notifyEnabled", it.notifyEnabled)
                put("notifyHour", it.notifyHour)
                put("notifyMinute", it.notifyMinute)
            })
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, arr.toString()).apply()
    }

    fun add(context: Context, title: String, details: String) {
        val items = load(context)
        items += UserVrat(title = title, details = details)
        save(context, items)
    }

    fun update(context: Context, item: UserVrat) {
        val items = load(context)
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = item
            save(context, items)
        }
    }

    fun delete(context: Context, id: String) {
        val items = load(context)
        items.removeAll { it.id == id }
        save(context, items)
    }
}
