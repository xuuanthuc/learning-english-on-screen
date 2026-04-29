package com.example.learning
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ScreenReceiverService : Service() {

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_ON) {
                context?.let {
                    val overlayIntent = Intent(it, AlarmOverlayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    it.startActivity(overlayIntent)

                    val serviceIntent = Intent(it, ScreenReceiverService::class.java).apply {
                        action = ACTION_REFRESH_NOTIFICATION
                    }
                    ContextCompat.startForegroundService(it, serviceIntent)
                }
            }
        }
    }

    companion object {
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_REFRESH_NOTIFICATION = "ACTION_REFRESH_NOTIFICATION"
    }

    override fun onCreate() {
        super.onCreate()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
        startForegroundService()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "SCREEN_MONITOR_CHANNEL"
            val channelName = "Màn hình khóa Monitor"
            val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Dùng để lắng nghe sự kiện mở màn hình"
            }
            service.createNotificationChannel(channel)
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val stopIntent = Intent(this, ScreenReceiverService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel()
        val channelId = "SCREEN_MONITOR_CHANNEL"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("App đang chạy")
            .setContentText("Đang lắng nghe sự kiện màn hình...")
            .setSmallIcon(android.R.drawable.star_on)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Tắt tính năng", stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            startForeground(
                1,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_REFRESH_NOTIFICATION -> {
                startForegroundService()
            }

            ACTION_STOP_SERVICE -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}