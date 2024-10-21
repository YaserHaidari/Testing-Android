package com.example.assignment3.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment3.AddSongActivity
import com.example.assignment3.R
import com.example.assignment3.Song
import com.example.assignment3.SongAdapter
import com.example.assignment3.UsersActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainFrag : Fragment(R.layout.fragment_main), SongAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var songList: MutableList<Song>
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve user ID from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("unique_id", "") ?: ""

        // Initialize the FAB for adding songs
        val fabButton: FloatingActionButton = view.findViewById(R.id.fab)
        fabButton.setOnClickListener {
            val intent = Intent(requireContext(), AddSongActivity::class.java)
            startActivity(intent)
        }

        // Initialize the FAB for navigating to UsersActivity
        val fabProfileButton: FloatingActionButton = view.findViewById(R.id.fab_profile)
        fabProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), UsersActivity::class.java)
            startActivity(intent)
        }

        // Initialize the RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the song list and adapter
        songList = mutableListOf()
        songAdapter = SongAdapter(songList, this)
        recyclerView.adapter = songAdapter

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("songs")

        // Fetch songs from Firebase
        fetchSongsFromFirebase()
    }

    private fun fetchSongsFromFirebase() {
        lifecycleScope.launch {
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    database.get().await()
                }
                songList.clear()
                for (songSnapshot in snapshot.children) {
                    val song = songSnapshot.getValue(Song::class.java)
                    if (song != null) {
                        songList.add(song)
                    }
                }
                songAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load songs: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(song: Song) {
        Toast.makeText(requireContext(), "Clicked: ${song.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onLikeClick(song: Song) {
        lifecycleScope.launch {
            try {
                song.liked = !song.liked
                withContext(Dispatchers.IO) {
                    database.child(song.key).child("liked").setValue(song.liked).await()
                }
                songAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to update like status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onEditClick(songKey: String) {
        val bundle = Bundle().apply {
            putString("songKey", songKey)
        }
        findNavController().navigate(R.id.action_mainFragment_to_editSongFragment, bundle)
    }

    override fun onDeleteClick(songKey: String) {
        val song = songList.find { it.key == songKey }
        song?.let {
            view?.let { view ->
                Snackbar.make(view, "Confirm deleting ${song.name} by ${song.artist}?", Snackbar.LENGTH_LONG)
                    .setAction("Confirm") {
                        deleteSongFromFirebase(songKey)
                    }.show()
            }
        }
    }

    private fun deleteSongFromFirebase(songKey: String) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.child(songKey).removeValue().await()
                }
                Toast.makeText(requireContext(), "Song deleted successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to delete song: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}