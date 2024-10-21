package com.example.assignment3.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.assignment3.R
import java.util.UUID

class SplashFrag : Fragment(R.layout.fragment_splash_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if a unique ID already exists
        val uniqueID = getUniqueID(requireContext()) ?: UUID.randomUUID().toString().also {
            // Store the UUID in SharedPreferences if it doesn't exist
            saveUniqueID(requireContext(), it)
        }

        // Hide the toolbar and navigate after a delay
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

    // Function to save the unique ID in SharedPreferences
    private fun saveUniqueID(context: Context, uniqueID: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("unique_id", uniqueID)
            apply() // Use apply() for asynchronous storage
        }
    }

    // Function to retrieve the unique ID
    private fun getUniqueID(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("unique_id", null)
    }
}