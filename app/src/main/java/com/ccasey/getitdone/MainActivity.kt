package com.ccasey.getitdone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ccasey.getitdone.ui.AchievementsFragment
import com.ccasey.getitdone.ui.HistoryFragment
import com.ccasey.getitdone.ui.RunningFragment
import com.ccasey.getitdone.ui.SettingsFragment
import com.ccasey.getitdone.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(RunningFragment())

        bottomNavBar.selectedItemId = R.id.menuRun
        bottomNavBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuSettings -> loadFragment(SettingsFragment())
                R.id.menuAchievements -> loadFragment(AchievementsFragment())
                R.id.menuHistory -> loadFragment(HistoryFragment())
                R.id.menuRun -> loadFragment(RunningFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }
}
