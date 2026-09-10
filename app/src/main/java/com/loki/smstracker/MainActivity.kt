package com.loki.smstracker
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
class MainActivity : AppCompatActivity() {
    private lateinit var et1: EditText; private lateinit var et2: EditText
    private lateinit var btnStart: Button; private lateinit var btnStop: Button
    private lateinit var btnReq: Button; private lateinit var tvStatus: TextView
    private var map: MapView? = null
    private val perms = mutableListOf<String>().apply {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.SEND_SMS); add(Manifest.permission.RECEIVE_SMS)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()
    private val reqPerm = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){}
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); setContentView(R.layout.activity_main)
        Preferences.init(this)
        et1 = findViewById(R.id.etPhone1); et2 = findViewById(R.id.etPhone2)
        btnStart = findViewById(R.id.btnStart); btnStop = findViewById(R.id.btnStop)
        btnReq = findViewById(R.id.btnRequest); tvStatus = findViewById(R.id.tvStatus)
        et1.setText(Preferences.phone1); et2.setText(Preferences.phone2)
        map = MapView(this).apply { setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE); setMultiTouchControls(true); controller.setZoom(15.0) }
        findViewById<android.widget.FrameLayout>(R.id.mapContainer).addView(map)
        if (perms.any { ContextCompat.checkSelfPermission(this, it) != 0 }) reqPerm.launch(perms)
        btnStart.setOnClickListener { startSvc() }; btnStop.setOnClickListener { stopSvc() }; btnReq.setOnClickListener { sendNow() }
        updateUI()
    }
    private fun startSvc() {
        val p1 = et1.text.toString().trim(); val p2 = et2.text.toString().trim()
        if (p1.isEmpty() && p2.isEmpty()) { android.widget.Toast.makeText(this, "Saisis au moins un numéro", android.widget.Toast.LENGTH_SHORT).show(); return }
        Preferences.phone1 = p1; Preferences.phone2 = p2
        EventLog.log(this, "Suivi démarré — $p1 / $p2")
        val i = Intent(this, LocationTrackerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(i) else startService(i)
        updateUI()
    }
    private fun stopSvc() { EventLog.log(this, "Suivi arrêté"); stopService(Intent(this, LocationTrackerService::class.java)); updateUI() }
    private fun sendNow() {
        val i = Intent(this, LocationTrackerService::class.java); i.action = LocationTrackerService.ACTION_SEND_NOW; startService(i)
    }
    private fun updateUI() {
        val a = Preferences.isTrackingActive
        btnStart.isEnabled = !a; btnStop.isEnabled = a
        tvStatus.setText(if(a) R.string.tracking_active else R.string.tracking_stopped)
    }
    override fun onResume() { super.onResume(); map?.onResume(); updateUI() }
    override fun onPause() { super.onPause(); map?.onPause() }
}
