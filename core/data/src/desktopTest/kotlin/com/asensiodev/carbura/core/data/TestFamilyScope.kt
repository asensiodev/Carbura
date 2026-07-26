package com.asensiodev.carbura.core.data

import com.asensiodev.carbura.core.data.local.CarburaDatabase
import com.asensiodev.carbura.core.model.ActiveFamilyScope
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.core.model.UserId

internal fun CarburaDatabase.activateTestFamily(familyId: FamilyId): ActiveFamilyScope =
    SqlDelightActiveFamilyScopeGateway(this).activateAuthenticated(UserId("test-user"), familyId)
