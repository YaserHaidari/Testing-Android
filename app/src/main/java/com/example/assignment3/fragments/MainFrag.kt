package com.example.assignment3.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment3.AddSongActivity
import com.example.assignment3.R
import com.example.assignment3.Song
import com.example.assignment3.SongAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainFrag : Fragment(R.layout.fragment_main), SongAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private lateinit var songList: MutableList<Song>
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the FAB
        val fabButton: FloatingActionButton = view.findViewById(R.id.fab)
        fabButton.setOnClickListener {
            val intent = Intent(requireContext(), AddSongActivity::class.java)
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
        database = FirebaseDatabase.getInstance().reference.child("songs")

        // Fetch songs from Firebase
        fetchSongsFromFirebase()
    }

    private fun fetchSongsFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                songList.clear()
                for (songSnapshot in snapshot.children) {
                    val song = songSnapshot.getValue(Song::class.java)
                    if (song != null) {
                        songList.add(song)
                    }
                }
                songAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load songs: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onItemClick(song: Song) {
        Toast.makeText(requireContext(), "Clicked: ${song.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onLikeClick(song: Song) {
        song.liked = !song.liked
        database.child(song.key).child("liked").setValue(song.liked).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                songAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(requireContext(), "Failed to update like status: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
        database.child(songKey).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Song deleted successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete song: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}