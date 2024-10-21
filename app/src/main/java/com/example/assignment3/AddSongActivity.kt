package com.example.assignment3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class AddSongActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var songName: EditText
    private lateinit var songArtist: EditText
    private lateinit var songAlbum: EditText
    private lateinit var songImage: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_song)

        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference

        songName = findViewById(R.id.et_song_name)
        songArtist = findViewById(R.id.et_artist_name)
        songAlbum = findViewById(R.id.et_album_name)
        songImage = findViewById(R.id.iv_song_image)
        val selectImageBtn = findViewById<Button>(R.id.btn_select_image)
        val submitBtn = findViewById<Button>(R.id.btn_submit)

        selectImageBtn.setOnClickListener {
            selectImage()
        }

        submitBtn.setOnClickListener {
            uploadImageAndPostSongData()
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            songImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageAndPostSongData() {
        val songNameValue = songName.text.toString().trim()
        val songArtistValue = songArtist.text.toString().trim()
        val songAlbumValue = songAlbum.text.toString().trim()

        // Check for empty fields
        if (songNameValue.isEmpty() || songArtistValue.isEmpty() || songAlbumValue.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a unique key for each song entry
        val songKey = database.child("songs").push().key
        if (songKey == null) {
            Toast.makeText(this, "Failed to generate song key", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRef = storageReference.child("images/$songKey.jpg")
        imageRef.putFile(selectedImageUri!!).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val songData = Song(
                    key = songKey,
                    name = songNameValue,
                    artist = songArtistValue,
                    album = songAlbumValue,
                    imageUrl = uri.toString()
                )

                // Write to the database
                database.child("songs").child(songKey).setValue(songData).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Song added successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after successful submission
                    } else {
                        Log.e("FirebaseError", "Failed to add song: ${task.exception?.message}")
                        Toast.makeText(this, "Failed to add song: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_IMAGE_PICK = 1
    }
}