package com.asensiodev.carbura.core.domain.user

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId

interface RemoteUserProfileGateway {
    suspend fun getProfileForUser(userId: UserId): RemoteUserProfile?
    suspend fun ensureProfile(
        displayName: String,
        email: String?,
    ): RemoteUserProfile
}

data class RemoteUserProfile(
    val userId: UserId,
    val familyId: FamilyId,
    val familyName: String?,
    val displayName: String,
    val email: String?,
)
