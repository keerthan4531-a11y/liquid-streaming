package com.cybersec.liquidstream.data.model

data class StreamSource(
    val title: String,
    val directStreamUrl: String, // MP4 with ?stream=1 for ExoPlayer
    val directDownloadUrl: String, // Pure MP4 URL for DownloadManager
    val watchOnlinePageUrl: String,
    val fileName: String,
    val fileSize: String,
    val videoResolution: String,
    val duration: String,
    val streamHeaders: Map<String, String> = mapOf(
        "Referer" to "https://play.onestream.today/",
        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    )
)
