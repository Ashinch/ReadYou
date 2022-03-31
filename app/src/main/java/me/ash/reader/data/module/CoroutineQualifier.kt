package me.ash.reader.data.module

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DispatcherDefault

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DispatcherIO

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class DispatcherMain

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DispatcherMainImmediate