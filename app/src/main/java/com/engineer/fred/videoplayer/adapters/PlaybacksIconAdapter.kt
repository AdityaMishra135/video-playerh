package com.engineer.fred.videoplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.engineer.fred.videoplayer.data.Icon
import com.engineer.fred.videoplayer.databinding.IconViewItemBinding

class PlaybacksIconAdapter( private val context : Context, private val iconsList: ArrayList<Icon> ) : Adapter<PlaybacksIconAdapter.IconViewHolder>(){

    private lateinit var onIconClickListener : OnIconClickListener

    fun setIconClickListener(  onIconClickListener : OnIconClickListener  ) {
        this.onIconClickListener = onIconClickListener
    }

    inner class IconViewHolder( private val binding: IconViewItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        fun bind( currentIcon : Icon , position: Int) {
            binding.playbackIconIV.setImageResource( currentIcon.image )
            binding.iconTitle.text = currentIcon.title
            binding.root.setOnClickListener {
                this@PlaybacksIconAdapter.onIconClickListener.performItemClick( position )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup , viewType: Int): IconViewHolder {
        val binding = IconViewItemBinding.inflate( LayoutInflater.from ( context ), parent, false )
        return IconViewHolder( binding )
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val currentIcon = iconsList[position]
        holder.bind(currentIcon, position )
    }

    override fun getItemCount(): Int {
        return iconsList.size
    }

    interface OnIconClickListener {
        fun performItemClick( position : Int )
    }
}