package com.cybersec.liquidstream.data.model

data class MovieQualityOption(
    val resolutionName: String, // e.g. "1080p HD", "720p HD", "360p HD"
    val qualityFolderUrl: String, // URL to resolution page
    val sampleFileSize: String = "2.3 GB",
    val format: String = "MP4"
)

data class MovieDetail(
    val id: String,
    val title: String,
    val posterUrl: String,
    val detailUrl: String,
    val rating: String = "8.9",
    val year: String = "2026",
    val language: String = "Tamil",
    val synopsis: String = "",
    val director: String = "",
    val starring: String = "",
    val genres: String = "",
    val qualityBadge: String = "HQ PreDVD",
    val lastUpdated: String = "",
    val availableQualities: List<MovieQualityOption> = emptyList()
)
