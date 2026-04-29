package com.example.learning

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { startScreenService() }) {
                        Text("Bật tính năng hiển thị khi mở khóa")
                    }

                    Button(onClick = { stopScreenService() }) {
                        Text("Tắt tính năng hiển thị khi mở khóa")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startScreenService() {
        // 1. Kiểm tra xem người dùng đã cấp quyền hiển thị đè (Overlay) chưa
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                return // Dừng lại để đợi người dùng cho phép đã
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            // Nếu chưa, mở màn hình Cài đặt để họ cấp quyền
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            startActivity(intent)
            return
        }

        // 2. Nếu đã có quyền, khởi chạy Foreground Service
        val serviceIntent = Intent(this, ScreenReceiverService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun stopScreenService() {
        val serviceIntent = Intent(this, ScreenReceiverService::class.java)
        stopService(serviceIntent) // Lệnh này sẽ kích hoạt onDestroy() trong Service
    }
}
