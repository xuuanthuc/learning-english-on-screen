package com.example.learning.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Transaction
    @Query("SELECT * FROM words")
    suspend fun getAllWords(): List<WordFull>

    @Query("SELECT * FROM learning_progress")
    fun getHistory(): Flow<List<LearningProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LearningProgressEntity)

    @Query("SELECT * FROM learning_progress WHERE word = :word")
    suspend fun getProgress(word: String): LearningProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhonetics(phonetics: List<PhoneticEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeaning(meaningEntity: MeaningEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDefinitions(defs: List<DefinitionEntity>)

    @Query("SELECT COUNT(*) FROM words WHERE level = :level")
    fun observeWordCount(level: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM learning_progress")
    fun learnedWordCount(): Flow<Int>

    @Query(
        """
        SELECT w.* FROM words w
    LEFT JOIN learning_progress p ON w.word = p.word
    WHERE 
        (
            p.word IS NULL
            OR (p.status = 3 AND p.nextShowTime <= :now)
        )
        AND w.level IN (:levels)
    ORDER BY 
        CASE 
            WHEN p.status = 3 THEN 0
            ELSE 1
        END,
        RANDOM()
    LIMIT 1
    """
    )
    suspend fun getNextWord(now: Long,levels: List<String>): WordEntity?

    @Transaction
    @Query("SELECT * FROM words WHERE word = :word LIMIT 1")
    suspend fun getNextWordFull(word: String): WordFull?

    @Query("""
    UPDATE learning_progress 
    SET status = :status,
        lastSeen = :lastSeen,
        nextShowTime = :nextShowTime
    WHERE word = :word
""")
    suspend fun updateLearningProgressStatus(
        word: String,
        status: Int,
        lastSeen: Long,
        nextShowTime: Long?
    )

    @Query("DELETE FROM learning_progress WHERE word = :word")
    suspend fun deleteProgress(word: String)

    @Query("DELETE FROM learning_progress")
    suspend fun resetAll()
}