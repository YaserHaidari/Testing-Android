package com.example.assignment3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SongAdapter(
    private val songs: List<Song>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(song: Song)
        fun onLikeClick(song: Song)
        fun onEditClick(songKey: String)
        fun onDeleteClick(songKey: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return SongViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentSong = songs[position]
        holder.songNameTextView.text = currentSong.name
        holder.artistNameTextView.text = currentSong.artist
        holder.albumNameTextView.text = currentSong.album
        holder.likeButton.setImageResource(if (currentSong.liked) R.drawable.heart_ic_filled else R.drawable.heart_ic)

        // Load the song image using Glide
        Glide.with(holder.itemView.context)
            .load(currentSong.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.songImageView)
    }

    override fun getItemCount() = songs.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songImageView: ImageView = itemView.findViewById(R.id.song_image)
        val songNameTextView: TextView = itemView.findViewById(R.id.song_name)
        val artistNameTextView: TextView = itemView.findViewById(R.id.artist_name)
        val albumNameTextView: TextView = itemView.findViewById(R.id.album_name)
        val likeButton: ImageButton = itemView.findViewById(R.id.like_button)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(songs[position].key)
                }
            }

            likeButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onLikeClick(songs[position])
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(songs[position].key)
                }
                true
            }
        }
    }
}