package com.example.learning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.learning.models.AppDatabase
import com.example.learning.models.LearningProgressEntity
import com.example.learning.tools.SettingsRepository
import com.example.learning.viewmodels.WordViewModel

class HistoryActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = SettingsRepository(applicationContext)
        val db = Room.databaseBuilder(
            applicationContext, AppDatabase::class.java, "word-db"
        ).fallbackToDestructiveMigration(true).build()

        setContent {
            val viewModel = remember {
                WordViewModel(db.wordDao(), repo)
            }
            val list by viewModel.historyFlow.collectAsState(initial = emptyList())
            var searchText by remember { mutableStateOf("") }

            val filteredList = list.filter {
                it.word.contains(searchText, ignoreCase = true)
            }

            val context = LocalContext.current

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Learned words") }, navigationIcon = {
                            IconButton(onClick = {
                                (context as? HistoryActivity)?.finish()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        })
                    }) { padding ->

                    Column(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize()
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            label = { Text("Search word") },
                            singleLine = true
                        )

                        LazyColumn {
                            items(filteredList) { item ->
                                HistoryItem(item = item, onReview = {
                                    viewModel.updateHistoryStatus(
                                        item.word,
                                        3,
                                        System.currentTimeMillis() + 24 * 60 * 60 * 1000
                                    )
                                }, onIgnore = {
                                    viewModel.updateHistoryStatus(
                                        item.word, 2, null
                                    )
                                }, onSeen = {
                                    viewModel.updateHistoryStatus(
                                        item.word, 1, null
                                    )
                                }, onDelete = {
                                    viewModel.deleteFromHistory(item.word)
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HistoryItem(
        item: LearningProgressEntity,
        onSeen: () -> Unit,
        onReview: () -> Unit,
        onIgnore: () -> Unit,
        onDelete: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    item.word, style = TextStyle(
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Status: " + statusToText(item.status), style = TextStyle(
                        fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                )
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onSeen, modifier = Modifier.border(
                        width = 1.dp, color = Color.Black, shape = RoundedCornerShape(30.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Set as seen",
                        tint = Color.Magenta
                    )
                }
                IconButton(
                    onClick = onReview, modifier = Modifier.border(
                        width = 1.dp, color = Color.Black, shape = RoundedCornerShape(30.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Set as show again",
                        tint = Color.Blue
                    )
                }
                IconButton(
                    onClick = onIgnore, modifier = Modifier.border(
                        width = 1.dp, color = Color.Black, shape = RoundedCornerShape(30.dp)
                    )
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Set as known",
                        tint = Color.Green
                    )
                }
                IconButton(
                    onClick = onDelete, modifier = Modifier.border(
                        width = 1.dp, color = Color.Black, shape = RoundedCornerShape(30.dp)
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Gray)
            ) { }
        }
    }

    fun statusToText(status: Int): String {
        return when (status) {
            0 -> "New"
            1 -> "Seen"
            2 -> "Known"
            3 -> "Remind"
            else -> "Unknown"
        }
    }
}