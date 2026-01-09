package com.ldlywt.note.utils

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ldlywt.note.App
import com.ldlywt.note.R
import com.ldlywt.note.bean.Note
import com.ldlywt.note.db.repo.TagNoteRepo
import com.ldlywt.note.ui.page.home.clickable
import com.moriafly.salt.ui.SaltTheme
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class FirstTimeManager @Inject constructor() {

    @Inject
    lateinit var tagNoteRepo: TagNoteRepo

    fun generateIntroduceNoteList() {
        lunchIo {
            if (!SettingsPreferences.firstLaunch.first() || tagNoteRepo.queryAllNoteList().isNotEmpty()) {
                return@lunchIo
            }
            if (App.instance.isSystemLanguageEnglish()) {
                generateEnglishIntroduceNoteList()
            } else {
                generateChineseIntroduceNoteList()
            }
        }
    }

    private fun generateChineseIntroduceNoteList() {
        val functionNote = Note(
            content = "#灵感 \n生活不止眼前的苟且 还有诗和远方。",
        )
        tagNoteRepo.insertOrUpdate(functionNote)
    }

    private fun generateEnglishIntroduceNoteList() {
        val functionNote = Note(
            content = "#Life \nLess is more.",
        )
        tagNoteRepo.insertOrUpdate(functionNote)
    }

}

@Composable
fun FirstTimeWarmDialog(block: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        containerColor = SaltTheme.colors.subBackground,
        onDismissRequest = { },
        title = { Text(stringResource(R.string.welcome), color = SaltTheme.colors.text) },
        text = {
            Column {
                Text(stringResource(id = R.string.warm_reminder_desc), color = SaltTheme.colors.text)
                Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
                Text(
                    stringResource(id = R.string.browse_tos_tips_service),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.outline,
                    ),
                    modifier = androidx.compose.ui.Modifier.clickable {
                        Constant.startUserAgreeUrl(context)
                    }
                )
                Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                Text(
                    stringResource(id = R.string.browse_tos_tips_privacy),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.outline,
                    ),
                    modifier = androidx.compose.ui.Modifier.clickable {
                        Constant.startPrivacyUrl(context)
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                block()
            }) {
                Text(stringResource(id = R.string.agree))
            }
        },
        dismissButton = {
            Button(onClick = {
                if (context is Activity) {
                    context.finish()
                }
            }) {
                Text(stringResource(id = R.string.exit))
            }
        }
    )
}


@Composable
fun TipsDialog(block: () -> Unit) {
    AlertDialog(
        containerColor = SaltTheme.colors.subBackground,
        onDismissRequest = { },
        title = { Text(stringResource(R.string.warm_reminder), color = SaltTheme.colors.text) },
        text = {
            Column {
                Text(stringResource(id = R.string.warm_reminder_desc), color = SaltTheme.colors.text)
            }
        },
        confirmButton = {
            Button(onClick = {
                block()
            }) {
                Text("OK")
            }
        }
    )
}