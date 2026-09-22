package com.cybersec.liquidstream.ui.screens.downloads

import androidx.lifecycle.ViewModel
import com.cybersec.liquidstream.data.model.DownloadItem
import com.cybersec.liquidstream.data.repository.DownloadRepository
import kotlinx.coroutines.flow.StateFlow

class DownloadsViewModel(private val repository: DownloadRepository) : ViewModel() {

    val downloads: StateFlow<List<DownloadItem>> = repository.downloads

    fun removeDownload(id: String) {
        repository.removeDownload(id)
    }
}
