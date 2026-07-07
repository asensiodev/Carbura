package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SupabaseUserProfileGateway(
    private val client: SupabaseClient,
) : RemoteUserProfileGateway {
    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile? =
        client.from("user_profiles")
            .select {
                filter {
                    eq("user_id", userId.value)
                }
                limit(1)
            }
            .decodeList<UserProfileDto>()
            .firstOrNull()
            ?.toRemoteUserProfile()

    override suspend fun ensureProfile(
        displayName: String,
        email: String?,
    ): RemoteUserProfile =
        client.postgrest.rpc(
            function = "ensure_user_profile",
            parameters = EnsureUserProfileDto(
                profileDisplayName = displayName,
                profileEmail = email,
            )
        ).decodeSingle<UserProfileDto>()
            .toRemoteUserProfile()
}

@Serializable
internal data class EnsureUserProfileDto(
    @SerialName("profile_display_name") val profileDisplayName: String,
    @SerialName("profile_email") val profileEmail: String? = null,
)

@Serializable
internal data class UserProfileDto(
    @SerialName("user_id") val userId: String,
    @SerialName("family_id") val familyId: String,
    @SerialName("display_name") val displayName: String,
    val email: String? = null,
)

internal fun UserProfileDto.toRemoteUserProfile(): RemoteUserProfile = RemoteUserProfile(
    userId = UserId(userId),
    familyId = FamilyId(familyId),
    displayName = displayName,
    email = email,
)
