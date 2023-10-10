package com.animestudios.animeapp.navigation

import android.view.View
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment


class MyNavHostFragment: NavHostFragment() {
    private val containerId: Int
        get() {
            val id = id
            return if (id != 0 && id != View.NO_ID) {
                id
            } else androidx.navigation.fragment.R.id.nav_host_fragment_container
            // Fallback to using our own ID if this Fragment wasn't added via
            // add(containerViewId, Fragment)
        }

    override fun onCreateNavHostController(navHostController: NavHostController) {
        super.onCreateNavHostController(navHostController)
        navHostController.navigatorProvider.addNavigator(
            SwipeableFragmentNavigator(requireContext(), childFragmentManager, containerId)
        )
    }
}