package me.ash.reader.ui.widget

import kotlinx.serialization.Serializable

@Serializable
data class FeedWidgetConfig(val theme: Theme, val dataSource: DataSource) {
    companion object {
        fun default(accountId: Int) =
            FeedWidgetConfig(theme = Theme.SansSerif, dataSource = DataSource.Account(accountId))
    }
}

@Serializable
sealed interface DataSource {
    @Serializable data class Feed(val feedId: String) : DataSource

    @Serializable data class Group(val groupId: String) : DataSource

    @Serializable data class Account(val accountId: Int) : DataSource
}

@Serializable
enum class Theme {
    SansSerif,
    Serif,
}
