package com.asensiodev.carbura.core.domain.auth

import com.asensiodev.carbura.core.model.FamilyId

interface AccountLocalDataCleaner {
    suspend fun clear(familyId: FamilyId)
}
