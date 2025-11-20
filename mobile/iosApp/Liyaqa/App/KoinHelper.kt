package com.liyaqa.gym

import com.liyaqa.gym.di.platformModule
import com.liyaqa.gym.di.sharedModule
import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(
            sharedModule(enableLogging = true),
            platformModule
        )
    }
}
