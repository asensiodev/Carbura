package com.asensiodev.carbura.feature.reminders.di

import com.asensiodev.carbura.core.domain.reminder.usecase.CompleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.CreateReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.DeleteReminderUseCase
import com.asensiodev.carbura.core.domain.reminder.usecase.GetPendingRemindersUseCase
import com.asensiodev.carbura.core.model.FamilyId
import com.asensiodev.carbura.feature.reminders.presentation.RemindersViewModel
import org.koin.dsl.module

val remindersModule = module {
    factory { CreateReminderUseCase(get(), get()) }
    factory { GetPendingRemindersUseCase(get()) }
    factory { CompleteReminderUseCase(get(), get()) }
    factory { DeleteReminderUseCase(get(), get()) }
    factory { parameters ->
        RemindersViewModel(
            familyId = parameters.get<FamilyId>(),
            vehicleRepository = get(),
            dispatchers = get(),
            createReminderUseCase = get(),
            getPendingRemindersUseCase = get(),
            completeReminderUseCase = get(),
            deleteReminderUseCase = get(),
            syncManager = get(),
        )
    }
}
