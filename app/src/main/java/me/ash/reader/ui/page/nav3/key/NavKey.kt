package me.ash.reader.ui.page.nav3.key

import androidx.navigation3.runtime.NavKey

sealed interface Route {
    // Startup
    data object Startup : Route

    // Home
    data object Feeds : Route

    data object Flow : Route

    class Reading(val articleId: String) : Route

//    // Settings
//    data object Settings : Route
//
//    // Accounts
//    data object Accounts : Route
//
//    data class AccountDetails(val accountId: Int) : Route
//
//    data object AddAccounts : Route
//
//    // Color & Style
//    data object ColorAndStyle : Route
//
//    data object DarkTheme : Route
//
//    data object FeedsPageStyle : Route
//
//    data object FlowPageStyle : Route
//
//    data object ReadingPageStyle : Route
//
//    // Interaction
//    data object Interaction : Route
//
//    // Languages
//    data object Languages : Route
}
