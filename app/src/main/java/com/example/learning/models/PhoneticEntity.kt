package com.example.learning.models
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phonetics",
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
data class PhoneticEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordId: String,
    val text: String?,
    val audio: String?,
    val sourceUrl: String?,
    val licenseName: String?,
    val licenseUrl: String?
)