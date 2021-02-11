package android.cmcnall1.energiser

import com.google.android.apps.auto.sdk.CarActivity
import com.google.android.apps.auto.sdk.CarActivityService

class AutoService : CarActivityService() {
    override fun getCarActivity(): Class<out CarActivity> {
        return AutoActivity::class.java
    }
}