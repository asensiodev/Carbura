package com.asensiodev.carbura.quality

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import kotlin.test.Test

class ArchitectureKonsistTest {
    private val projectScope = Konsist.scopeFromDirectory(".")

    @Test
    fun `common source sets do not depend on Android platform APIs`() {
        projectScope
            .slice { it.projectPath.contains("/src/commonMain/") }
            .files
            .assertFalse { file ->
                file.imports.any { import -> import.name.startsWith("android.") }
            }
    }

    @Test
    fun `feature modules do not depend on data implementations`() {
        projectScope
            .slice { it.projectPath.startsWith("feature/") }
            .files
            .assertFalse { file ->
                file.imports.any { import -> import.name.startsWith("com.asensiodev.carbura.core.data") }
            }
    }

    @Test
    fun `presentation code does not depend on persistence or network clients`() {
        projectScope
            .slice { it.projectPath.contains("/presentation/") }
            .files
            .assertFalse { file ->
                file.imports.any { import ->
                    import.name.startsWith("app.cash.sqldelight") ||
                        import.name.startsWith("io.github.jan-tennert.supabase") ||
                        import.name.startsWith("io.ktor")
                }
            }
    }

    @Test
    fun `use cases live in usecase packages`() {
        projectScope
            .classes()
            .withNameEndingWith("UseCase")
            .assertTrue { it.resideInPackage("..usecase..") }
    }

    @Test
    fun `domain repositories are interfaces`() {
        projectScope
            .interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.resideInPackage("..core.domain..repository..") }
    }
}
