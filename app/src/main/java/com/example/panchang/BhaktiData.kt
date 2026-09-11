package com.example.panchang

/** Default mantra list intentionally empty. Users add their own mantras in the app. */
data class BhaktiItem(val title: String, val text: String)

object BhaktiData {
    val items = emptyList<BhaktiItem>()
}
