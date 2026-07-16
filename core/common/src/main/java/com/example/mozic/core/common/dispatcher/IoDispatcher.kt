package com.example.mozic.core.common.dispatcher

import javax.inject.Qualifier

/** Injected instead of an inline `Dispatchers.IO` reference — keeps I/O call sites testable. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
