package com.asensiodev.carbura.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class SupabaseUserProfileGatewayTest {
    @Test
    fun mapsUserProfileDtoToRemoteProfile() {
        val profile = UserProfileDto(
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
}
