package com.example.learning.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface WordDao {
    @Transaction
    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordFull>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LearningProgressEntity)
    @Query("SELECT * FROM learning_progress WHERE word = :word")
    suspend fun getProgress(word: String): LearningProgressEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhonetics(phonetics: List<PhoneticEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeaning(meaningEntity: MeaningEntity) : Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinitions(defs: List<DefinitionEntity>)
}