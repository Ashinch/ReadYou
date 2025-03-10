package me.ash.reader.infrastructure.preference.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.widgetDataStore

open class WidgetPreference<T: Any>(
    protected val context: Context,
    protected val keyName: String,
    val default: T,
    protected val keyFactory: (String) -> Preferences.Key<T>
) {

    protected val cachedValues = mutableMapOf<Int, AtomicReference<T?>>()

    protected fun preferencesKey(widgetId: Int) = keyFactory(widgetDataKey(keyName, widgetId))

    /**
     * Return a `Flow` that observes the relevant data in the `DataStore`. The value can be obtained
     * from within a `Composable` by calling `collectAsStateValue` on the `Flow`.
     */
    fun asFlow(widgetId: Int): Flow<T> {
        val key = preferencesKey(widgetId)
        return context.widgetDataStore.data.map {
            val value = it[key]
            value ?: default
        }
    }

    /**
     * Store `value` in the `DataStore`.
     */
    suspend fun put(widgetId: Int, value: T) {
        context.widgetDataStore.edit {
            it[preferencesKey(widgetId)] = value
        }
        refresh(widgetId)
    }

    /**
     * Get the current value of the preference, looking for a cached value first and then retrieving
     * the value from the `DataStore` if this is not present.
     */
    suspend fun get(widgetId: Int): T =
        getCached(widgetId) ?: asFlow(widgetId).first().also {
            setCached(widgetId, it)
        }

    /**
     * Get the cached value immediately.
     */
    fun getCached(widgetId: Int): T? {
        return cachedValues.getOrPut(widgetId) {
            AtomicReference<T?>(default)
        }.get()
    }

    /**
     * Get the cached value, or return the preference's default value if no cached value is present.
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
        setCached(widgetId, fromFlow)
    }

    /**
     * Delete this key-value pair from the `DataStore`.
     */
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
        put(widgetId, !current)
    }
}

open class StringWidgetPreference(context: Context, keyName: String, default: String):
    WidgetPreference<String>(context, keyName, default, {n -> stringPreferencesKey(n) })

class IntWidgetPreference(context: Context, keyName: String, default: Int):
    WidgetPreference<Int>(context, keyName, default, {n -> intPreferencesKey(n) })
