package android.cmcnall1.energiser

import android.app.Application
import org.koin.android.ext.android.startKoin

//Application class needed for dependency injection
class Energiser: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(koin))
    }
}