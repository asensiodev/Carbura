package com.asensiodev.carbura.core.data

import kotlinx.coroutines.sync.Mutex

internal class SyncOperationLock {
    val mutex = Mutex()
}
