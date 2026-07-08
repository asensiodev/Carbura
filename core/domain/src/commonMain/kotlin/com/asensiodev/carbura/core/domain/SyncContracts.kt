package com.asensiodev.carbura.core.domain

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

class SyncNowUseCase(
    private val syncManager: SyncManager,
) : SuspendUseCase<Unit, SyncResult> {
    override suspend fun invoke(params: Unit): SyncResult = syncManager.syncNow()
}

class ObserveSyncStatusUseCase(
    private val syncManager: SyncManager,
) {
    operator fun invoke(): StateFlow<SyncStatus> = syncManager.status
}
