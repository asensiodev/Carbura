package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.ReminderId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesiredNotificationOutboxQueriesTest {
    @Test
    fun desiredStateIsInsertedReplacedAndListedDeterministically() =
        runTest {
            withDatabase { database ->
                val queries = database.carburaDatabaseQueries
                assertTrue(queries.selectDesiredNotifications(FAMILY_ID.value).executeAsList().isEmpty())

                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-b", "Schedule", "payload-b", 1)
                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-a", "Schedule", "payload-a", 1)

                assertEquals(
                    listOf("reminder-a", "reminder-b"),
                    queries.selectDesiredNotifications(FAMILY_ID.value).executeAsList().map { it.reminderId },
                )

                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-a", "Cancel", null, 2)
                val replacement = queries.selectDesiredNotifications(FAMILY_ID.value).executeAsList().first()
                assertEquals("Cancel", replacement.action)
                assertEquals(null, replacement.payload)
                assertEquals(2L, replacement.revision)
            }
        }

    @Test
    fun unchangedDesiredStateKeepsItsRevision() =
        runTest {
            withDatabase { database ->
                val queries = database.carburaDatabaseQueries
                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-1", "Schedule", "payload", 1)

                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-1", "Schedule", "payload", 1)

                assertEquals(1L, queries.selectDesiredNotifications(FAMILY_ID.value).executeAsOne().revision)
            }
        }

    @Test
    fun acknowledgementDeletesOnlyTheAppliedRevision() =
        runTest {
            withDatabase { database ->
                val queries = database.carburaDatabaseQueries
                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-1", "Schedule", "payload", 1)
                queries.replaceDesiredNotification(FAMILY_ID.value, "reminder-1", "Cancel", null, 2)

                queries.acknowledgeDesiredNotification(FAMILY_ID.value, "reminder-1", 1)
                assertEquals(2L, queries.selectDesiredNotifications(FAMILY_ID.value).executeAsOne().revision)

                queries.acknowledgeDesiredNotification(FAMILY_ID.value, "reminder-1", 2)
                assertTrue(queries.selectDesiredNotifications(FAMILY_ID.value).executeAsList().isEmpty())
            }
        }

    @Test
    fun revisionRemainsMonotonicAfterAcknowledgement() =
        runTest {
            withDatabase { database ->
                val outbox = SqlDelightNotificationOutbox(database)
                val scope = database.activateTestFamily(FAMILY_ID)
                val reminderId = ReminderId("reminder-1")
                outbox.recordCancel(scope, reminderId)
                val first = outbox.pending(scope).single()
                outbox.acknowledge(scope, reminderId, first.revision)

                outbox.recordCancel(scope, reminderId)

                assertEquals(
                    2L,
                    outbox
                        .pending(scope)
                        .single()
                        .revision.value,
                )
            }
        }

    private suspend fun withDatabase(block: suspend (CarburaDatabase) -> Unit) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            CarburaDatabase.Schema.create(driver)
            block(CarburaDatabase(driver))
        } finally {
            driver.close()
        }
    }

    private companion object {
        val FAMILY_ID = FamilyId("family")
    }
}
