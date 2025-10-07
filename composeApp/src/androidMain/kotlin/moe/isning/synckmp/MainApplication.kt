package moe.isning.synckmp

import android.app.Application
import io.github.oshai.kotlinlogging.KotlinLogging
import moe.isning.synckmp.di.commonModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

val logger = KotlinLogging.logger {  }
class MainApplication : Application() {
    init {
        System.setProperty("kotlin-logging-to-android-native", "true")
    }
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(commonModules)
        }
    }
}