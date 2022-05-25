package me.ash.reader.data.preference

import android.content.Context
import android.os.LocaleList
import android.util.Log
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

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.Languages,
                value
            )
            setLocale(context)
        }
    }

    fun getDesc(context: Context): String =
        when (this) {
            UseDeviceLanguages -> context.getString(R.string.use_device_languages)
            English -> context.getString(R.string.english)
            ChineseSimplified -> context.getString(R.string.chinese_simplified)
            German -> context.getString(R.string.german)
            French -> context.getString(R.string.french)
            Czech -> context.getString(R.string.czech)
        }

    fun getLocale(): Locale =
        when (this) {
            UseDeviceLanguages -> LocaleList.getDefault().get(0)
            English -> Locale("en", "US")
            ChineseSimplified -> Locale("zh", "CN")
            German -> Locale("de", "DE")
            French -> Locale("fr", "FR")
            Czech -> Locale("cs", "CZ")
        }

    fun setLocale(context: Context) {
        val locale = getLocale()

        Log.i("Rlog", "setLocale: $locale, ${LocaleList.getDefault().get(0)}")

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
        val values = listOf(UseDeviceLanguages, English, ChineseSimplified, German, French, Czech)

        fun fromPreferences(preferences: Preferences): LanguagesPreference =
            when (preferences[DataStoreKeys.Languages.key]) {
                0 -> UseDeviceLanguages
                1 -> English
                2 -> ChineseSimplified
                3 -> German
                4 -> French
                5 -> Czech
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
                else -> default
            }
    }
}