package com.boudreau.guitarteacher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.boudreau.guitarteacher.adapters.LearnProgressViewPagerAdapter
import com.boudreau.guitarteacher.database.DbHelper
import com.boudreau.guitarteacher.managers.GtUserManager
import com.boudreau.guitarteacher.models.User
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LearnProgressActivity : AppCompatActivity() {

    // Database objects and logged in user
    lateinit var dbHelper: DbHelper
    private lateinit var loggedInUser: User

    // UI elements
    private lateinit var tabLearnProgress: TabLayout
    private lateinit var vpgLearnProgress: ViewPager2
    private lateinit var adapter: LearnProgressViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.learn_progress_activity)
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set the UI
        setUI()

        // Handle the selection of tabs
        TabLayoutMediator(tabLearnProgress, vpgLearnProgress) { selectedTab, position ->
            // Check the position of the selected tab
            when (position) {
                0 -> {
                    selectedTab.text = "Learn"
                }
                1 -> {
                    selectedTab.text = "Progress"
                }
                else -> {
                    selectedTab.text = "Learn"
                }
            }
        }.attach()
    }

    private fun setUI() {
        // Initialize the database helper
        dbHelper = DbHelper(this)

        // Set the logged in user
        loggedInUser =
                GtUserManager.getUserById(dbHelper, intent.extras!!.getInt(LOGGED_IN_USER_ID))!!

        // Get references to UI elements
        tabLearnProgress = findViewById(R.id.tabLearnProgress)
        vpgLearnProgress = findViewById(R.id.vpgLearnProgress)

        // Create an adapter of VPG2Adapter type passing it the supportFragmentManager and lifecycle
        adapter = LearnProgressViewPagerAdapter(supportFragmentManager, lifecycle)

        // Attach the adapter
        vpgLearnProgress.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_back -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}