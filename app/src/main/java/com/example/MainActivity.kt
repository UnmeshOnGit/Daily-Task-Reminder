package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.TaskDatabase
import com.example.data.TaskRepository
import com.example.ui.TaskApp
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room Database, DAO & Repository
        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())
        
        // Construct ViewModel via Companion Factory
        val viewModel: TaskViewModel by viewModels {
            TaskViewModelFactory(application, repository)
        }

        setContent {
            MyApplicationTheme {
                TaskApp(viewModel = viewModel)
            }
        }
    }
}

