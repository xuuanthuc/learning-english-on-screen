package com.example.learning.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "definitions",
    foreignKeys = [
        ForeignKey(
            entity = MeaningEntity::class,
            parentColumns = ["id"],
            childColumns = ["meaningId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meaningId")]
)
data class DefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val meaningId: Long,
    val definition: String?,
    val example: String?
)