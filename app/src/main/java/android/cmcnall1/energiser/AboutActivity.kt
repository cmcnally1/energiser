package android.cmcnall1.energiser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/*
    This activity controls the app's About view
 */

class AboutActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        //change the app theme to the intended theme
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        //Hide the Action Bar/Title Bar
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {}

        //Display the about page
        setContentView(R.layout.about_page)
    }
}