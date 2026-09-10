package com.loki.smstracker
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.action != "android.intent.action.DATA_SMS_RECEIVED") return
        val b = i.extras ?: return
        if (b.getInt("port", -1) != LocationTrackerService.PORT) return
        val pdus = b.get("pdus") as? Array<*> ?: return
        val m = StringBuilder()
        for (p in pdus) m.append(String(SmsMessage.createFromPdu(p as ByteArray, SmsMessage.getDataFormat()).userData))
        val t = m.toString()
        if (t.startsWith("LOKI1")) {
            val p = t.split("|")
            if (p.size >= 3) { Preferences.init(c); p[1].toDoubleOrNull()?.let { Preferences.lastLat = it }; p[2].toDoubleOrNull()?.let { Preferences.lastLon = it } }
        }
    }
}
