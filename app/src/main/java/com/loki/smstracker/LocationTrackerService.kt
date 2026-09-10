package com.loki.smstracker
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
class LocationTrackerService : android.app.Service() {
    companion object {
        const val CHANNEL_ID = "LOC_TRACK"
        const val ACTION_SEND_NOW = "com.loki.smstracker.SEND_NOW"
        const val PORT = 7777
        private const val INTERVAL = 60_000L
    }
    private lateinit var fused: FusedLocationProviderClient
    private lateinit var cb: LocationCallback
    private val h = Handler(Looper.getMainLooper())
    private var loc: Location? = null
    private val task = object : Runnable { override fun run() { sendPos(); h.postDelayed(this, INTERVAL) } }
    override fun onCreate() {
        super.onCreate(); Preferences.init(this); chan()
        startForeground(1, notif()); Preferences.isTrackingActive = true
        EventLog.log(this, "Service créé")
        fused = LocationServices.getFusedLocationProviderClient(this)
        val req = LocationRequest.Builder(INTERVAL).setPriority(Priority.PRIORITY_HIGH_ACCURACY).build()
        cb = object : LocationCallback() { override fun onLocationResult(r: LocationResult) { loc = r.lastLocation } }
        try { fused.requestLocationUpdates(req, cb, Looper.getMainLooper()) } catch(e: SecurityException) {}
        h.post(task)
    }
    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int { if (i?.action == ACTION_SEND_NOW) sendPos(); return START_STICKY }
    override fun onDestroy() { super.onDestroy(); Preferences.isTrackingActive = false; h.removeCallbacks(task); fused.removeLocationUpdates(cb) }
    override fun onBind(i: Intent?): IBinder? = null
    private fun sendPos() {
        val l = loc ?: return
        val msg = "LOKI1|${l.latitude}|${l.longitude}|${System.currentTimeMillis()}"
        val sms = SmsManager.getDefault()
        listOfNotNull(Preferences.phone1, Preferences.phone2).forEach { d ->
            try { sms.sendDataMessage(d, null, PORT.toShort(), msg.toByteArray()); EventLog.log(this, "→ SMS à $d") }
            catch(e: Exception) { EventLog.log(this, "✗ Échec à $d") }
        }
    }
    private fun notif(): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Suivi actif").setContentText("Envoi toutes les 60s").setSmallIcon(android.R.drawable.ic_menu_mylocation).setContentIntent(pi).setOngoing(true).build()
    }
    private fun chan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val c = NotificationChannel(CHANNEL_ID, "Suivi GPS", NotificationManager.IMPORTANCE_HIGH)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(c)
        }
    }
}
