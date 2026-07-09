package com.asensiodev.carbura.core.domain.sync

import kotlinx.coroutines.flow.StateFlow

data class SyncStatus(
    val isSyncing: Boolean = false,
    val lastSyncedAtMillis: Long? = null,
    val lastErrorMessage: String? = null,
)

sealed interface SyncResult {
    data class Success(val syncedAtMillis: Long) : SyncResult
    data class Failure(val message: String) : SyncResult
}

interface SyncManager {
    val status: StateFlow<SyncStatus>
    suspend fun syncNow(): SyncResult
}
