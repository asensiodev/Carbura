package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.domain.user.RemoteUserProfile
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileGateway
import com.asensiodev.carbura.core.domain.user.RemoteUserProfileUnavailableException
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.io.IOException

class SupabaseUserProfileGateway(
    private val client: SupabaseClient,
) : RemoteUserProfileGateway {
    override suspend fun getProfileForUser(userId: UserId): RemoteUserProfile? =
        profileRequest {
            client
                .from("user_profiles")
                .select {
                    filter {
                        eq("user_id", userId.value)
                    }
                    limit(1)
                }.decodeList<UserProfileDto>()
                .firstOrNull()
                ?.toRemoteUserProfile()
                ?.withFamilyName()
        }

    override suspend fun ensureProfile(
        displayName: String,
        email: String?,
    ): RemoteUserProfile =
        profileRequest {
            client.postgrest
                .rpc(
                    function = "ensure_user_profile",
                    parameters =
                        EnsureUserProfileDto(
                            profileDisplayName = displayName,
                            profileEmail = email,
                        ),
                ).decodeSingle<UserProfileDto>()
                .toRemoteUserProfile()
                .withFamilyName()
        }

    private suspend fun RemoteUserProfile.withFamilyName(): RemoteUserProfile =
        copy(
            familyName = resolveFamilyName(familyId),
        )

    private suspend fun resolveFamilyName(familyId: FamilyId): String? =
        resolveFamilyNameOrNull {
            client
                .from("families")
                .select {
                    filter {
                        eq("id", familyId.value)
                    }
                    limit(1)
                }.decodeList<FamilyDto>()
                .firstOrNull()
                ?.name
        }
}

private suspend fun <T> profileRequest(block: suspend () -> T): T =
    try {
        block()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        if (error.isTemporaryProfileFailure()) throw RemoteUserProfileUnavailableException(error)
        throw error
    }

private fun Throwable.isTemporaryProfileFailure(): Boolean =
    generateSequence(this) { it.cause }.any { error ->
        error is IOException || error is RestException && error.statusCode !in 400..499
    }

internal suspend fun resolveFamilyNameOrNull(lookup: suspend () -> String?): String? =
    try {
        lookup()
    } catch (error: CancellationException) {
        throw error
    } catch (_: Exception) {
        null
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

@Serializable
internal data class FamilyDto(
    val id: String,
    val name: String,
)

internal fun UserProfileDto.toRemoteUserProfile(): RemoteUserProfile =
    RemoteUserProfile(
        userId = UserId(userId),
        familyId = FamilyId(familyId),
        familyName = null,
        displayName = displayName,
        email = email,
    )
