package com.kidzie.poc_aidl.watchdog

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WatchdogService : Service() {

    private val watchDogScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        startForegroundNotification()
        startLoop()
        return START_STICKY
    }

    //TODO("Disable this when using custom AOSP")
    private fun startForegroundNotification() {
        val channelId = "WATCHDOG_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Edge Watchdog Service",
                NotificationManager.IMPORTANCE_LOW // Low priority so it doesn't buzz your phone
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
            .setContentTitle("Edge POC Watchdog")
            .setContentText("Monitoring system daemons...")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard Android system icon
            .build()

        // Tell the OS this is a critical foreground daemon
        startForeground(1, notification)
    }
    private fun startLoop() {
        watchDogScope.launch {
            while (isActive) {
                Log.d("watchdog", "watchdog is live")
                delay(10000L) // 1 second
            }

        }
    }
}