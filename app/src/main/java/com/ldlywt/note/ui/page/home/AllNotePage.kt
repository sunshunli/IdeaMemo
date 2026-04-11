package com.ldlywt.note.ui.page.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ldlywt.note.R
import com.ldlywt.note.bean.NoteShowBean
import com.ldlywt.note.component.NoteCard
import com.ldlywt.note.component.RYScaffold
import com.ldlywt.note.state.NoteState
import com.ldlywt.note.ui.page.LocalMemosState
import com.ldlywt.note.ui.page.SortTime
import com.ldlywt.note.ui.page.input.ChatInputDialog
import com.ldlywt.note.ui.page.router.Screen
import com.ldlywt.note.utils.FirstTimeWarmDialog
import com.ldlywt.note.utils.SettingsPreferences
import com.ldlywt.note.utils.SharedPreferencesUtils
import com.ldlywt.note.utils.lunchMain
import com.ldlywt.note.utils.str
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AllNotesPage(
    navController: NavHostController,
    hideBottomNavBar: ((Boolean) -> Unit)
) {
    val noteState: NoteState = LocalMemosState.current
    var showWarnDialog by rememberSaveable { mutableStateOf(false) }
    var showInputDialog by rememberSaveable { mutableStateOf(false) }
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }
    var parentNoteForComment by rememberSaveable { mutableStateOf<NoteShowBean?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val sortTime by SharedPreferencesUtils.sortTime.collectAsState(SortTime.UPDATE_TIME_DESC)
    var lastScrolledSortTime by rememberSaveable { mutableStateOf<SortTime?>(null) }

    // Ensure list stays at the top when sorting changes.
    // We trigger this when sortTime changes OR when notes update, 
    // to counter LazyColumn's attempts to maintain scroll position by key.
    LaunchedEffect(sortTime, noteState.notes) {
        if (lastScrolledSortTime != sortTime) {
            listState.scrollToItem(0)
            if (noteState.notes.isNotEmpty()) {
                lastScrolledSortTime = sortTime
            }
        }
    }

    LaunchedEffect(Unit) {
        showWarnDialog = SettingsPreferences.firstLaunch.first()
    }

    RYScaffold(
        title = R.string.all_note.str, navController = null,
        actions = {
            Toolbar(navController, dateRangeBlock = {
                showDateRangePicker = true
            }, onSortChanged = {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            })
        },
        floatingActionButton = {
            if (!showInputDialog) {
                FloatingActionButton(onClick = {
                    hideBottomNavBar.invoke(true)
                    parentNoteForComment = null
                    showInputDialog = true
                }, modifier = Modifier.padding(end = 16.dp, bottom = 32.dp)) {
                    Icon(
                        Icons.Rounded.Edit, stringResource(R.string.edit)
                    )
                }
            }
        },
    ) {

        Box {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = noteState.notes.size,
                    key = { noteState.notes[it].note.noteId }
                ) { index ->
                    NoteCard(
                        noteShowBean = noteState.notes[index],
                        navHostController = navController,
                        onCommentClick = {
                            parentNoteForComment = it
                            hideBottomNavBar.invoke(true)
                            showInputDialog = true
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            if (showInputDialog) {
                BackHandler(enabled = true) {
                    hideBottomNavBar.invoke(false)
                    showInputDialog = false
                    parentNoteForComment = null
                }
            }

            ChatInputDialog(
                isShow = showInputDialog,
                parentNote = parentNoteForComment,
                modifier =
                Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            ) {
                hideBottomNavBar.invoke(false)
                showInputDialog = false
                parentNoteForComment = null
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

    if (showWarnDialog) {
        FirstTimeWarmDialog {
            lunchMain {
                SettingsPreferences.changeFirstLaunch(false)
                showWarnDialog = false
            }
        }
    }

    if (showDateRangePicker) {
        ModernDateRangePicker(
            onDismissRequest = { showDateRangePicker = false },
            onConfirm = { startTime, endTime ->
                navController.navigate(
                    Screen.DateRangePage(
                        startTime = startTime,
                        endTime = endTime
                    )
                )
                showDateRangePicker = false
            }
        )
    }

}

@Composable
private fun Toolbar(
    navController: NavHostController,
    dateRangeBlock: () -> Unit,
    onSortChanged: () -> Unit
) {
    IconButton(onClick = dateRangeBlock) {
        Icon(
            contentDescription = R.string.date_range.str,
            imageVector = Icons.Outlined.AvTimer,
            tint = SaltTheme.colors.text
        )
    }
    IconButton(
        onClick = {
            navController.navigate(route = Screen.TagList) {
                launchSingleTop = true
            }
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.Tag,
            contentDescription = R.string.tag.str,
            tint = SaltTheme.colors.text
        )
    }

    IconButton(
        onClick = {
            navController.navigate(route = Screen.Search) {
                launchSingleTop = true
            }
        },
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = R.string.search_hint.str,
            tint = SaltTheme.colors.text
        )
    }

    SortFilterMenu(
        onSortChanged = {
            onSortChanged()
        }
    )
}

@Composable
fun SortFilterMenu(onSortChanged: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val sortTime by SharedPreferencesUtils.sortTime.collectAsState(SortTime.UPDATE_TIME_DESC)
    val scope = rememberCoroutineScope()

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.FilterList,
                contentDescription = "sort",
                tint = SaltTheme.colors.text
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SaltTheme.colors.popup)
        ) {
            SortMenuItem(
                text = stringResource(R.string.update_time_desc),
                selected = sortTime == SortTime.UPDATE_TIME_DESC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_DESC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
            SortMenuItem(
                text = stringResource(R.string.update_time_asc),
                selected = sortTime == SortTime.UPDATE_TIME_ASC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_ASC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
            SortMenuItem(
                text = stringResource(R.string.create_time_desc),
                selected = sortTime == SortTime.CREATE_TIME_DESC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_DESC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
            SortMenuItem(
                text = stringResource(R.string.create_time_asc),
                selected = sortTime == SortTime.CREATE_TIME_ASC,
                onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_ASC)
                        expanded = false
                        onSortChanged()
                    }
                }
            )
        }
    }
}

@Composable
fun SortMenuItem(text: String, selected: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = text,
                    color = if (selected) SaltTheme.colors.highlight else SaltTheme.colors.text,
                    fontSize = 14.sp
                )
                if (selected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = SaltTheme.colors.highlight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDateRangePicker(
    onDismissRequest: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    val dateRangePickerState = rememberDateRangePickerState()

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        // end is the start of the last selected day (00:00:00 UTC).
                        // Add 24 hours (minus 1ms) to include the entire last day up to 23:59:59.999.
                        onConfirm(start, end + 86399999L)
                    }
                },
                enabled = dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = SaltTheme.colors.background,
        )
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            modifier = Modifier.height(height = 500.dp),
            title = {
                Text(
                    text = stringResource(R.string.select_date),
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                Text(
                    text = dateRangePickerState.displayMode.toString(),
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
            },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = SaltTheme.colors.background,
                titleContentColor = SaltTheme.colors.text,
                headlineContentColor = SaltTheme.colors.text,
                selectedDayContainerColor = SaltTheme.colors.highlight,
                dayContentColor = SaltTheme.colors.text,
                selectedDayContentColor = Color.White,
                todayContentColor = SaltTheme.colors.highlight,
                dayInSelectionRangeContainerColor = SaltTheme.colors.highlight.copy(alpha = 0.15f)
            )
        )
    }
}
