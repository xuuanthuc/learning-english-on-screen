package com.example.learning

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.room.Room
import androidx.room.withTransaction
import com.example.learning.models.AppDatabase
import com.example.learning.models.DefinitionEntity
import com.example.learning.models.MeaningEntity
import com.example.learning.models.PhoneticEntity
import com.example.learning.models.WordData
import com.example.learning.models.WordEntity
import com.example.learning.tools.SettingsRepository
import com.example.learning.viewmodels.WordViewModel
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
        val repo = SettingsRepository(applicationContext)
        setContent {
            val context = LocalContext.current
            val viewModel = remember {
                WordViewModel(db.wordDao(), repo)
            }
            val levels by viewModel.levelsFlow.collectAsState(initial = emptyList())
            val repeatEnabled by viewModel.repeatEnabled.collectAsState(initial = false)
            val hasAllPermission =
                hasNotificationPermission(context) && hasOverlayPermission(context)
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }

            LaunchedEffect(repeatEnabled) {
                if (repeatEnabled) {
                    startScreenService()
                } else {
                    stopScreenService()
                }
            }

            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WordsCount(viewModel, onUpdateDB = { importWordsFromJson(context) })

                    LevelFilter(levels, onChange = {
                        viewModel.updateLevels(it)
                    })
                    PermissionGate(
                        onRequestNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onRequestOverlay = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            context.startActivity(intent)
                        }
                    )
                    FeatureToggle(
                        repeatEnabled,
                        onToggle = {
                            viewModel.updateRepeat(it)
                            if (it) {
                                startScreenService()
                            } else {
                                stopScreenService()
                            }
                        },
                        hasPermissions = hasAllPermission
                    )
                    Button(onClick = {
                        val intent = Intent(context, HistoryActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Learned Words")
                    }

                    Button(onClick = {
                        val intent = Intent(context, AlarmOverlayActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Mở màn hình test")
                    }
                }
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun hasOverlayPermission(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
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
        CoroutineScope(Dispatchers.IO).launch {
            db.withTransaction {
                paths.forEach { p ->
                    val json = context.assets.open("cefr_english_${p}.json")
                        .bufferedReader()
                        .use { it.readText() }

                    val type = object : com.google.gson.reflect.TypeToken<List<WordData>>() {}.type
                    val words: List<WordData> = Gson().fromJson(json, type)

                    words.forEach { word ->
                        dao.insertWord(
                            WordEntity(
                                word = word.word,
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
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri()
            )
            startActivity(intent)
            return
        }

        val serviceIntent = Intent(this, ScreenReceiverService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun stopScreenService() {
        val serviceIntent = Intent(this, ScreenReceiverService::class.java)
        stopService(serviceIntent)
    }

    @Composable
    fun PermissionGate(
        onRequestNotification: () -> Unit,
        onRequestOverlay: () -> Unit
    ) {
        val context = LocalContext.current
        val hasNoti = hasNotificationPermission(context)
        val hasOverlay = hasOverlayPermission(context)

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(30.dp)
        ) {
            Text("Permissions required", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Notification permission", modifier = Modifier.weight(1f))
                if (hasNoti) {
                    Text("✅")
                } else {
                    Button(onClick = onRequestNotification) {
                        Text("Allow")
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Overlay permission", modifier = Modifier.weight(1f))
                if (hasOverlay) {
                    Text("✅")
                } else {
                    Button(onClick = onRequestOverlay) {
                        Text("Grant")
                    }
                }
            }
        }
    }
}

@Composable
fun WordsCount(viewModel: WordViewModel, onUpdateDB: () -> Unit) {
    val countB1 by viewModel.countB1.collectAsState()
    val countB2 by viewModel.countB2.collectAsState()
    val countC1 by viewModel.countC1.collectAsState()
    val countC2 by viewModel.countC2.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Total words",
            style = TextStyle(
                fontWeight = FontWeight.Bold
            )
        )
        Text(text = "Total B1 Words: $countB1")
        Text(text = "Total B2 words: $countB2")
        Text(text = "Total C1 words: $countC1")
        Text(text = "Total C2 words: $countC2")
        Text(text = "Total words learned: $learnedCount")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onUpdateDB) {
            Text("Update database")
        }
    }
}

@Composable
fun LevelFilter(
    selectedLevels: List<String>,
    onChange: (List<String>) -> Unit
) {
    val levels = listOf("B1", "B2", "C1", "C2")

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
    ) {

        Text(
            text = "Filter by level",
            style = TextStyle(
                fontWeight = FontWeight.Bold
            )
        )
        levels.forEach { level ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedLevels.contains(level),
                    onCheckedChange = { checked ->
                        val newList = selectedLevels.toMutableList()
                        if (checked) {
                            newList.add(level)
                        } else {
                            newList.remove(level)
                        }
                        onChange(newList)
                    }
                )
                Text(text = level)
            }
        }
    }
}

@Composable
fun FeatureToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    hasPermissions: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
    ) {
        Text(
            "Turn on/off the display feature on the lock screen.",
            style = TextStyle(
                fontWeight = FontWeight.Bold
            )
        )
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            enabled = hasPermissions
        )
    }
}
