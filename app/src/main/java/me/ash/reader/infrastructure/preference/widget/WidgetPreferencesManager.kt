package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.widgetDataStore

/**
 * Get the widget-specific key (as a string) for the given key name.
 */
fun widgetDataKey(keyName: String, appWidgetId: Int): String = "${keyName}_${appWidgetId}"

open class WidgetPreference<T: Any>(
    protected val context: Context,
    protected val keyName: String,
    val default: T,
    protected val keyFactory: (String) -> Preferences.Key<T>
) {

    protected val cachedValues = mutableMapOf<Int, AtomicReference<T?>>()

    protected fun preferencesKey(widgetId: Int) = keyFactory(widgetDataKey(keyName, widgetId))

    /**
     * Return a `Flow` that observes the `DataStore`.
     */
    fun asFlow(widgetId: Int): Flow<T> {
        val key = preferencesKey(widgetId)
        Log.d("WidgetPreference", "asFlow key: $key")
        //Log.d("WidgetPreference", "widgetDataStore: ${context.widgetDataStore.data}")
        return context.widgetDataStore.data.map {
            Log.d("WidgetPreferences", "data: $it")
            val value = it[key]
            Log.d("WidgetPreference", "asFlow value: $value")
            value ?: default
        }
    }

    suspend fun put(widgetId: Int, value: T) {
        Log.d("WidgetPreference", "put key: ${preferencesKey(widgetId)}, value: $value")
        context.widgetDataStore.edit {
            it[preferencesKey(widgetId)] = value
        }
        refresh(widgetId)
    }

    suspend fun get(widgetId: Int): T =
        getCached(widgetId) ?: asFlow(widgetId).first().also {
            setCached(widgetId, it)
        }

    /**
     * Get the cached value immediately.
     */
    fun getCached(widgetId: Int): T? {
        Log.d("WidgetPreferencesManager", "Getting cached value for widget $widgetId. Stored value is ${cachedValues[widgetId]}")
        return cachedValues.getOrPut(widgetId) {
            AtomicReference<T?>(default)
        }.get()
    }

    /**
     *
     */

    fun getCachedOrDefault(widgetId: Int) =
        getCached(widgetId) ?: default

    /**
     * Set the cached value.
     */
    fun setCached(widgetId: Int, value: T) {
        cachedValues.getOrPut(widgetId) { AtomicReference(value) }.set(value)
    }

    /**
     * Update the cached value from the DataStore.
     */
    suspend fun refresh(widgetId: Int) {
        val fromFlow = asFlow(widgetId).first()
        Log.d("WidgetPreference", "Obtained value $fromFlow for widget $widgetId")
        setCached(widgetId, fromFlow)
    }

    fun delete(widgetId: Int, scope: CoroutineScope) {
        scope.launch {
            context.widgetDataStore.edit {
                it.remove(preferencesKey(widgetId))
            }
        }
    }
}

class BooleanWidgetPreference(context: Context, keyName: String, default: Boolean):
    WidgetPreference<Boolean>(context, keyName, default, {n -> booleanPreferencesKey(n) }) {
    suspend fun toggle(widgetId: Int) {
        val current = getCachedOrDefault(widgetId)
        Log.d("BooleanWidgetPreference", "Current value is $current, so putting ${!current}")
        put(widgetId, !current)
    }
}

class WidgetPreferencesManager(context: Context) {
    internal val showFeedIcon = BooleanWidgetPreference(context, "showFeedIcon", true)
    internal val showFeedName = BooleanWidgetPreference(context, "showFeedName", false)

    fun deleteAll(widgetId: Int, scope: CoroutineScope) {
        scope.launch {
            showFeedIcon.delete(widgetId, this)
            showFeedName.delete(widgetId, this)
        }
    }

    companion object {
        @Volatile
        private var instance: WidgetPreferencesManager? = null

        fun getInstance(context: Context): WidgetPreferencesManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = WidgetPreferencesManager(context)
                    }
                }
            }
            return instance!!
        }
    }
}
