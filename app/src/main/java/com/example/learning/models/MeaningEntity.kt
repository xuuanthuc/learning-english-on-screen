package com.example.learning.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "meanings",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["word"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId")]
)
data class MeaningEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordId: String,
    val partOfSpeech: String?,
    val vietnamese: String?
)

data class MeaningWithDefinitions(
    @Embedded val meaning: MeaningEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "meaningId"
    )
    val definitions: List<DefinitionEntity>
)