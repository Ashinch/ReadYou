package me.ash.reader.infrastructure.di

import javax.inject.Qualifier

/**
 * @see CoroutineDispatcherModule.provideDefaultDispatcher
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DefaultDispatcher

/**
 * @see CoroutineDispatcherModule.provideIODispatcher
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class IODispatcher

/**
 * @see CoroutineDispatcherModule.provideMainDispatcher
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class MainDispatcher

/**
 * @see CoroutineDispatcherModule.provideMainImmediateDispatcher
 */
@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainImmediateDispatcher
