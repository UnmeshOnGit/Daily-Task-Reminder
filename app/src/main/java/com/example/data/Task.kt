package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String, // "Today", "Tomorrow", "Daily", "Custom Date"
    val date: String, // String representation of the date: YYYY-MM-DD
    val time: String, // String representation of the time: HH:MM
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = true,
    val priority: String = "Medium", // "Low", "Medium", "High"
    val repeatWeekly: Boolean = false
) : Serializable
