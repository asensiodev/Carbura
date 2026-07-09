package com.asensiodev.carbura.core.domain.sync.usecase

import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import kotlinx.coroutines.flow.StateFlow

class ObserveSyncStatusUseCase(
    private val syncManager: SyncManager,
) {
    operator fun invoke(): StateFlow<SyncStatus> = syncManager.status
}
