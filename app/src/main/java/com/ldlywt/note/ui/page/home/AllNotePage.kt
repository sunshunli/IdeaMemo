package com.ldlywt.note.ui.page.home

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ldlywt.note.R
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
import java.time.LocalDate
import java.time.ZoneId

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun AllNotesPage(
    navController: NavHostController,
    hideBottomNavBar: ((Boolean) -> Unit)
) {
    val noteState: NoteState = LocalMemosState.current
    var openFilterBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showWarnDialog by rememberSaveable { mutableStateOf(false) }
    var showInputDialog by rememberSaveable { mutableStateOf(false) }
    var showCustomTimePicker by rememberSaveable { mutableStateOf(false) }
    val maxLine by SettingsPreferences.cardMaxLine.collectAsState(SettingsPreferences.CardMaxLineMode.MAX_LINE)
    LaunchedEffect(Unit) {
        showWarnDialog = SettingsPreferences.firstLaunch.first()
    }

    RYScaffold(
        title = R.string.all_note.str, navController = null,
        actions = {
            toolbar(navController, filterBlock = {
                openFilterBottomSheet = true
            }, dateRangeBlock = {
                showCustomTimePicker = true
            })
        },
        floatingActionButton = {
            if (!showInputDialog) {
                FloatingActionButton(onClick = {
                    hideBottomNavBar.invoke(true)
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
                Modifier
                    .fillMaxSize()
            ) {
                items(count = noteState.notes.size, key = { it }) { index ->
                    NoteCard(noteShowBean = noteState.notes[index], navController, maxLine = maxLine.line)
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            if (showInputDialog) {
                BackHandler(enabled = true) {
                    hideBottomNavBar.invoke(false)
                    showInputDialog = false
                }
            }

            ChatInputDialog(
                isShow = showInputDialog,
                modifier =
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            ) {
                hideBottomNavBar.invoke(false)
                showInputDialog = false
            }
        }
    }

    HomeFilterBottomSheet(
        show = openFilterBottomSheet,
        onDismissRequest = {
            openFilterBottomSheet = false
        })

    if (showWarnDialog) {
        FirstTimeWarmDialog {
            lunchMain {
                SettingsPreferences.changeFirstLaunch(false)
                showWarnDialog = false
            }
        }
    }

    // 添加自定义时间选择对话框
    if (showCustomTimePicker) {
        CustomTimePickerDialog(
            onDismissRequest = { showCustomTimePicker = false },
            onConfirm = { startTime, endTime ->
                lunchMain {
                    navController.navigate(Screen.DateRangePage(startTime = startTime, endTime = endTime))
                }
                showCustomTimePicker = false
            }
        )
    }

}

@Composable
private fun toolbar(navController: NavHostController, filterBlock: () -> Unit, dateRangeBlock: () -> Unit) {
    IconButton(
        onClick = {
            dateRangeBlock()
        }
    ) {
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
            imageVector = Icons.Outlined.Search, contentDescription = R.string.search_hint.str, tint = SaltTheme.colors.text
        )
    }

    IconButton(
        onClick = {
            filterBlock()
        },
    ) {
        Icon(
            imageVector = Icons.Outlined.FilterList, contentDescription = "sort", tint = SaltTheme.colors.text
        )
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFilterBottomSheet(show: Boolean, onDismissRequest: () -> Unit) {

    val sortTime = SharedPreferencesUtils.sortTime.collectAsState(SortTime.UPDATE_TIME_DESC)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    if (show) {
        ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth()) {
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_DESC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.update_time_desc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.UPDATE_TIME_DESC, null)
                    }
                }
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.UPDATE_TIME_ASC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.update_time_asc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.UPDATE_TIME_ASC, null)
                    }
                }
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_DESC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.create_time_desc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.CREATE_TIME_DESC, null)
                    }
                }
                TextButton(onClick = {
                    scope.launch {
                        SharedPreferencesUtils.updateSortTime(SortTime.CREATE_TIME_ASC)
                        sheetState.hide()
                        onDismissRequest()
                    }
                }) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.create_time_asc))
                        Spacer(modifier = Modifier.weight(1f))
                        Checkbox(checked = sortTime.value == SortTime.CREATE_TIME_ASC, null)
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Long, Long) -> Unit
) {
    var startDateText by rememberSaveable { mutableStateOf("") }
    var endDateText by rememberSaveable { mutableStateOf("") }
    var startDateError by rememberSaveable { mutableStateOf(false) }
    var endDateError by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = SaltTheme.colors.popup, // 设置容器背景颜色
        title = { Text(stringResource(R.string.select_date)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.start_time))
                TextField(
                    value = startDateText,
                    onValueChange = {
                        startDateText = it
                        startDateError = false
                    },
                    placeholder = { Text("20250101") },
                    isError = startDateError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (startDateError) {
                    Text(
                        text = stringResource(R.string.input_correct_date),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.end_date))
                TextField(
                    value = endDateText,
                    onValueChange = {
                        endDateText = it
                        endDateError = false
                    },
                    placeholder = { Text("20250909") },
                    isError = endDateError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (endDateError) {
                    Text(
                        text = stringResource(R.string.input_correct_date),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // 验证并解析日期
                try {
                    val startDate = parseCompactDate(startDateText)
                    val endDate = parseCompactDate(endDateText)

                    val startMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val endMillis = endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onConfirm(startMillis, endMillis)
                } catch (e: Exception) {
                    startDateError = startDateText.isNotEmpty() && !isValidCompactDate(startDateText)
                    endDateError = endDateText.isNotEmpty() && !isValidCompactDate(endDateText)
                }
            }) {
                Text(R.string.sure.str)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(R.string.cancel.str)
            }
        }
    )
}

// 辅助函数验证日期格式 YYYYMMDD
private fun isValidCompactDate(dateString: String): Boolean {
    return try {
        parseCompactDate(dateString)
        true
    } catch (e: Exception) {
        false
    }
}

// 解析 YYYYMMDD 格式的日期字符串
private fun parseCompactDate(dateString: String): LocalDate {
    if (dateString.length != 8) {
        throw IllegalArgumentException(R.string.date_not_correct.str)
    }

    val year = dateString.substring(0, 4).toInt()
    val month = dateString.substring(4, 6).toInt()
    val day = dateString.substring(6, 8).toInt()

    return LocalDate.of(year, month, day)
}