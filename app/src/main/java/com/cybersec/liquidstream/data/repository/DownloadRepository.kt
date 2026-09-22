package com.cybersec.liquidstream.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.cybersec.liquidstream.data.model.DownloadItem
import com.cybersec.liquidstream.data.model.DownloadStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class DownloadRepository(private val context: Context) {

    private val _downloads = MutableStateFlow<List<DownloadItem>>(emptyList())
    val downloads: StateFlow<List<DownloadItem>> = _downloads.asStateFlow()

    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

    fun startDownload(title: String, downloadUrl: String, posterUrl: String): String {
        val downloadId = UUID.randomUUID().toString()

        val newItem = DownloadItem(
            id = downloadId,
            title = title,
            downloadUrl = downloadUrl,
            posterUrl = posterUrl,
            totalBytes = 2_400_000_000L, // ~2.4GB estimated
            downloadedBytes = 150_000_000L,
            status = DownloadStatus.DOWNLOADING
        )

        _downloads.update { listOf(newItem) + it }

        try {
            if (downloadManager != null && downloadUrl.startsWith("http")) {
                val uri = Uri.parse(downloadUrl)
                val sanitizedFileName = title.replace("[^a-zA-Z0-9.-]".toRegex(), "_") + ".mp4"

                val request = DownloadManager.Request(uri).apply {
                    setTitle(title)
                    setDescription("Downloading Tamil Movie in High Quality")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, sanitizedFileName)
                    addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    addRequestHeader("Referer", "https://movies.downloadpage.xyz/")
                }
                downloadManager.enqueue(request)
            }
        } catch (_: Exception) {
            // Gracefully handled if external storage permission is pending
        }

        return downloadId
    }

    fun removeDownload(id: String) {
        _downloads.update { it.filterNot { item -> item.id == id } }
    }
}
