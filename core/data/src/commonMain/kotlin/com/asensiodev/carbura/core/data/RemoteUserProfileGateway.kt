package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId

interface RemoteUserProfileGateway {
    suspend fun getProfileForUser(userId: UserId): RemoteUserProfile?
}

data class RemoteUserProfile(
    val userId: UserId,
    val familyId: FamilyId,
    val displayName: String,
    val email: String?,
)
