package me.ash.reader.data.model.preference

import android.content.Context
import android.os.LocaleList
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import java.util.*

sealed class LanguagesPreference(val value: Int) : Preference() {
    object UseDeviceLanguages : LanguagesPreference(0)
    object English : LanguagesPreference(1)
    object ChineseSimplified : LanguagesPreference(2)
    object German : LanguagesPreference(3)
    object French : LanguagesPreference(4)
    object Czech : LanguagesPreference(5)
    object Italian : LanguagesPreference(6)
    object Hindi : LanguagesPreference(7)
    object Spanish : LanguagesPreference(8)
    object Polish : LanguagesPreference(9)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.Languages,
                value
            )
            setLocale(context)
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            UseDeviceLanguages -> context.getString(R.string.use_device_languages)
            English -> context.getString(R.string.english)
            ChineseSimplified -> context.getString(R.string.chinese_simplified)
            German -> context.getString(R.string.german)
            French -> context.getString(R.string.french)
            Czech -> context.getString(R.string.czech)
            Italian -> context.getString(R.string.italian)
            Hindi -> context.getString(R.string.hindi)
            Spanish -> context.getString(R.string.spanish)
            Polish -> context.getString(R.string.polish)
        }

    fun getLocale(): Locale =
        when (this) {
            UseDeviceLanguages -> LocaleList.getDefault().get(0)
            English -> Locale("en", "US")
            ChineseSimplified -> Locale("zh", "CN")
            German -> Locale("de", "DE")
            French -> Locale("fr", "FR")
            Czech -> Locale("cs", "CZ")
            Italian -> Locale("it", "IT")
            Hindi -> Locale("hi", "IN")
            Spanish -> Locale("es", "ES")
            Polish -> Locale("pl", "PL")
        }

    fun setLocale(context: Context) {
        val locale = getLocale()
        val resources = context.resources
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        context.createConfigurationContext(configuration)
        resources.updateConfiguration(configuration, metrics)

        val appResources = context.applicationContext.resources
        val appMetrics = appResources.displayMetrics
        val appConfiguration = appResources.configuration
        appConfiguration.setLocale(locale)
        appConfiguration.setLocales(LocaleList(locale))
        context.applicationContext.createConfigurationContext(appConfiguration)
        appResources.updateConfiguration(appConfiguration, appMetrics)
    }

    companion object {

        val default = UseDeviceLanguages
        val values = listOf(UseDeviceLanguages,
            English,
            ChineseSimplified,
            German,
            French,
            Czech,
            Italian,
            Hindi,
            Spanish,
            Polish)

        fun fromPreferences(preferences: Preferences): LanguagesPreference =
            when (preferences[DataStoreKeys.Languages.key]) {
                0 -> UseDeviceLanguages
                1 -> English
                2 -> ChineseSimplified
                3 -> German
                4 -> French
                5 -> Czech
                6 -> Italian
                7 -> Hindi
                8 -> Spanish
                9 -> Polish
                else -> default
            }

        fun fromValue(value: Int): LanguagesPreference =
            when (value) {
                0 -> UseDeviceLanguages
                1 -> English
                2 -> ChineseSimplified
                3 -> German
                4 -> French
                5 -> Czech
                6 -> Italian
                7 -> Hindi
                8 -> Spanish
                9 -> Polish
                else -> default
            }
    }
}
