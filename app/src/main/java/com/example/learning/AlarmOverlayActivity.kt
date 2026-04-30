package com.example.learning

import android.content.ClipData
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.room.Room
import com.example.learning.models.AppDatabase
import com.example.learning.viewmodels.WordViewModel
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.style.TextAlign
import java.util.Locale
import kotlin.math.abs
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.toClipEntry

class AlarmOverlayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            val context = LocalContext.current
            val db = Room.databaseBuilder(
                context, AppDatabase::class.java, "word-db"
            ).fallbackToDestructiveMigration(true).build()
            val viewModel = remember {
                WordViewModel(db.wordDao())
            }

            MyOverlayScreen(viewModel = viewModel, onDismiss = { finish() })
        }
    }
}


@Composable
fun MyOverlayScreen(viewModel: WordViewModel, onDismiss: () -> Unit) {

    LaunchedEffect(Unit) {
        viewModel.getNextWord()
    }

    val word by viewModel.currentWord.collectAsState()
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(word) {
        word?.let {
            viewModel.saveCurrentWordStatus(it)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.clickable {
                    val clipData =
                        ClipData.newPlainText(word?.word?.word ?: "", word?.word?.word ?: "")
                    scope.launch { clipboardManager.setClipEntry(clipData.toClipEntry()) }
                },
                text = word?.word?.word ?: "", style = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp),
            ) {

                word?.let {
                    Text(
                        modifier = Modifier
                            .border(width = 1.dp, color = Color.Gray, CircleShape)
                            .padding(4.dp), text = word?.word?.level ?: "", style = TextStyle(
                            fontSize = 11.sp,
                            color = Color.White,
                        )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                val phoneticText = word?.word?.phonetic?.takeIf { it.isNotBlank() }
                    ?: word?.phonetics?.firstOrNull { it.text.isNullOrBlank().not() }?.text

                phoneticText?.let {
                    Text(
                        text = it, style = TextStyle(
                            fontSize = 16.sp, color = Color.White, fontStyle = FontStyle.Italic
                        )
                    )
                }
            }


            Text(
                text = word?.word?.vietnamese ?: "", style = TextStyle(
                    fontSize = 30.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                val bottomFade = Brush.verticalGradient(
                    0f to Color.Black,
                    0.85f to Color.Black,
                    1f to Color.Transparent
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            drawRect(brush = bottomFade, blendMode = BlendMode.DstIn)
                        }
                        .verticalScroll(rememberScrollState())) {
                    word?.meanings?.forEach { m ->
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 30.dp),
                        ) {
                            Text(
                                modifier = Modifier
                                    .background(
                                        color = Color.White, shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(vertical = 2.dp, horizontal = 8.dp),
                                text = (m.meaning.partOfSpeech ?: "").replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                },
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }

                        m.definitions.forEachIndexed { dIndex, d ->
                            Row(
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 30.dp),
                            ) {
                                val definition = d.definition ?: ""
                                Text(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(20.dp)
                                        .border(width = 1.dp, color = Color.White, CircleShape)
                                        .padding(4.dp),
                                    text = (dIndex + 1).toString(),
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val clipData =
                                                ClipData.newPlainText(definition, definition)
                                            scope.launch { clipboardManager.setClipEntry(clipData.toClipEntry()) }
                                        },
                                    text = definition,
                                    style = TextStyle(
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            d.example?.let {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 60.dp,
                                            end = 30.dp,
                                            bottom = 6.dp
                                        ),
                                    text = ("• " + d.example),
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        color = Color.White,
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.weight(0.5f))
            CircleSwipeToDismiss(onDismiss = onDismiss)
            Row(
                modifier = Modifier
                    .padding(horizontal = 30.dp, vertical = 50.dp)
                    .height(45.dp)
                    .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(12.dp))
            ) {
                Button(
                    onClick = {
                        word?.let { viewModel.neverShowAgain(it) }
                        onDismiss()
                    },
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.1f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                ) {
                    Text(
                        text = "Never show again"
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(color = Color.White)
                )
                Button(
                    onClick = {
                        word?.let { viewModel.scheduleReDisplay(it) }
                        onDismiss()
                    },
                    shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.1f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                ) {
                    Text(
                        text = "Prioritize re-display"
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CircleSwipeToDismiss(
    onDismiss: () -> Unit, modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val size = 140.dp
    val knobSize = 84.dp
    val density = LocalDensity.current
    val radiusPx = with(density) { (size / 2 - knobSize / 2).toPx() }
    val offsetX = remember { Animatable(0f) }
    val currentDistance = abs(offsetX.value)
    val isTouchingEdge = currentDistance >= radiusPx * 0.85f
    val outerBorderColor by animateColorAsState(
        targetValue = if (isTouchingEdge) Color.White.copy(alpha = 0.05f) else Color.Transparent,
        label = "outerBorderColor"
    )

    Box(
        modifier = modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(outerBorderColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(knobSize)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(24.dp)) {
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = Color.White,
                    start = Offset(0f, 0f),
                    end = Offset(24.dp.toPx(), 24.dp.toPx()),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White,
                    start = Offset(24.dp.toPx(), 0f),
                    end = Offset(0f, 24.dp.toPx()),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        Box(
            modifier = Modifier
                .size(knobSize)
                .offset { IntOffset(offsetX.value.toInt(), 0) }
                .background(Color.White.copy(alpha = 0f), CircleShape)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val newX = (offsetX.value + delta).coerceIn(-radiusPx, radiusPx)
                            offsetX.snapTo(newX)
                        }
                    },
                    onDragStopped = {
                        if (currentDistance > radiusPx * 0.7f) {
                            onDismiss()
                        } else {
                            scope.launch {
                                offsetX.animateTo(0f)
                            }
                        }
                    })
        )
    }
}