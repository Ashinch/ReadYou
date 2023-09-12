package me.ash.reader.infrastructure.di

import kotlinx.coroutines.CoroutineScope
import javax.inject.Qualifier

/**
 * Provides [CoroutineScope] for the application.
 *
 * @see CoroutineScopeModule.provideCoroutineScope
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
