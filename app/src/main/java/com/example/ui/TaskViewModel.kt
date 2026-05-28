package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Raw tasks flow
    val allTasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filter and Search states
    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered tasks flow
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        _selectedFilter,
        _searchQuery
    ) { tasks, filter, query ->
        var result = tasks

        // Apply Category/Status filter
        result = when (filter) {
            "Today" -> result.filter { it.category == "Today" && !it.isCompleted }
            "Tomorrow" -> result.filter { it.category == "Tomorrow" && !it.isCompleted }
            "Daily" -> result.filter { it.category == "Daily" && !it.isCompleted }
            "Completed" -> result.filter { it.isCompleted }
            else -> result // "All"
        }

        // Apply Search query filter
        if (query.isNotEmpty()) {
            result = result.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statistics structure
    data class TaskStats(
        val total: Int = 0,
        val completed: Int = 0,
        val pending: Int = 0
    )

    val statistics: StateFlow<TaskStats> = allTasks.map { tasks ->
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed
        TaskStats(total, completed, pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    init {
        // Initialize Notification Channels on application launch
        NotificationHelper.createNotificationChannel(context)
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            val insertedId = repository.insertTask(task)
            val savedTask = task.copy(id = insertedId.toInt())
            if (savedTask.isReminderEnabled && !savedTask.isCompleted) {
                NotificationHelper.scheduleAlarm(context, savedTask)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
            if (task.isReminderEnabled && !task.isCompleted) {
                NotificationHelper.scheduleAlarm(context, task)
            } else {
                NotificationHelper.cancelAlarm(context, task.id)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            NotificationHelper.cancelAlarm(context, task.id)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updatedTask)
            if (updatedTask.isCompleted) {
                NotificationHelper.cancelAlarm(context, task.id)
            } else if (updatedTask.isReminderEnabled) {
                NotificationHelper.scheduleAlarm(context, updatedTask)
            }
        }
    }

    fun toggleReminder(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isReminderEnabled = !task.isReminderEnabled)
            repository.updateTask(updatedTask)
            if (updatedTask.isReminderEnabled && !updatedTask.isCompleted) {
                NotificationHelper.scheduleAlarm(context, updatedTask)
            } else {
                NotificationHelper.cancelAlarm(context, task.id)
            }
        }
    }
}

class TaskViewModelFactory(
    private val application: Application,
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
