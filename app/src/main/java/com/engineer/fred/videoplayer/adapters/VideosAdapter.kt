package com.engineer.fred.videoplayer.adapters


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
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
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

class VideosAdapter(  private val  context: Context, private var videoList: ArrayList<Video>, private var isFolder: Boolean = false ) : Adapter<VideosAdapter.VideoViewHolder>() {

    private var newPosition = 0
    private lateinit var renameDialog: AlertDialog

    inner class VideoViewHolder(private val binding : VideoViewBinding) : ViewHolder(binding.root) {
        @SuppressLint("NotifyDataSetChanged")
        fun bind(currentVideo : Video , position : Int) {
            binding.videoTitle.text=currentVideo.title
            binding.videoSizeTv.text=
                android.text.format.Formatter.formatFileSize(context , currentVideo.size)
            binding.videoDurationTv.text=DateUtils.formatElapsedTime(currentVideo.duration / 1000)
            Glide.with(context).asBitmap().load(currentVideo.playUri)
                .apply(RequestOptions().placeholder(R.drawable.video_24).centerCrop())
                .into(binding.thumbnailIV)
            binding.root.setOnClickListener {
                when {
                    isFolder -> sendIntent(position , "FoldersActivity")
                    VideosFragment.isSearching -> sendIntent(position , "SearchedVideos")
                    else -> sendIntent(position , "AllVideos")
                }
            }
            binding.verticalMenu.setOnClickListener {
                newPosition = position
                val bottomSheetDialog=BottomSheetDialog(context , R.style.BottomSheetTheme)
                val bsView=LayoutInflater.from(context).inflate(R.layout.video_bottom_sheet_layout , binding.root , false)
                bottomSheetDialog.setContentView(bsView)
                bottomSheetDialog.show()
                val bsBinding=VideoBottomSheetLayoutBinding.bind(bsView)
                bsBinding.videoPlayLA.setOnClickListener {
                    binding.root.performClick()
                    bottomSheetDialog.dismiss()
                }
                bsBinding.videoRenameLA.setOnClickListener {
                    requestWriteR( newPosition )
                    bottomSheetDialog.dismiss()
                }
                bsBinding.videoShareLA.setOnClickListener {

                    val videoUri=Uri.parse(videoList[position].path)
                    val shareIntent=Intent(ACTION_SEND)
                    shareIntent.type="video/*"
                    shareIntent.putExtra(Intent.EXTRA_STREAM , videoUri)
                    val chooser=createChooser(shareIntent , "Share video via...")
                    context.startActivity(chooser)

                    bottomSheetDialog.dismiss()

                }
                bsBinding.videoDeleteLA.setOnClickListener {
                   delete( newPosition )
                    bottomSheetDialog.dismiss()
                }
                bsBinding.videoInfoLA.setOnClickListener {
                    val path=videoList[position].path // here your get the full video path with video name and extension
                    val indexOfPath=path.lastIndexOf("/")

                    val displayName=videoList[position].displayName
                    val index=displayName.lastIndexOf(".")
                    val format=displayName.substring(index + 1)

                    val mediaMetaDataRetriever=MediaMetadataRetriever()
                    mediaMetaDataRetriever.setDataSource(videoList[position].path)
                    val alertDialog=MaterialAlertDialogBuilder(context)
                        .setTitle("Properties!")
                        .setMessage(
                            "" +
                                    "Title: ${videoList[position].title}\n\n" +
                                    "Location: ${path.substring(0 , indexOfPath)}\n\n" +
                                    "Size: ${
                                        android.text.format.Formatter.formatFileSize(
                                            context ,
                                            videoList[position].size
                                        )
                                    }\n\n" +
                                    "Length: ${DateUtils.formatElapsedTime(videoList[position].duration / 1000)}\n\n" +
                                    "Format: ${format}\n\n" +
                                    "Resolution: ${
                                        mediaMetaDataRetriever.extractMetadata(
                                            MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                                        )
                                    }x${
                                        mediaMetaDataRetriever.extractMetadata(
                                            MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                                        )
                                    }"
                        )
                        .setCancelable(false)
                        .setPositiveButton("OK") { dialog , _ ->
                            dialog.dismiss()
                        }.create()
                    alertDialog.show()
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                    bottomSheetDialog.dismiss()
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent : ViewGroup ,
        viewType : Int
    ) : VideosAdapter.VideoViewHolder {
        val binding=VideoViewBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        if (MainActivity.themeIndex == 1) {
            binding.videoTitle.setTextColor(ContextCompat.getColor(context , R.color.white))
            binding.videoDurationTv.setTextColor(ContextCompat.getColor(context , R.color.white))
            binding.verticalMenu.setColorFilter(Color.WHITE)
            binding.root.background=
                AppCompatResources.getDrawable(context , R.drawable.dark_ripple_effect)
            binding.videoSizeTv.setTextColor(ContextCompat.getColor(context , R.color.white))
        }
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder : VideosAdapter.VideoViewHolder , position : Int) {
        val currentVideo=videoList[position]
        holder.bind(currentVideo , position)
    }

    override fun getItemCount() : Int {
        return videoList.size
    }

    private fun sendIntent(position : Int , ref : String) {
        val i=Intent(context , PlayerActivity::class.java)
        i.putExtra("videoPos" , position)
        i.putExtra("class" , ref)
        ContextCompat.startActivity(context , i , null)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateVideoList(list : ArrayList<Video>) {
        videoList=ArrayList()
        videoList.addAll(list)
        notifyDataSetChanged()
    }

    //for requesting android 11 or higher storage permission
    fun delete( position : Int ) {
        //list of videos to delete
        val uriList=listOf<Uri>(
            Uri.withAppendedPath(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI ,
                videoList[position].id
            )
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //that's android 11 and above
            val pi=MediaStore.createDeleteRequest(context.contentResolver , uriList)
            ( context as Activity).startIntentSenderForResult(pi.intentSender , 123 , null , 0 , 0 , 0 , null)
        }
        else {
            val deleteDialog=MaterialAlertDialogBuilder(context)
                .setTitle("DELETE!")
                .setMessage(videoList[position].title)
                .setPositiveButton("Delete") { dialog , _ ->
                    val file=File(videoList[position].path)
                    if (file.delete()) {
                        //if file is deleted
                        MediaScannerConnection.scanFile(context , arrayOf(file.path) , arrayOf("video/*") , null)
                       updateDeleteUi( position )
                        dialog.dismiss()
                    } else Toast.makeText(context , "Something went wrong!" , Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel") { dialog , _ -> dialog.dismiss() }.create()
            deleteDialog.show()
            deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            deleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
        }
    }

    fun updateDeleteUi( position : Int ) {
        when {
            VideosFragment.isSearching -> {
                VideosFragment.dataChanged = true
                VideosFragment.searchList.remove( videoList[position] )
                notifyItemRemoved( position )
            }

            isFolder -> {
                VideosFragment.dataChanged = true
                FoldersActivity.currentFolderVideos.remove( videoList[position] )
                notifyDataSetChanged()
            }

            else -> {
                VideosFragment.videosList.remove(  VideosFragment.videosList[position]  )
                notifyDataSetChanged()
            }
        }
        Toast.makeText(context , "Video deleted!" , Toast.LENGTH_SHORT).show()
    }
    private fun updateRenameUi( position : Int, newFile: File, userInput: String ) {
        when {
            VideosFragment.isSearching -> {
                //updates the video list
                VideosFragment.searchList[position].title=userInput
                VideosFragment.searchList[position].path = newFile.path
                VideosFragment.searchList[position].playUri=Uri.fromFile(newFile)
                notifyItemChanged( position )
            }

            isFolder -> {
                //updates the video list
                VideosFragment.dataChanged=true
                FoldersActivity.currentFolderVideos[position].title=userInput
                FoldersActivity.currentFolderVideos[position].path = newFile.path
                FoldersActivity.currentFolderVideos[position].playUri=Uri.fromFile(newFile)
                notifyDataSetChanged()
            }

            else -> {
                //updates the video list
                VideosFragment.videosList[position].title=userInput
                VideosFragment.searchList[position].path = newFile.path
                VideosFragment.videosList[position].playUri=Uri.fromFile(newFile)
                notifyItemChanged( position )
            }
        }

        Toast.makeText(context.applicationContext , "Video renamed successfully!" , Toast.LENGTH_LONG).show()
    }

    private fun requestWriteR( position : Int ) {
        //list of videos to delete
        val uriList=listOf<Uri>(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI , videoList[position].id))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //that's android 11 and above
            val pi=MediaStore.createWriteRequest(context.contentResolver , uriList)
            ( context as Activity).startIntentSenderForResult(pi.intentSender , 321 , null , 0 , 0 , 0 , null)
        } else renameVideo( position )
    }
    private fun renameVideo( position : Int ) {
        val editText=EditText(context)
        val videoPath=videoList[position].path   //selected video path
        val file=File(videoPath)
        var videoName=file.name  //gets the name of the file
        videoName=videoName.substring(0 , videoName.lastIndexOf("."))  //removes the extension type on the name of the video
        editText.setText(videoName)
        editText.requestFocus()

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ) {

            val renameDialog=MaterialAlertDialogBuilder(context)
                .setCancelable(false)
                .setTitle("Rename to")
                .setView(editText)
                .setPositiveButton("OK") { self , _ ->
                    val onlyPath=file.parentFile?.absolutePath
                    var extension=file.absolutePath
                    extension=extension.substring(extension.lastIndexOf("."))  // we have to keep the extension of the current video
                    val userInput=editText.text.toString()

                    if (userInput.isNotEmpty()) {
                        val newPath="${onlyPath}/${userInput}${extension}"
                        val newFile=File(newPath)

                        val uri = Uri.withAppendedPath( MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoList[ position ].id )

                        ContentValues().also {
                            it.put( MediaStore.Files.FileColumns.IS_PENDING, 1 )
                            context.contentResolver.update( uri, it, null, null )
                            it.clear()

                            //updating file details
                            it.put( MediaStore.Files.FileColumns.DISPLAY_NAME, userInput )
                            it.put( MediaStore.Files.FileColumns.IS_PENDING, 0 )
                            context.contentResolver.update( uri, it, null, null )
                        }

                        updateRenameUi( position, newFile, userInput )
                    } else {
                        Toast.makeText(context.applicationContext , "No new name was provided!" , Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel") { self , _ ->
                    self.dismiss()
                }
                .create()
            renameDialog.show()
            renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)


        } else {

            val renameDialog=MaterialAlertDialogBuilder(context)
                .setCancelable(false)
                .setTitle("Rename to")
                .setView(editText)
                .setPositiveButton("OK") { self , _ ->
                    val onlyPath=file.parentFile?.absolutePath
                    var extension=file.absolutePath
                    extension=extension.substring(extension.lastIndexOf("."))  // we have to keep the extension of the current video
                    val userInput=editText.text.toString()

                    if (userInput.isNotEmpty()) {
                        val newPath="${onlyPath}/${userInput}${extension}"
                        val newFile=File(newPath)

                        if (file.renameTo(newFile)) {
                            //if the video is renamed
                            val resolver=context.applicationContext.contentResolver
                            resolver.delete(MediaStore.Files.getContentUri("external") , MediaStore.MediaColumns.DATA + "=?" , arrayOf(file.absolutePath))
                            val intent=Intent(ACTION_MEDIA_SCANNER_SCAN_FILE)
                            intent.data=Uri.fromFile(newFile)
                            context.applicationContext.sendBroadcast(intent)
                            updateRenameUi( position, newFile, userInput )
                            self.dismiss()
                        } else {
                            //if the video is not renamed
                            Toast.makeText(context.applicationContext , "Something went wrong!" , Toast.LENGTH_LONG).show()
                            self.dismiss()
                        }
                    } else {
                        Toast.makeText(context.applicationContext , "No new name was provided!" , Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel") { self , _ ->
                    self.dismiss()
                }
                .create()
            renameDialog.show()
            renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
            renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

        }
    }

    fun onResult( requestCode : Int , resultCode : Int ) {
        if ( requestCode == 123 ) { //deleting
            if ( resultCode == Activity.RESULT_OK ) updateDeleteUi( newPosition )
        } else if( requestCode == 321 ) { //renaming
            if ( resultCode == Activity.RESULT_OK ) requestWriteR( newPosition )
        }
    }

}

