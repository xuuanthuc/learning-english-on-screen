package com.example.learning.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.learning.models.LearningProgressEntity
import com.example.learning.models.WordDao
import com.example.learning.models.WordEntity
import com.example.learning.models.WordFull
import com.example.learning.tools.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordViewModel(private val dao: WordDao,private val repo: SettingsRepository) : ViewModel() {
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
    val historyFlow = dao.getHistory()
    val levelsFlow = repo.levelsFlow
    val repeatEnabled = repo.repeatEnabledFlow

    suspend fun getNextWord() {
        var levels = repo.levelsFlow.first()
        Log.d("SEE DATA", "$levels")
        if (levels.isEmpty()) {
            repo.saveLevels(listOf("B1","B2","C1","C2"))
            levels = listOf("B1","B2","C1","C2")
        }

        val w = dao.getNextWord(System.currentTimeMillis(), levels)
        val f = dao.getNextWordFull(w?.word ?: "")
        currentWord.value = f
    }

    fun updateLevels(levels: List<String>) {
        viewModelScope.launch {
            repo.saveLevels(levels)
        }
    }

    fun updateRepeat(enabled: Boolean) {
        viewModelScope.launch {
            repo.setRepeatEnabled(enabled)
        }
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

    fun updateHistoryStatus(word: String, status: Int, time: Long?) {
        viewModelScope.launch {
            dao.updateLearningProgressStatus(
                word = word,
                status = status,
                lastSeen = System.currentTimeMillis(),
                nextShowTime = time
            )
        }
    }

    fun deleteFromHistory(word: String) {
        viewModelScope.launch {
            dao.deleteProgress(word)
        }
    }
}