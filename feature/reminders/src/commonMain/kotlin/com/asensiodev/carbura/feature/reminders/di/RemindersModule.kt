package com.asensiodev.carbura.feature.reminders.di

import com.asensiodev.carbura.core.domain.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.DeleteReminderUseCase
import com.asensiodev.carbura.core.domain.GetPendingRemindersUseCase
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import org.koin.dsl.module

val remindersModule = module {
    factory { CreateReminderUseCase(get()) }
    factory { GetPendingRemindersUseCase(get()) }
    factory { CompleteReminderUseCase(get()) }
    factory { DeleteReminderUseCase(get()) }
    factory { RemindersViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
