package com.cybersec.liquidstream.ui.navigation

import com.cybersec.liquidstream.data.model.Movie
import com.cybersec.liquidstream.data.model.StreamSource
import com.cybersec.liquidstream.ui.components.NavItem

sealed class Screen {
    object Home : Screen()
    object Categories : Screen()
    object Search : Screen()
    object Downloads : Screen()
    object Profile : Screen()
    object Settings : Screen()
    object About : Screen()
    object AiChat : Screen()
    data class Detail(val movie: Movie) : Screen()
    data class Player(val streamSource: StreamSource) : Screen()
    data class CategoryDetail(val title: String, val categoryKey: String) : Screen()
}
