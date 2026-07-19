package com.asensiodev.carbura.core.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class SupabaseUserProfileGatewayTest {
    @Test
    fun mapsUserProfileDtoToRemoteProfile() {
        val profile =
            UserProfileDto(
                userId = "user-1",
                familyId = "family-1",
                displayName = "Angela",
                email = "angela@example.com",
            ).toRemoteUserProfile()

        assertEquals("user-1", profile.userId.value)
        assertEquals("family-1", profile.familyId.value)
        assertEquals(null, profile.familyName)
        assertEquals("Angela", profile.displayName)
        assertEquals("angela@example.com", profile.email)
    }

    @Test
    fun familyNameLookupCancellationPropagates() =
        runTest {
            val cancellation = CancellationException("Family lookup cancelled")

            val thrown =
                assertFailsWith<CancellationException> {
                    resolveFamilyNameOrNull { throw cancellation }
                }

            assertSame(cancellation, thrown)
        }

    @Test
    fun familyNameLookupFailureFallsBackToNull() =
        runTest {
            val familyName =
                resolveFamilyNameOrNull {
                    throw IllegalStateException("Family unavailable")
                }

            assertNull(familyName)
        }
}
