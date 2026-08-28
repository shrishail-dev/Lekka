package com.nanokernel.expensetracker.util

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class ExportResult(val fileName: String, val uri: Uri, val mimeType: String)

/**
 * Saves a file into the device's Downloads/Lekka folder. Uses MediaStore on Android 10+ (no
 * permission needed under scoped storage); falls back to the legacy public-directory File API
 * on older versions, where the caller must have already secured WRITE_EXTERNAL_STORAGE.
 */
object DownloadFileWriter {

    fun write(context: Context, fileName: String, mimeType: String, writer: (OutputStream) -> Unit): ExportResult? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(context, fileName, mimeType, writer)
        } else {
            writeViaLegacyFile(context, fileName, mimeType, writer)
        }
    }

    private fun writeViaMediaStore(
        context: Context,
        fileName: String,
        mimeType: String,
        writer: (OutputStream) -> Unit
    ): ExportResult? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Lekka")
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        return runCatching {
            resolver.openOutputStream(uri)?.use(writer)
            ExportResult(fileName, uri, mimeType)
        }.getOrNull()
    }

    private fun writeViaLegacyFile(
        context: Context,
        fileName: String,
        mimeType: String,
        writer: (OutputStream) -> Unit
    ): ExportResult? {
        return runCatching {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Lekka")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)
            FileOutputStream(file).use(writer)
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null, null)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            ExportResult(fileName, uri, mimeType)
        }.getOrNull()
    }
}
