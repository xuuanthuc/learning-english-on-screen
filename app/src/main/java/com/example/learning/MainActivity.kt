package com.example.learning

import android.content.Context
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
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.room.Room
import com.example.learning.models.AppDatabase
import com.example.learning.models.DefinitionEntity
import com.example.learning.models.MeaningEntity
import com.example.learning.models.PhoneticEntity
import com.example.learning.models.WordData
import com.example.learning.models.WordEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    lateinit var db: AppDatabase
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        connectDatabase(applicationContext)
        setContent {
            val context = LocalContext.current
            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { importWordsFromJson(context) }) {
                        Text("Build database")
                    }

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

    private fun connectDatabase(context: Context) {
        db = Room.databaseBuilder(
            context, AppDatabase::class.java, "word-db"
        ).fallbackToDestructiveMigration(true).build()
    }

    fun importWordsFromJson(
        context: Context
    ) {
        val dao = db.wordDao()
        val paths = listOf("B1", "B2", "C1", "C2")

        paths.forEach { p ->
            val json = context.assets.open("cefr_english_${p}.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : com.google.gson.reflect.TypeToken<List<WordData>>() {}.type
            val words: List<WordData> = Gson().fromJson(json, type)

            CoroutineScope(Dispatchers.IO).launch {
                words.forEach { word ->
                    dao.insertWord(
                        WordEntity(
                            word = word.word,
                            vietnamese = word.vietnamese,
                            phonetic = word.phonetic,
                            level = p
                        )
                    )

                    val phonetics = word.phonetics.orEmpty().map {
                        PhoneticEntity(
                            wordId = word.word,
                            text = it.text,
                            audio = it.audio,
                            sourceUrl = it.sourceUrl,
                            licenseName = it.license?.name,
                            licenseUrl = it.license?.url
                        )
                    }
                    dao.insertPhonetics(phonetics)

                    word.meanings.orEmpty().forEach { meaning ->
                        val meaningId = dao.insertMeaning(
                            MeaningEntity(
                                wordId = word.word,
                                partOfSpeech = meaning.partOfSpeech
                            )
                        )

                        val definitions = meaning.definitions.orEmpty().map {
                            DefinitionEntity(
                                meaningId = meaningId,
                                definition = it.definition,
                                example = it.example
                            )
                        }

                        dao.insertDefinitions(definitions)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startScreenService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            // Nếu chưa, mở màn hình Cài đặt để họ cấp quyền
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri()
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
