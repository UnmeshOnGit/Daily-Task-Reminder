package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Task
import com.example.notification.NotificationHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskApp(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("task_reminder_prefs", Context.MODE_PRIVATE) }
    
    // Onboarding launch control
    var showOnboarding by remember { 
        mutableStateOf(sharedPrefs.getBoolean("is_first_launch_v1", true)) 
    }

    if (showOnboarding) {
        OnboardingScreen(
            onFinished = {
                sharedPrefs.edit().putBoolean("is_first_launch_v1", false).apply()
                showOnboarding = false
            }
        )
    } else {
        MainDashboard(viewModel = viewModel)
    }
}

// --- ONBOARDING FEATURE ---
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Welcome to Daily Task Reminder",
                description = "Stay organized, set reminders, and build perfect daily habits effortlessly.",
                icon = Icons.Rounded.CheckCircle,
                tint = Color(0xFF0061A4)
            ),
            OnboardingPageData(
                title = "Never Miss a Thing",
                description = "Exact alarms trigger beautiful push notifications, keeping you on track even when the app is closed.",
                icon = Icons.Rounded.NotificationsActive,
                tint = Color(0xFF006A60)
            ),
            OnboardingPageData(
                title = "Habits & Routines",
                description = "Configure recurring daily tasks to repeat and build steady streaks day after day.",
                icon = Icons.Rounded.DateRange,
                tint = Color(0xFFBA1A1A)
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Skip Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinished) {
                    Text("Skip", color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.SemiBold)
                }
            }

            // Slide Content
            val currentPageData = pages[currentPage]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            color = currentPageData.tint.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentPageData.icon,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = currentPageData.tint
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = currentPageData.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentPageData.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom Navigation Footer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentPage) 12.dp else 8.dp)
                                .background(
                                    color = if (index == currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Control Button
                Button(
                    onClick = {
                        if (currentPage < pages.lastIndex) {
                            currentPage++
                        } else {
                            onFinished()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_action_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = if (currentPage == pages.lastIndex) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val tint: Color
)


// --- MAIN DASHBOARD SCREEN (HIGH DENSITY PLACEMENT) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statistics by viewModel.statistics.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    
    // Bottom bar active tab
    var selectedTab by remember { mutableStateOf("Tasks") } // "Tasks", "Calendar", "Stats"
    
    // Calendar variables
    var selectedCalendarDate by remember { mutableStateOf("") } // YYYY-MM-DD
    
    // Statistics detailed dialog state
    var showStatsDialog by remember { mutableStateOf(false) }

    // Trigger runtime permission request for Android 13+ Setup
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Reminders may not fire due to missing alert permissions.", Toast.LENGTH_LONG).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // HIGH DENSITY: Top App Bar matching the template's look & feel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // App Header Title Text
                    Column {
                        Text(
                            text = "Daily Task Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "High Density Dynamic Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp
                        )
                    }

                    // Sync & Alert Alarms Trigger Action Button
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                )
                                .clickable {
                                    // Refresh active scheduled reminders
                                    allTasks.forEach { task ->
                                        if (task.isReminderEnabled && !task.isCompleted) {
                                            NotificationHelper.scheduleAlarm(context, task)
                                        }
                                    }
                                    Toast.makeText(context, "Active reminders synchronized successfully", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Sync Alarms",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // HIGH DENSITY: Clean Statistics Grid Panel
                StatisticsHeader(
                    stats = statistics,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        },
        bottomBar = {
            // HIGH DENSITY: Visual Footer Navigation & FAB Layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .navigationBarsPadding()
            ) {
                // Bottom Tab Items row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tasks Tab Link
                    BottomNavItem(
                        label = "Tasks",
                        icon = if (selectedTab == "Tasks") Icons.Rounded.Home else Icons.Outlined.Home,
                        isActive = selectedTab == "Tasks",
                        onClick = {
                            selectedTab = "Tasks"
                            viewModel.setFilter("All")
                            selectedCalendarDate = ""
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Calendar Selector Tab Link
                    BottomNavItem(
                        label = "Calendar",
                        icon = if (selectedTab == "Calendar") Icons.Rounded.CalendarMonth else Icons.Outlined.CalendarMonth,
                        isActive = selectedTab == "Calendar",
                        onClick = {
                            selectedTab = "Calendar"
                            // Immediately launch datepicker tool
                            val calendar = Calendar.getInstance()
                            try {
                                val pickerActivity = context.findActivity() ?: context
                                android.app.DatePickerDialog(
                                    pickerActivity,
                                    { _, year, month, dayOfMonth ->
                                        val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                        selectedCalendarDate = formatted
                                        viewModel.setFilter("Custom Date")
                                        selectedTab = "Tasks" // return to task view showing filtered customs
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Unable to show calendar selector", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Stats Details Modal popup Link
                    BottomNavItem(
                        label = "Stats",
                        icon = if (selectedTab == "Stats") Icons.Rounded.BarChart else Icons.Outlined.BarChart,
                        isActive = selectedTab == "Stats",
                        onClick = {
                            showStatsDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            // Elevated Floating Action Button (FAB)
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showAddEditDialog = true
                },
                modifier = Modifier
                    .testTag("add_task_fab"),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Compact Search Bar Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_tasks_input"),
                placeholder = { Text("Search task lists...", fontSize = 14.sp) },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Rounded.Search, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.outline
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear Search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            // Horizontal Weekly Strip Calendar View
            WeeklyCalendarView(
                allTasks = allTasks,
                selectedDate = selectedCalendarDate,
                onDateSelected = { date ->
                    selectedCalendarDate = date
                    if (date.isNotEmpty()) {
                        viewModel.setFilter("Custom Date")
                    } else {
                        viewModel.setFilter("All")
                    }
                }
            )

            // Category Filter Chips Row
            FilterChipsRow(
                selectedFilter = if (selectedFilter == "Custom Date") "Custom Date" else selectedFilter,
                onFilterSelected = { filter ->
                    if (filter != "Custom Date") {
                        selectedCalendarDate = "" // Clear calendar selection
                    }
                    viewModel.setFilter(filter)
                }
            )

            // Task List Core Pane
            Box(
                modifier = Modifier.weight(1f)
            ) {
                val listToShow = remember(filteredTasks, selectedFilter, selectedCalendarDate) {
                    if (selectedFilter == "Custom Date" && selectedCalendarDate.isNotEmpty()) {
                        filteredTasks.filter { it.date == selectedCalendarDate }
                    } else {
                        filteredTasks
                    }
                }

                if (listToShow.isEmpty()) {
                    EmptyStateView(
                        filter = selectedFilter,
                        hasSearch = searchQuery.isNotEmpty()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp)
                    ) {
                        items(listToShow, key = { it.id }) { task ->
                            TaskItemRow(
                                task = task,
                                onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                onToggleReminder = { viewModel.toggleReminder(task) },
                                onEditClick = {
                                    taskToEdit = task
                                    showAddEditDialog = true
                                },
                                onDeleteClick = {
                                    viewModel.deleteTask(task)
                                    Toast.makeText(context, "Reminder task deleted", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Add/Edit dialog box
    if (showAddEditDialog) {
        AddEditTaskDialog(
            task = taskToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { task ->
                if (task.id == 0) {
                    viewModel.addTask(task)
                } else {
                    viewModel.updateTask(task)
                }
                showAddEditDialog = false
            }
        )
    }

    // Statistics breakdown modal Dialog block
    if (showStatsDialog) {
        StatsBreakdownDialog(
            stats = statistics,
            allTasks = allTasks,
            onDismiss = { showStatsDialog = false }
        )
    }
}

// Custom Bottom Nav Item helper
@Composable
fun BottomNavItem(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            fontSize = 10.sp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

// --- STATISTICS VIEWS (HIGH DENSITY STYLE) ---
@Composable
fun StatisticsHeader(
    stats: TaskViewModel.TaskStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp), // Highly rounded matching M3 rounded-3xl
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "TOTAL", 
                count = stats.total, 
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatItem(
                label = "DONE", 
                count = stats.completed, 
                color = MaterialTheme.colorScheme.secondary, // Teal green
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            StatItem(
                label = "LEFT", 
                count = stats.pending, 
                color = MaterialTheme.colorScheme.error, // Crimson red
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItem(
    label: String, 
    count: Int, 
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.outline,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = String.format("%02d", count),
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

// --- CALENDAR STRIP COMPONENT ---
@Composable
fun WeeklyCalendarView(
    allTasks: List<Task>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val calendarDays = remember {
        val list = mutableListOf<Date>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2) // Show relative preceding
        repeat(12) {
            list.add(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayNameFormat = remember { SimpleDateFormat("EEE", Locale.getDefault()) }
    val dayNumFormat = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthFormat.format(Calendar.getInstance().time).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )

            if (selectedDate.isNotEmpty()) {
                TextButton(
                    onClick = { onDateSelected("") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Clear Filter", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(calendarDays) { date ->
                val dateStr = dbDateFormat.format(date)
                val isSelected = dateStr == selectedDate
                val dayName = dayNameFormat.format(date)
                val dayNum = dayNumFormat.format(date)

                // Check pending tasks for this day
                val hasPendingTasks = remember(allTasks, dateStr) {
                    allTasks.any { it.date == dateStr && !it.isCompleted }
                }

                Card(
                    modifier = Modifier
                        .width(46.dp)
                        .height(64.dp)
                        .clickable {
                            if (isSelected) onDateSelected("") else onDateSelected(dateStr)
                        }
                        .testTag("calendar_day_$dateStr"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = if (isSelected) null else {
                        // Outline today
                        val todayStr = dbDateFormat.format(Calendar.getInstance().time)
                        if (dateStr == todayStr) {
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = dayNum,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Indicators indicator dot
                        if (hasPendingTasks) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- FILTER CHIPS ROW ---
@Composable
fun FilterChipsRow(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = remember { listOf("All", "Today", "Tomorrow", "Daily", "Completed") }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter
            
            // Custom rounded-lg button styled matching the design HTML specs
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onFilterSelected(filter) }
                    .testTag("filter_chip_$filter"),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary // bg-[#0061A4]
                } else {
                    MaterialTheme.colorScheme.primaryContainer // bg-[#D1E4FF]
                },
                contentColor = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary // white
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer // text-[#001D36]
                }
            ) {
                Text(
                    text = filter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Custom date indicator Pill tag
        if (selectedFilter == "Custom Date") {
            item {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onFilterSelected("All") },
                    color = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Custom Date", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// --- TASK LIST EMPTY STATE COMPONENT ---
@Composable
fun EmptyStateView(filter: String, hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasSearch) Icons.Rounded.SearchOff else Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val title = if (hasSearch) "No tasks found" else "All caught up"
        val desc = when {
            hasSearch -> "We couldn't find any tasks matching your keywords. Try editing your query."
            filter == "Completed" -> "You haven't completed any tasks yet. Tick your tasks on the home screen!"
            filter == "Daily" -> "Create recurring daily tasks to build robust self-improvement habits."
            else -> "Enjoy your peaceful day! Click the FAB below to declare exciting new reminders."
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// --- INDIVIDUAL TASK LIST ROW CARD (HIGH DENSITY PLACEMENT) ---
@Composable
fun TaskItemRow(
    task: Task,
    onCheckedChange: () -> Unit,
    onToggleReminder: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Priority colors matching our Crimson theme colors
    val accentColor = when (task.priority) {
        "High" -> MaterialTheme.colorScheme.error // Crimson Red (#BA1A1A)
        "Medium" -> Color(0xFFF2A104) // Dark amber
        else -> MaterialTheme.colorScheme.secondary // Teal Green
    }

    var showDetailDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)) // rounded-2xl
            .testTag("task_card_item_${task.id}")
            .alpha(if (task.isCompleted) 0.7f else 1f)
            .clickable { showDetailDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehindAccentBorder(enabled = task.priority == "High" && !task.isCompleted, color = accentColor)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task status Tick Checkbox with high density rounding
            IconButton(
                onClick = onCheckedChange,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("task_checkbox_completed_${task.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            color = if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (task.isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body text area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Priority Indicator Color Bullet
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accentColor, shape = CircleShape)
                    )

                    // Title
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Metadata badge and date Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category chip indicator badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                color = if (task.isCompleted) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (task.repeatWeekly) "${task.category} • Weekly" else task.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Time description text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "${task.time}  (${task.date})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // High density Actions Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Reminder alarm toggler icon
                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (task.isReminderEnabled) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                        contentDescription = "Toggle Reminder",
                        tint = if (task.isCompleted) {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        } else if (task.isReminderEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Edit Button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("task_edit_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = "Edit Task",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("task_delete_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }

    // Detail Pop up Dialog when clicking on a card row
    if (showDetailDialog) {
        TaskDetailsDialog(
            task = task,
            onDismiss = { showDetailDialog = false },
            onToggleCompleted = onCheckedChange,
            onEditClick = {
                showDetailDialog = false
                onEditClick()
            }
        )
    }
}

// Drawing helper extension for left accent border (High priority tasks)
fun Modifier.drawBehindAccentBorder(enabled: Boolean, color: Color): Modifier = if (enabled) {
    this.drawWithContent {
        drawContent()
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(12f, this.size.height)
        )
    }
} else this

// --- DETAILED TASK POPUP VIEW DIALOG ---
@Composable
fun TaskDetailsDialog(
    task: Task,
    onDismiss: () -> Unit,
    onToggleCompleted: () -> Unit,
    onEditClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Task Summary",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "No detailed description written.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Attributes List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Category", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(task.category, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Priority", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(task.priority, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scheduled Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(task.date, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Remind Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(task.time, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reminder Alarm", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = if (task.isReminderEnabled) "ACTIVE" else "MUTED",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("State Status", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = if (task.isCompleted) "COMPLETED" else "PENDING",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions Button footer row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onToggleCompleted()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (task.isCompleted) "Re-open Task" else "Complete Task")
                    }

                    Button(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Details")
                    }
                }
            }
        }
    }
}


// --- ADD/EDIT MODAL DIALOG SHEET (HIGH DENSITY STYLE) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    task: Task?, // Null represents creating a new one
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    val context = LocalContext.current
    
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    
    var category by remember { mutableStateOf(task?.category ?: "Today") }
    var priority by remember { mutableStateOf(task?.priority ?: "Medium") }
    
    var isReminderEnabled by remember { mutableStateOf(task?.isReminderEnabled ?: true) }
    var repeatWeekly by remember { mutableStateOf(task?.repeatWeekly ?: false) }

    // State controllers for selecting date and time
    val dbDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    var selectedDateStr by remember { 
        mutableStateOf(task?.date ?: dbDateFormat.format(Date())) 
    }
    var selectedTimeStr by remember {
        mutableStateOf(task?.time ?: timeFormat.format(Date()))
    }

    // Dropdowns visibility controllers
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp)
                .testTag("add_edit_task_panel"),
            shape = RoundedCornerShape(24.dp), // M3 Card Outline
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (task == null) "Schedule Reminder" else "Update Reminder Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title*", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Task Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Description (Optional)", fontSize = 13.sp) },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_task_desc_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category & Priority Dropdowns in a Side-By-Side Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Selection
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category", fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(onClick = { expandedCategory = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCategory = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            val categories = listOf("Today", "Tomorrow", "Daily", "Custom Date")
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        val cal = Calendar.getInstance()
                                        when (cat) {
                                            "Today" -> selectedDateStr = dbDateFormat.format(cal.time)
                                            "Tomorrow" -> {
                                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                                selectedDateStr = dbDateFormat.format(cal.time)
                                            }
                                        }
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    // Priority Selection
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = priority,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Priority", fontSize = 12.sp) },
                            trailingIcon = {
                                IconButton(onClick = { expandedPriority = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedPriority = true },
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = expandedPriority,
                            onDismissRequest = { expandedPriority = false }
                        ) {
                            listOf("Low", "Medium", "High").forEach { choice ->
                                DropdownMenuItem(
                                    text = { Text(choice) },
                                    onClick = {
                                        priority = choice
                                        expandedPriority = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Date & Time Picker Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date picker button setup
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            try {
                                val currentParsed = dbDateFormat.parse(selectedDateStr)
                                if (currentParsed != null) {
                                    calendar.time = currentParsed
                                }
                            } catch (e: Exception) {
                                // Ignore
                            }
                            try {
                                val pickerActivity = context.findActivity() ?: context
                                android.app.DatePickerDialog(
                                    pickerActivity,
                                    { _, year, month, dayOfMonth ->
                                        val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                        selectedDateStr = formatted
                                        if (category != "Daily") {
                                            category = "Custom Date"
                                        }
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Unable to show date selector", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = selectedDateStr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Time picker button setup
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            try {
                                val parts = selectedTimeStr.split(":")
                                if (parts.size == 2) {
                                    calendar.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                                    calendar.set(Calendar.MINUTE, parts[1].toInt())
                                }
                            } catch (e: Exception) {
                                // Ignore
                            }
                            try {
                                val pickerActivity = context.findActivity() ?: context
                                android.app.TimePickerDialog(
                                    pickerActivity,
                                    { _, hour, minute ->
                                        selectedTimeStr = String.format("%02d:%02d", hour, minute)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    true
                                ).show()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Unable to show time selector", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.AccessTime, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = selectedTimeStr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Options switches
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Reminder active switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                            Text("Activate alert notification", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it },
                            modifier = Modifier
                                .scale(0.85f)
                                .testTag("add_task_reminder_switch")
                        )
                    }

                    // Repeat weekly switch
                    if (category != "Daily") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Loop, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                Text("Repeat Weekly", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                            Switch(
                                checked = repeatWeekly,
                                onCheckedChange = { repeatWeekly = it },
                                modifier = Modifier
                                    .scale(0.85f)
                                    .testTag("add_task_week_repeat_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Bottom Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (title.trim().isEmpty()) {
                                Toast.makeText(context, "Please enter a task name", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val savedTask = Task(
                                id = task?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                category = category,
                                date = selectedDateStr,
                                time = selectedTimeStr,
                                isCompleted = task?.isCompleted ?: false,
                                isReminderEnabled = isReminderEnabled,
                                priority = priority,
                                repeatWeekly = if (category == "Daily") false else repeatWeekly
                            )
                            onSave(savedTask)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_task_submit_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Task", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- FULL BREAKDOWN STATISTICS POPUP GRAPHICS ---
@Composable
fun StatsBreakdownDialog(
    stats: TaskViewModel.TaskStats,
    allTasks: List<Task>,
    onDismiss: () -> Unit
) {
    val highPriorityCount = remember(allTasks) { allTasks.count { it.priority == "High" && !it.isCompleted } }
    val medPriorityCount = remember(allTasks) { allTasks.count { it.priority == "Medium" && !it.isCompleted } }
    val lowPriorityCount = remember(allTasks) { allTasks.count { it.priority == "Low" && !it.isCompleted } }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STATISTICS REPORT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close description")
                    }
                }

                // Completion Progress Bar Widget
                Column(modifier = Modifier.fillMaxWidth()) {
                    val progressRatio = if (stats.total > 0) stats.completed.toFloat() / stats.total else 0f
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Completion Streak Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(progressRatio * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = progressRatio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Detailed state counts
                Text(text = "Priority Grid Pending", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Crimson High Priority card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("High Urgent", fontSize = 10.sp, color = Color(0xFF410002), fontWeight = FontWeight.Bold)
                            Text(text = String.format("%02d", highPriorityCount), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFBA1A1A))
                        }
                    }

                    // Medium card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1C5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Medium Regular", fontSize = 10.sp, color = Color(0xFF632B00), fontWeight = FontWeight.Bold)
                            Text(text = String.format("%02d", medPriorityCount), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFFE56B00))
                        }
                    }

                    // Low card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFCCE8E1)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Low Flexible", fontSize = 10.sp, color = Color(0xFF00201C), fontWeight = FontWeight.Bold)
                            Text(text = String.format("%02d", lowPriorityCount), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF006A60))
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                // Simple Tip box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (stats.pending > 0) "Finish your pending tasks on time today to guarantee a superb daily daily streak!" else "Amazing! All tasks completed today. Treat yourself to a nice relaxing self-reward break!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got It")
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    return null
}
