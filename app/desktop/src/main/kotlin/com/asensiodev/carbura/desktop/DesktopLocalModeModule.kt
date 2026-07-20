package com.asensiodev.carbura.desktop

import com.asensiodev.carbura.core.domain.sync.SyncManager
import com.asensiodev.carbura.core.domain.sync.SyncResult
import com.asensiodev.carbura.core.domain.sync.SyncStatus
import com.asensiodev.carbura.core.model.FamilyId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.dsl.module

internal val desktopLocalModeModule =
    module {
        single { FamilyId("local-family") }
        single<SyncManager> { DesktopLocalSyncManager }
    }

private object DesktopLocalSyncManager : SyncManager {
    override val status: StateFlow<SyncStatus> = MutableStateFlow(SyncStatus())

    override suspend fun syncNow(): SyncResult = SyncResult.Success(System.currentTimeMillis())

    override fun acknowledgeFailure(failureId: Long) = Unit
}
