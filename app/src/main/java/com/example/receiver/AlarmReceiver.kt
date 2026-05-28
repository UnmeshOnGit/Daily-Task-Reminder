package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.Task
import com.example.data.TaskDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val desc = intent.getStringExtra("TASK_DESC") ?: ""
        val category = intent.getStringExtra("TASK_CATEGORY") ?: ""
        val repeatWeekly = intent.getBooleanExtra("TASK_REPEAT_WEEKLY", false)

        Log.d("AlarmReceiver", "Received alarm for task ID $taskId, title: $title")

        if (taskId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = TaskDatabase.getDatabase(context)
                val task = db.taskDao().getTaskById(taskId)

                if (task != null && task.isReminderEnabled && !task.isCompleted) {
                    // Display Notification
                    NotificationHelper.triggerNotification(context, taskId, title, desc)

                    // Auto reschedule for Daily or Repeat Weekly
                    if (task.category == "Daily") {
                        val nextTask = updateTaskDate(task, 1)
                        db.taskDao().updateTask(nextTask)
                        NotificationHelper.scheduleAlarm(context, nextTask)
                        Log.d("AlarmReceiver", "Rescheduled Daily task $taskId to next day date: ${nextTask.date}")
                    } else if (task.repeatWeekly) {
                        val nextTask = updateTaskDate(task, 7)
                        db.taskDao().updateTask(nextTask)
                        NotificationHelper.scheduleAlarm(context, nextTask)
                        Log.d("AlarmReceiver", "Rescheduled Weekly task $taskId to next week date: ${nextTask.date}")
                    }
                } else {
                    Log.d("AlarmReceiver", "Task not found, disabled or already completed. Skipping notification.")
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error processing alarm broadcast: ${e.message}", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun updateTaskDate(task: Task, daysToAdd: Int): Task {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        try {
            val date = sdf.parse(task.date)
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Error parsing date: ${task.date}")
        }
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        val nextDateStr = sdf.format(calendar.time)
        return task.copy(date = nextDateStr)
    }
}
