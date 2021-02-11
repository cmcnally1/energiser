package android.cmcnall1.energiser

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class SettingsFragment : Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val aboutButton: Button = view.findViewById(R.id.aboutButton)
        val helpButton: Button = view.findViewById(R.id.helpButton)
        val accountButton: Button = view.findViewById(R.id.accountButton)

        aboutButton.setOnClickListener {
            val intent = Intent(view.context, AboutActivity::class.java)
            view.context.startActivity(intent)
        }

        helpButton.setOnClickListener {
            val intent = Intent(view.context, HelpActivity::class.java)
            view.context.startActivity(intent)
        }

        accountButton.setOnClickListener {
            val intent = Intent(view.context, AccountActivity::class.java)
            view.context.startActivity(intent)
        }
        return view
    }

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}