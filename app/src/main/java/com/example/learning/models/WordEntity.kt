package com.example.learning.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey val word: String,
    val phonetic: String?,
    val level: String?
)

data class WordFull(
    @Embedded val word: WordEntity,

    @Relation(
        parentColumn = "word",
        entityColumn = "wordId"
    )
    val phonetics: List<PhoneticEntity>,

    @Relation(
        parentColumn = "word",
        entity = MeaningEntity::class,
        entityColumn = "wordId"
    )
    val meanings: List<MeaningWithDefinitions>,

    @Relation(
        parentColumn = "word",
        entityColumn = "word"
    )
    val progress: LearningProgressEntity?
)