package com.vyvu.musikapp.view.adapter

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.R
import com.vyvu.musikapp.databinding.ItemMp3Binding
import com.vyvu.musikapp.model.Mp3
import com.vyvu.musikapp.service.MusikService
import com.vyvu.musikapp.utils.toTime

class Mp3ViewHolder(
    private val binding: ItemMp3Binding,
    private val context: Context
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: Mp3) {
        binding.run {
            textArtist.text = data.artist
            textTitle.text = data.title
            textDuration.text = data.duration.toTime()
            textAlbum.text = data.album
            imageThumbnail.setImageURI(data.thumbnailUri)
            buttonFavorite.setOnClickListener(object : View.OnClickListener {
                private var state = R.drawable.ic_round_favorite_border_32
                override fun onClick(v: View?) {
                    state = when (state) {
                        R.drawable.ic_round_favorite_border_32 -> R.drawable.ic_baseline_favorite_32
                        else -> R.drawable.ic_round_favorite_border_32
                    }
                    buttonFavorite.setImageResource(state)
                }
            })
            cardView.setOnClickListener {
                context.startService(
                    Intent(context, MusikService::class.java).apply {
                        putExtra(
                            AppVals.Action.INTENT_KEY_ACTIONS_CONTROL,
                            AppVals.Action.PLAY_PLAYLIST
                        )
                        putExtra(AppVals.Action.PLAY_PLAYLIST, data)
                    })
            }
        }
    }
}
