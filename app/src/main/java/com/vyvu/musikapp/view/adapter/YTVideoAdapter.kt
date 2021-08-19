package com.vyvu.musikapp.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.databinding.ItemYtVideoBinding
import com.vyvu.musikapp.model.Result

class YTVideoAdapter(private val context: Activity) : RecyclerView.Adapter<YTVideoViewHolder>() {
    var results = mutableListOf<Result>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YTVideoViewHolder =
        YTVideoViewHolder(
            ItemYtVideoBinding.inflate(LayoutInflater.from(context), parent, false),
            context,
        )

    override fun onBindViewHolder(holder: YTVideoViewHolder, position: Int) {
        holder.urls.clear()
        holder.bind(results[position], position)
    }

    override fun getItemCount(): Int = results.size

    companion object {
        var oldPos = AppVals.Code.NONE_CODE
        var oldButton: MaterialButton? = null
        var currentPlayingButton: MaterialButton? = null
    }
}
