package com.boudreau.guitarteacher.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boudreau.guitarteacher.LearnFragment
import com.boudreau.guitarteacher.ProgressTrackerFragment

class LearnProgressViewPagerAdapter(fragmentManager: FragmentManager, lifeCycle: Lifecycle) :
                                FragmentStateAdapter(fragmentManager, lifeCycle) {
    override fun getItemCount(): Int {
        // Return 2, since there are only 2 options for the tab layout
        return 2
    }

    // Overridden createFragment method to handle selection of tab or page change
    override fun createFragment(position: Int): Fragment {
        // Check position and returns the required fragment
        return when (position) {
            0 -> {
                // Return a learn activity fragment
                LearnFragment()
            }
            1 -> {
                // If position is 1 return a progress tracker fragment
                ProgressTrackerFragment()
            }
            else -> {
                // Default to learn activity fragment
                LearnFragment()
            }
        }
    }
}