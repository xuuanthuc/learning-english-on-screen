package com.example.learning.models
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "learning_progress")
data class LearningProgressEntity(
    @PrimaryKey val word: String,
    // trạng thái
    val status: Int = 0,
    // 0 = NEW (chưa xem)
    // 1 = SEEN (đã vuốt)
    // 2 = KNOWN (đã biết)
    // 3 = REVIEW (muốn xem lại)
    val priority: Int = 0,
    // 0 = bình thường
    // 1 = thấp
    // 2 = cao
    val lastSeen: Long? = null,
    val nextShowTime: Long? = null
)