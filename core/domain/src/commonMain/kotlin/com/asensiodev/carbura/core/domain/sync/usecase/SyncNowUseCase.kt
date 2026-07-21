package com.asensiodev.carbura.core.domain.sync.usecase

import com.asensiodev.carbura.core.domain.SuspendUseCase
import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncResult

class SyncNowUseCase(
    private val syncManager: SyncManager,
) : SuspendUseCase<Unit, SyncResult> {
    override suspend fun invoke(params: Unit): SyncResult = syncManager.syncNow()
}
