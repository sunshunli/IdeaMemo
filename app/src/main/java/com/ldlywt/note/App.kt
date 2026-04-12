package com.ldlywt.note

import android.app.Application
import com.ldlywt.note.backup.BackupScheduler
import com.ldlywt.note.db.repo.TagNoteRepo
import com.ldlywt.note.utils.SettingsPreferences
import com.ldlywt.note.utils.SharedPreferencesUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

fun getAppName(): String {
    return "IdeaMemo"
}


@HiltAndroidApp
class App : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        instance = this
        super.onCreate()

        applicationScope.launch {
            SharedPreferencesUtils.localAutoBackup.collect { enabled ->
                if (enabled) {
                    BackupScheduler.scheduleDailyBackup(this@App)
                } else {
                    BackupScheduler.cancelDailyBackup(this@App)
                }
            }
        }

        applicationScope.launch {
            SettingsPreferences.themeMode.collect {
                SettingsPreferences.applyAppCompatThemeMode(it)
            }
        }
    }

    companion object {
        lateinit var instance: App
            private set
    }
}

@InstallIn(SingletonComponent::class)   // <-- add this line
@EntryPoint
interface AppEntryPoint {
    fun tagNoteRepo(): TagNoteRepo
}
