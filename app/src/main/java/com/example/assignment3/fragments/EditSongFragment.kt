package com.example.assignment3.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.assignment3.R
import com.example.assignment3.Song
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class EditSongFragment : Fragment(R.layout.fragment_edit_song) {

    private lateinit var etSongName: EditText
    private lateinit var etArtistName: EditText
    private lateinit var etAlbumName: EditText
    private lateinit var btnSave: Button
    private lateinit var database: DatabaseReference
    private var songKey: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etSongName = view.findViewById(R.id.et_song_name)
        etArtistName = view.findViewById(R.id.et_artist_name)
        etAlbumName = view.findViewById(R.id.et_album_name)
        btnSave = view.findViewById(R.id.btn_submit)

        database = FirebaseDatabase.getInstance().reference.child("songs")

        songKey = arguments?.getString("songKey")
        if (songKey != null) {
            database.child(songKey!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val song = snapshot.getValue(Song::class.java)
                    song?.let {
                        etSongName.setText(it.name)
                        etArtistName.setText(it.artist)
                        etAlbumName.setText(it.album)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load song: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "Song key is null", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            updateSongInFirebase()
        }
    }

    private fun updateSongInFirebase() {
        val updatedSong = mapOf(
            "name" to etSongName.text.toString(),
            "artist" to etArtistName.text.toString(),
            "album" to etAlbumName.text.toString()
        )

        if (songKey != null) {
            database.child(songKey!!).updateChildren(updatedSong).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Song updated successfully!", Toast.LENGTH_SHORT).show()
//                    findNavController().navigate(R.id.editSongFragment_to_main)

                } else {
                    Toast.makeText(requireContext(), "Failed to update song: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Song key is null", Toast.LENGTH_SHORT).show()
        }
    }
}