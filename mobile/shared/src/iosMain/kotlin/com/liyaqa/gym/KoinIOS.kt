package com.liyaqa.gym

import com.liyaqa.gym.di.platformModule
import com.liyaqa.gym.di.sharedModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initialize Koin for iOS
 */
fun initKoinIos(appDeclaration: KoinAppDeclaration = {}): KoinApplication {
    return startKoin {
        appDeclaration()
        modules(
            sharedModule(enableLogging = true),
            platformModule
        )
    }
}

// Type alias for easier Swift interop
typealias KoinApplication = org.koin.core.KoinApplication
