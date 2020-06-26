package com.ccasey.getitdone.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ccasey.getitdone.R
import com.ccasey.getitdone.utils.FirebaseUtil
import kotlinx.android.synthetic.main.fragment_setttings.*

class SettingsFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setttings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.setTitle("Settings")

        signOutTV.setOnClickListener {
            FirebaseUtil.firebaseAuth.signOut()
            val intent = Intent(activity, LandingActivity::class.java)
            activity?.startActivity(intent)
        }
    }
}