package com.loki.smstracker
import android.content.Context
import android.content.SharedPreferences
object Preferences {
    private const val N = "MapmePrefs"
    private lateinit var p: SharedPreferences
    fun init(c: Context) { p = c.getSharedPreferences(N, Context.MODE_PRIVATE) }
    var phone1: String? get() = p.getString("phone1", null); set(v) = p.edit().putString("phone1", v).apply()
    var phone2: String? get() = p.getString("phone2", null); set(v) = p.edit().putString("phone2", v).apply()
    var isTrackingActive: Boolean get() = p.getBoolean("tracking_active", false); set(v) = p.edit().putBoolean("tracking_active", v).apply()
    var lastLat: Double get() = p.getFloat("last_lat", 0f).toDouble(); set(v) = p.edit().putFloat("last_lat", v.toFloat()).apply()
    var lastLon: Double get() = p.getFloat("last_lon", 0f).toDouble(); set(v) = p.edit().putFloat("last_lon", v.toFloat()).apply()
}
