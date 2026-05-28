package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun updateCompletedStatus(id: Int, isCompleted: Boolean) {
        taskDao.updateCompletedStatus(id, isCompleted)
    }

    suspend fun updateReminderStatus(id: Int, isReminderEnabled: Boolean) {
        taskDao.updateReminderStatus(id, isReminderEnabled)
    }
}
