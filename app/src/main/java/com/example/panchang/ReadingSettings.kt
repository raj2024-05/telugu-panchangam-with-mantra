package com.example.panchang

import android.content.Context
import androidx.core.content.ContextCompat

object ReadingSettings {
    private const val PREF = "reading_settings"
    private const val FONT = "font_size"
    private const val THEME = "theme"

    private val themeNames = arrayOf("తెలుపు", "క్రీమ్", "లేత ఆకుపచ్చ", "లేత నీలం", "డార్క్")
    private val themeColors = arrayOf(
        android.R.color.white,
        android.R.color.background_light,
        android.R.color.holo_green_light,
        android.R.color.holo_blue_light,
        android.R.color.background_dark
    )

    fun fontSize(context: Context): Float =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getFloat(FONT, 18f)

    fun setFontSize(context: Context, size: Float) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putFloat(FONT, size.coerceIn(14f, 32f)).apply()
    }

    fun themeIndex(context: Context): Int =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getInt(THEME, 0)

    fun setTheme(context: Context, index: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putInt(THEME, index.coerceIn(0, themeNames.lastIndex)).apply()
    }

    fun themeNames(): Array<String> = themeNames.copyOf()

    fun backgroundColor(context: Context): Int =
        ContextCompat.getColor(context, themeColors[themeIndex(context)])

    fun textColor(context: Context): Int =
        if (themeIndex(context) == 4) ContextCompat.getColor(context, android.R.color.white)
        else ContextCompat.getColor(context, android.R.color.black)
}
