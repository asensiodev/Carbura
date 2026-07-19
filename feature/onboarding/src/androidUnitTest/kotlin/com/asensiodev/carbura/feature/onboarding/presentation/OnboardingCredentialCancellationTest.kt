package com.asensiodev.carbura.feature.onboarding.presentation

import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class OnboardingCredentialCancellationTest {
    @Test
    fun credentialCancellationPropagates() {
        val cancellation = CancellationException("Credential request cancelled")
        var cancellationHandled = false

        val thrown =
            assertFailsWith<CancellationException> {
                propagateCredentialCancellation(cancellation) {
                    cancellationHandled = true
                }
            }

        assertSame(cancellation, thrown)
        assertTrue(cancellationHandled)
    }
}
