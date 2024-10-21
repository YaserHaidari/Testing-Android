package com.example.assignment3.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.assignment3.R
import com.google.android.material.appbar.AppBarLayout

class SplashFrag : Fragment(R.layout.fragment_splash_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hide the toolbar
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(
                R.id.action_splashFrag_to_mainFragment,
                null, // No arguments
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.splashFrag, true) // Clear splashFrag from back stack
                    .build()
            )
        }, 3000) // 3-second delay
    }
}