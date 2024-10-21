package com.example.assignment3

import android.os.Parcel
import android.os.Parcelable

data class Song(
    val key: String = "",
    val name: String = "",
    val artist: String = "",
    val album: String = "",
    val imageUrl: String = "",
    var liked: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(album)
        parcel.writeString(imageUrl)
        parcel.writeByte(if (liked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Song> {
        override fun createFromParcel(parcel: Parcel): Song {
            return Song(parcel)
        }

        override fun newArray(size: Int): Array<Song?> {
            return arrayOfNulls(size)
        }
    }
}