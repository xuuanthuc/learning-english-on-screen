package com.example.learning.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WordEntity::class,
        PhoneticEntity::class,
        MeaningEntity::class,
        DefinitionEntity::class,
        LearningProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}