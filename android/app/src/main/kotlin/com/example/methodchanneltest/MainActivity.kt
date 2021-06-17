package com.example.methodchanneltest

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
//import android.support.v7.app.AppCompatActivity
import android.util.Log
//import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : FlutterActivity() {

    private val REQUEST_CODE = 100
    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent

    var OVERLAY_PERMISSION_REQ_CODE = 1000

    fun checkPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        }
    }

    private val CHANNEL = "samples.flutter.dev/runAlarm"
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            // Note: this method is invoked on the main thread.
                call, result ->
            if (call.method == "runAlarm") {
                val resultInt = runAlarm()
                if (resultInt != -1) {
                    result.success(resultInt)
                } else {
                    result.error("UNAVAILABLE", "Battery level not available.", null)
                }
            } else {
                result.notImplemented()
            }
        }
    }
    private fun runAlarm(): Int {
        checkPermission();


        // Creating the pending intent to send to the BroadcastReceiver
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MyAlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Setting the specific time for the alarm manager to trigger the intent, in this example, the alarm is set to go off at 23:30, update the time according to your need
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 10)

        Log.d("MyApp", "通りました")
        // Starts the alarm manager
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            20000,
            10000,
            pendingIntent
        )

        return 1;
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancels the pendingIntent if it is no longer needed after this activity is destroyed.
        alarmManager.cancel(pendingIntent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d("debug", "SYSTEM_ALERT_WINDOW permission not granted...")
                // SYSTEM_ALERT_WINDOW permission not granted...
                // nothing to do !
            }
        }
    }
}