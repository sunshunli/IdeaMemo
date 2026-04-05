package com.ldlywt.note.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract


object None

object ExportNotesJsonContract : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "IdeaMemo.json")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object ChoseFolderContract : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object ExportTextContract : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object ExportHtmlContract : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
            putExtra(Intent.EXTRA_TITLE, "IdeaMemoHtml.zip")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

class ExportMarkDownContract(val name: String) : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/markdown"
            putExtra(Intent.EXTRA_TITLE, "$name.md")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object RestoreNotesContract : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object ImportHtmlZipContract : ActivityResultContract<None?, Uri?>() {
    override fun createIntent(context: Context, input: None?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/zip"
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent != null && resultCode == Activity.RESULT_OK) intent.data else null
    }
}

object TakePictureContract : ActivityResultContract<Uri, Boolean>() {
    override fun createIntent(context: Context, input: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, input)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return resultCode == Activity.RESULT_OK
    }
}
