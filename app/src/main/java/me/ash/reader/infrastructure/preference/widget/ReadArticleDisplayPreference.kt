package me.ash.reader.infrastructure.preference.widget

import android.content.Context

enum class ReadArticleDisplayOption {
    SHOW,
    SOFT_HIDE,
    HARD_HIDE
}

class ReadArticleDisplayPreference(context: Context):
    StringWidgetPreference(context, "readArticleDisplay", ReadArticleDisplayOption.SHOW.toString()) {

    val defaultOption = ReadArticleDisplayOption.valueOf(default)

    suspend fun putOption(widgetId: Int, value: ReadArticleDisplayOption) =
        put(widgetId, value.toString())

    suspend fun getOption(widgetId: Int) =
        ReadArticleDisplayOption.valueOf(get(widgetId))

    fun getCachedOption(widgetId: Int) =
        getCached(widgetId)?.let {
            ReadArticleDisplayOption.valueOf(it)
        }
    }