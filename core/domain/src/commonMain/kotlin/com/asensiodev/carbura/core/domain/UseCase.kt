package com.asensiodev.carbura.core.domain

fun interface SuspendUseCase<in Params, out Result> {
    suspend operator fun invoke(params: Params): Result
}
