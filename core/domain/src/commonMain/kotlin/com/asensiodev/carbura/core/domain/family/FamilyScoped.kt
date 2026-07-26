package com.asensiodev.carbura.core.domain.family

import com.asensiodev.carbura.core.model.ActiveFamilyScope

data class FamilyScoped<T>(
    val scope: ActiveFamilyScope,
    val value: T,
)
