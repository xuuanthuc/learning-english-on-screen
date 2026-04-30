package com.example.learning.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learning.models.LearningProgressEntity
import com.example.learning.models.WordDao
import com.example.learning.models.WordEntity
import com.example.learning.models.WordFull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordViewModel(private val dao: WordDao) : ViewModel() {
    val countB1 = dao.observeWordCount("B1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val countB2 = dao.observeWordCount("B2")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val countC1 = dao.observeWordCount("C1")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val countC2 = dao.observeWordCount("C2")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val learnedCount = dao.learnedWordCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val currentWord = MutableStateFlow<WordFull?>(null)
    suspend fun getNextWord() {
        val w = dao.getNextWord(System.currentTimeMillis())
        val f = dao.getNextWordFull(w?.word ?: "")
        currentWord.value = f
    }

    fun saveCurrentWordStatus(word: WordFull) {
        viewModelScope.launch {
            dao.insertProgress(
                LearningProgressEntity(
                    word = word.word.word,
                    status = 1,
                    lastSeen = System.currentTimeMillis()
                )
            )
        }
    }

    fun scheduleReDisplay(word: WordFull) {
        viewModelScope.launch {
            dao.updateLearningProgressStatus(
                word = word.word.word,
                status = 3,
                lastSeen = System.currentTimeMillis(),
                nextShowTime = System.currentTimeMillis() + 24 * 60 * 60 * 1000
            )
        }

    }

    fun neverShowAgain(word: WordFull) {
        viewModelScope.launch {

            dao.updateLearningProgressStatus(
                word = word.word.word,
                status = 2,
                lastSeen = System.currentTimeMillis(),
                nextShowTime = null
            )
        }
    }
}