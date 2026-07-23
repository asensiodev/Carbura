package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.domain.family.ActiveFamilyScopeGateway
import com.asensiodev.carbura.core.domain.family.StaleFamilyScopeException
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId

internal class SqlDelightActiveFamilyScopeGateway(
    private val database: CarburaDatabase,
) : ActiveFamilyScopeGateway {
    override fun activateAuthenticated(
        userId: UserId,
        familyId: FamilyId,
    ): ActiveFamilyScope = activate(userId, familyId)

    override fun activateLocal(): ActiveFamilyScope = activate(null, LOCAL_FAMILY_ID)

    override fun current(): ActiveFamilyScope {
        val row = database.carburaDatabaseQueries.selectActiveFamilyScope().executeAsOneOrNull()
        if (row != null) return row.toScope()
        return ActiveFamilyScope(null, LOCAL_FAMILY_ID, 1).also(::persist)
    }

    override fun requireCurrent(expected: ActiveFamilyScope) {
        if (current() != expected) throw StaleFamilyScopeException()
    }

    private fun activate(
        userId: UserId?,
        familyId: FamilyId,
    ): ActiveFamilyScope {
        val current = current()
        if (current.userId == userId && current.familyId == familyId) return current
        return ActiveFamilyScope(userId, familyId, current.generation + 1).also(::persist)
    }

    private fun persist(scope: ActiveFamilyScope) {
        database.carburaDatabaseQueries.replaceActiveFamilyScope(
            userId = scope.userId?.value,
            familyId = scope.familyId.value,
            generation = scope.generation,
        )
    }

    private fun com.asensiodev.carbura.core.data.local.ActiveFamilyScope.toScope() =
        ActiveFamilyScope(userId?.let(::UserId), FamilyId(familyId), generation)
}

internal val LOCAL_FAMILY_ID = FamilyId("local-family")
