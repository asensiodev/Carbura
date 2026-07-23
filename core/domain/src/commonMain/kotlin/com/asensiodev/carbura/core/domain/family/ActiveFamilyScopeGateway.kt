package com.asensiodev.carbura.core.domain.family

import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId

interface ActiveFamilyScopeGateway {
    fun activateAuthenticated(
        userId: UserId,
        familyId: FamilyId,
    ): ActiveFamilyScope

    fun activateLocal(): ActiveFamilyScope

    fun current(): ActiveFamilyScope

    fun requireCurrent(expected: ActiveFamilyScope)

    fun capture(familyId: FamilyId): ActiveFamilyScope = current().also { require(it.familyId == familyId) }
}

class StaleFamilyScopeException : IllegalStateException("The active account family changed")
