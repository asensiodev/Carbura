package com.asensiodev.carbura.core.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.asensiodev.carbura.core.data.local.CarburaDatabase
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
                assertTrue(queries.selectDesiredNotifications().executeAsList().isEmpty())

                queries.replaceDesiredNotification("reminder-b", "Schedule", "payload-b", 1)
                queries.replaceDesiredNotification("reminder-a", "Schedule", "payload-a", 1)

                assertEquals(
                    listOf("reminder-a", "reminder-b"),
                    queries.selectDesiredNotifications().executeAsList().map { it.reminderId },
                )

                queries.replaceDesiredNotification("reminder-a", "Cancel", null, 2)
                val replacement = queries.selectDesiredNotifications().executeAsList().first()
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
                queries.replaceDesiredNotification("reminder-1", "Schedule", "payload", 1)

                queries.replaceDesiredNotification("reminder-1", "Schedule", "payload", 1)

                assertEquals(1L, queries.selectDesiredNotifications().executeAsOne().revision)
            }
        }

    @Test
    fun acknowledgementDeletesOnlyTheAppliedRevision() =
        runTest {
            withDatabase { database ->
                val queries = database.carburaDatabaseQueries
                queries.replaceDesiredNotification("reminder-1", "Schedule", "payload", 1)
                queries.replaceDesiredNotification("reminder-1", "Cancel", null, 2)

                queries.acknowledgeDesiredNotification("reminder-1", 1)
                assertEquals(2L, queries.selectDesiredNotifications().executeAsOne().revision)

                queries.acknowledgeDesiredNotification("reminder-1", 2)
                assertTrue(queries.selectDesiredNotifications().executeAsList().isEmpty())
            }
        }

    @Test
    fun revisionRemainsMonotonicAfterAcknowledgement() =
        runTest {
            withDatabase { database ->
                val outbox = SqlDelightNotificationOutbox(database)
                val reminderId = ReminderId("reminder-1")
                outbox.recordCancel(reminderId)
                val first = outbox.pending().single()
                outbox.acknowledge(reminderId, first.revision)

                outbox.recordCancel(reminderId)

                assertEquals(
                    2L,
                    outbox
                        .pending()
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
}
