package com.cybersec.liquidstream.data.model

data class Movie(
    val id: String,
    val title: String,
    val posterUrl: String,
    val detailUrl: String,
    val rating: String = "8.5",
    val badge: String = "NEW",
    val year: String = "2026",
    val category: String = "Tamil",
    val genre: String = "Drama",
    val duration: String = "2h 18m",
    val synopsis: String = "Stream now in HD with direct multi-hop reverse-engineered pipeline.",
    val ageRating: String = "U/A 16+"
)
