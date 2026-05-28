package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.TaskDatabase
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling all pending reminders...")
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = TaskDatabase.getDatabase(context)
                    val tasks = db.taskDao().getAllTasks().first() // Get current snapshot of all tasks
                    
                    var rescheduledCount = 0
                    for (task in tasks) {
                        if (task.isReminderEnabled && !task.isCompleted) {
                            NotificationHelper.scheduleAlarm(context, task)
                            rescheduledCount++
                        }
                    }
                    Log.d("BootReceiver", "Rescheduled $rescheduledCount tasks successfully.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling tasks during boot: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
