package android.cmcnall1.energiser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/*
    The Help Activity controls the app's Help view
 */

class HelpActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        //change the app theme to the intended theme
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        //Hide the Action Bar/Title Bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        setContentView(R.layout.help_page)
    }
}