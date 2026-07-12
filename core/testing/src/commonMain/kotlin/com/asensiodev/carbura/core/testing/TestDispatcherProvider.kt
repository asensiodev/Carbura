package com.asensiodev.carbura.core.testing

import com.asensiodev.carbura.core.domain.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class TestDispatcherProvider
    @OptIn(ExperimentalCoroutinesApi::class)
    constructor(
        override val io: CoroutineDispatcher = UnconfinedTestDispatcher(),
        override val default: CoroutineDispatcher = UnconfinedTestDispatcher(),
        override val main: CoroutineDispatcher = UnconfinedTestDispatcher(),
    ) : DispatcherProvider
