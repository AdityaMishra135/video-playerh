package com.engineer.fred.videoplayer.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
import android.content.Intent.ACTION_SEND
import android.content.Intent.createChooser
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.engineer.fred.videoplayer.FoldersActivity
import com.engineer.fred.videoplayer.MainActivity
import com.engineer.fred.videoplayer.PlayerActivity
import com.engineer.fred.videoplayer.R
import com.engineer.fred.videoplayer.VideosFragment
import com.engineer.fred.videoplayer.data.Video
import com.engineer.fred.videoplayer.databinding.VideoBottomSheetLayoutBinding
import com.engineer.fred.videoplayer.databinding.VideoViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class VideosAdapterPL(private val  context: Context , private var videoList: ArrayList<Video> , private var isFolder: Boolean = false ) : Adapter<VideosAdapterPL.VideoViewHolder>() {

    private lateinit var playlistItemClick : PlaylistItemClick

    fun setPlaylistAdapterItemClick( playlistItemClick : PlaylistItemClick ) {
        this.playlistItemClick = playlistItemClick
    }

    inner class VideoViewHolder(private val binding: VideoViewBinding ) : ViewHolder( binding.root ) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(currentVideo: Video, position: Int) {
            binding.videoTitle.text = currentVideo.title
            binding.videoSizeTv.text = android.text.format.Formatter.formatFileSize( context, currentVideo.size )
            binding.videoDurationTv.text = DateUtils.formatElapsedTime(currentVideo.duration / 1000)
            binding.verticalMenu.visibility = View.GONE
            Glide.with(context).asBitmap().load(currentVideo.playUri ).apply(RequestOptions().placeholder(R.drawable.video_24).centerCrop()).into( binding.thumbnailIV )
            binding.root.setOnClickListener {
                playlistItemClick.playlistItemClicked( position )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosAdapterPL.VideoViewHolder {
        val binding = VideoViewBinding.inflate( LayoutInflater.from(  parent.context ), parent, false )
        binding.videoTitle.setTextColor( ContextCompat.getColor( context, R.color.white  ) )
        binding.videoDurationTv.setTextColor( ContextCompat.getColor( context, R.color.white )  )
        binding.root.background = AppCompatResources.getDrawable( context, R.drawable.dark_ripple_effect )
        binding.videoSizeTv.setTextColor(  ContextCompat.getColor( context, R.color.white )  )
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideosAdapterPL.VideoViewHolder , position: Int) {
        val currentVideo = videoList[ position ]
        holder.bind( currentVideo, position )
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    private fun sendIntent( position: Int, ref: String  ) {
        val i = Intent( context, PlayerActivity::class.java )
        i.putExtra( "videoPos", position )
        i.putExtra( "class", ref )
        ContextCompat.startActivity( context, i, null )
    }

    interface PlaylistItemClick {
        fun playlistItemClicked( position : Int )
    }

}