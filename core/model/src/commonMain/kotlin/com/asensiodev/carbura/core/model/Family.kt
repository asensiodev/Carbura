package com.asensiodev.carbura.core.model

data class Family(
    val id: FamilyId,
    val name: String,
    val inviteCode: String? = null,
)

data class UserProfile(
    val id: UserProfileId,
    val userId: UserId,
    val familyId: FamilyId,
    val displayName: String,
    val email: String? = null,
)
