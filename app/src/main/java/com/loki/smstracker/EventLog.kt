package com.loki.smstracker
import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
object EventLog {
    private const val F = "mapme_log.txt"
    fun log(c: Context, m: String) {
        val t = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.FRANCE).format(Date())
        File(c.filesDir, F).appendText("[$t] $m\n")
    }
}
