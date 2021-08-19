package com.vyvu.musikapp.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vyvu.musikapp.databinding.ItemMp3Binding
import com.vyvu.musikapp.model.Mp3

class Mp3Adapter(private val context: Context) : RecyclerView.Adapter<Mp3ViewHolder>() {
    var mp3s = mutableListOf<Mp3>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Mp3ViewHolder =
        Mp3ViewHolder(
            ItemMp3Binding.inflate(LayoutInflater.from(context), parent, false),
            context
        )

    override fun onBindViewHolder(holder: Mp3ViewHolder, position: Int) {
        holder.bind(mp3s[position])
    }

    override fun getItemCount(): Int = mp3s.size

}
