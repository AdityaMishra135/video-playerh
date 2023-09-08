package com.engineer.fred.videoplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.TimeBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView
import com.engineer.fred.videoplayer.adapters.PlaybacksIconAdapter
import com.engineer.fred.videoplayer.adapters.VideosAdapter
import com.engineer.fred.videoplayer.adapters.VideosAdapterPL
import com.engineer.fred.videoplayer.data.Icon
import com.engineer.fred.videoplayer.data.Video
import com.engineer.fred.videoplayer.databinding.ActivityPlayerBinding
import com.engineer.fred.videoplayer.databinding.PlaylistBottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.Timer


class PlayerActivity : AppCompatActivity() , AudioManager.OnAudioFocusChangeListener ,
    PlaybacksIconAdapter.OnIconClickListener, VideosAdapterPL.PlaylistItemClick {

    private lateinit var binding : ActivityPlayerBinding
    private lateinit var playbacksIconAdapter : PlaybacksIconAdapter
    private lateinit var videosAdapterPL : VideosAdapterPL
    private lateinit var iconsList : ArrayList<Icon>
    private lateinit var bottomSheetDialog : BottomSheetDialog
    private lateinit var popupMenu : PopupMenu
    private lateinit var videosAdapter : VideosAdapter

    private lateinit var playPauseBtn : ImageView
    private lateinit var tvVideoTitle : TextView
    private lateinit var backBtn : ImageView
    private lateinit var playListBtn : ImageView
    private lateinit var prevBtn : ImageView
    private lateinit var videoMoreMenu : ImageView
    private lateinit var nextBtn : ImageView
    private lateinit var topController : LinearLayout
    private lateinit var bottomController : LinearLayout
    private lateinit var rootControllerLA : ConstraintLayout
    private lateinit var videoScalingBtn : ImageView
    private lateinit var lockVideoBtn : ImageView
    private lateinit var lockedVideoBtn : ImageView
    private lateinit var progressBar : ProgressBar
    private lateinit var iconsRecyclerView : RecyclerView
    private var expanded=false
    private var nightModeOn=false
    private var muted = false
    private var repeatOn = false

    companion object {
        var audioManager : AudioManager?=null
        lateinit var player : ExoPlayer
        lateinit var playerList : ArrayList<Video>
        var videoPosition=0
        var isPlaying=false
        var repeat=false
        var isFullScreen=false
        var isLocked=false

        @SuppressLint("StaticFieldLeak")
        var speed=1.0f
        var timer : Timer?=null
        var pipStatus=0
    }

    private fun setControllerButtons() {
        playPauseBtn=findViewById(R.id.playPauseBtn)
        videoMoreMenu = findViewById(R.id.video_more_menu_btn)
        videoScalingBtn=findViewById(R.id.resize_btn)
        tvVideoTitle=findViewById(R.id.tvVideoTitle)
        backBtn=findViewById(R.id.back_btn)
        playListBtn =findViewById(R.id.play_list_btn )
        prevBtn=findViewById(R.id.prevBtn)
        nextBtn=findViewById(R.id.nextBtn)
        progressBar=findViewById(R.id.progressBar)
        topController=findViewById(R.id.topController)
        bottomController=findViewById(R.id.bottom_controller)
        rootControllerLA=findViewById(R.id.root_controllerLA)
        lockVideoBtn=findViewById(R.id.lock_video_btn)
        lockedVideoBtn=findViewById(R.id.locked_video_btn)
        iconsRecyclerView=findViewById(R.id.playback_icons_RV)
    }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setFullScreen()

        if (intent.data?.scheme.contentEquals("content")) playVideoFromPhoneStorage()
        else initialiseLayout()
    }

    @SuppressLint("Range")
    private fun playVideoFromPhoneStorage() {
        videoPosition=0
        playerList=ArrayList()
        val cursor=intent.data?.let {
            contentResolver.query(
                it ,
                arrayOf(MediaStore.Video.Media.DATA) ,
                null ,
                null ,
                null
            )
        }

        cursor?.let {
            it.moveToFirst() //without it application crushes
            val videoPath=it.getString(it.getColumnIndex(MediaStore.Video.Media.DATA))
            val file=File(videoPath)
            val video=Video(
                "" ,
                file.name ,
                duration=0L ,
                playUri=Uri.fromFile(file) ,
                path=videoPath ,
                folderName="" ,
                displayName=""
            )
            playerList.add(video)
            it.close()
        }
        createPlayer()
        initialiseLayout()
    }

    @SuppressLint("SourceLockedOrientationActivity" , "UnsafeOptInUsageError")
    private fun initialiseLayout() {

        nightModeOn =! nightModeOn
        repeatOn =! repeatOn

        setControllerButtons()

        iconsList=ArrayList()
        //horizontal recycler view variables
        iconsList.add(Icon(R.drawable.baseline_keyboard_arrow_right_24 , ""))
        if ( binding.nightMode.visibility  == View.VISIBLE ) iconsList.add(Icon(R.drawable.light_mode_24 , "Day")) else iconsList.add(Icon(R.drawable.night_mode , "Night"))
        iconsList.add( Icon(R.drawable.volume_off , "Mute"))
        iconsList.add(Icon(R.drawable.baseline_screen_rotation , "Rotate"))
        iconsList.add(Icon(R.drawable.baseline_repeat_24 , "Repeat"))
        //horizontal recycler view variables
        playbacksIconAdapter=PlaybacksIconAdapter(this , iconsList)
        playbacksIconAdapter.setIconClickListener(this)

        iconsRecyclerView.adapter=playbacksIconAdapter
        iconsRecyclerView.layoutManager=LinearLayoutManager(this , HORIZONTAL , true)


        videoPosition=intent.getIntExtra("videoPos" , 0)
        when (intent.getStringExtra("class")) {
            "AllVideos" -> {
                playerList=ArrayList()
                playerList.addAll(VideosFragment.videosList)
                createPlayer()
            }

            "FoldersActivity" -> {
                playerList=ArrayList()
                playerList.addAll(FoldersActivity.currentFolderVideos)
                createPlayer()
            }

            "SearchedVideos" -> {
                playerList=ArrayList()
                playerList.addAll(VideosFragment.searchList)
                createPlayer()
            }

            "NowPlaying" -> {
                videoPosition=intent.getIntExtra("pos" , 0)
                createPlayer()
            }
        }
        videosAdapterPL = VideosAdapterPL( this, playerList )
        videosAdapterPL.setPlaylistAdapterItemClick( this )
        screenOrientation()
        videosAdapter = VideosAdapter( this, playerList )
        clickEvents()
    }

    @SuppressLint("SetTextI18n" , "SourceLockedOrientationActivity" , "UnsafeOptInUsageError")
    private fun clickEvents() {
        backBtn.setOnClickListener { finish() }
        playPauseBtn.setOnClickListener {
            if (isPlaying) pauseVideo() else playVideo()
        }
        nextBtn.setOnClickListener { preNext() }
        prevBtn.setOnClickListener { preNext(false) }

        videoMoreMenu.setOnClickListener {
            popupMenu = PopupMenu( this, videoMoreMenu )
            popupMenu.inflate( R.menu.player_menu )
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.next -> {
                        nextBtn.performClick()
                    }

                    R.id.share -> {
                        val videoUri=Uri.parse(playerList[videoPosition].path)
                        val shareIntent=Intent(Intent.ACTION_SEND)
                        shareIntent.type="video/*"
                        shareIntent.putExtra(Intent.EXTRA_STREAM , videoUri)
                        val chooser=Intent.createChooser(shareIntent , "Share video via...")
                        startActivity(chooser)
                    }

                    R.id.properties -> {
                        val path=
                            playerList[videoPosition].path // here your get the full video path with video name and extension
                        val indexOfPath=path.lastIndexOf("/")

                        val displayName=playerList[videoPosition].displayName
                        val index=displayName.lastIndexOf(".")
                        val format=displayName.substring(index + 1)

                        val mediaMetaDataRetriever=MediaMetadataRetriever()
                        mediaMetaDataRetriever.setDataSource(playerList[videoPosition].path)
                        val alertDialog=MaterialAlertDialogBuilder(this@PlayerActivity)
                            .setTitle("Properties!")
                            .setMessage(
                                "" +
                                        "Title: ${playerList[videoPosition].title}\n\n" +
                                        "Location: ${path.substring(0 , indexOfPath)}\n\n" +
                                        "Size: ${
                                            android.text.format.Formatter.formatFileSize(
                                                this@PlayerActivity ,
                                                playerList[videoPosition].size
                                            )
                                        }\n\n" +
                                        "Length: ${DateUtils.formatElapsedTime(playerList[videoPosition].duration / 1000)}\n\n" +
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
                    }

                    R.id.delete -> {
                        //list of videos to delete
                        val uriList=listOf<Uri>(Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI , playerList[videoPosition].id))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            //that's android 11 and above
                            val pi=MediaStore.createDeleteRequest( contentResolver , uriList)
                            startIntentSenderForResult(pi.intentSender , 123 , null , 0 , 0 , 0 , null)
                        }
                        else {
                            val deleteDialog=MaterialAlertDialogBuilder(this)
                                .setTitle("DELETE!")
                                .setMessage(playerList[videoPosition].title)
                                .setPositiveButton("Delete") { dialog , _ ->
                                    val file=File(playerList[videoPosition].path)
                                    if (file.delete()) {
                                        //if file is deleted
                                        nextBtn.performClick()
                                        VideosFragment.dataChanged = true
                                        MediaScannerConnection.scanFile(this , arrayOf(file.path) , arrayOf("video/*") , null)
                                        Toast.makeText( this , "Video was deleted!" , Toast.LENGTH_LONG).show()
                                        dialog.dismiss()
                                    } else Toast.makeText( this , "Something went wrong!" , Toast.LENGTH_LONG).show()
                                }
                                .setNegativeButton("Cancel") { dialog , _ -> dialog.dismiss() }.create()
                            deleteDialog.show()
                            deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
                            deleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
                        }
                    }
                }
                true
            }
            setFullScreen()
        }

        lockVideoBtn.setOnClickListener {
            rootControllerLA.visibility=View.INVISIBLE
            lockedVideoBtn.visibility=View.VISIBLE
            isLocked=true
            Toast.makeText(applicationContext , "Locked!" , Toast.LENGTH_SHORT).show()
        }
        lockedVideoBtn.setOnClickListener {
            rootControllerLA.visibility=View.VISIBLE
            binding.playerView.useController=true
            isLocked=false
            lockedVideoBtn.visibility=View.GONE
            Toast.makeText(applicationContext , "UnLocked!" , Toast.LENGTH_SHORT).show()
        }

        videoScalingBtn.setOnClickListener {
            if (isFullScreen) {
                isFullScreen=false
                playInFullScreen(false)
            } else {
                isFullScreen=true
                playInFullScreen(true)
            }
        }
        playListBtn.setOnClickListener {
            bottomSheetDialog = BottomSheetDialog( this )
            val view = LayoutInflater.from( this ).inflate( R.layout.playlist_bottom_sheet_layout, binding.root, false )
            val bindingBs=PlaylistBottomSheetLayoutBinding.bind( view )
            bottomSheetDialog.setContentView( view )
            bottomSheetDialog.create()
            bottomSheetDialog.show()
            bottomSheetDialog.setOnCancelListener { setFullScreen() }
            bindingBs.playListRv.adapter = videosAdapterPL
            bindingBs.playListRv.layoutManager = LinearLayoutManager( this )

            setFullScreen()
        }
    }

    private fun playVideo() {
        isPlaying=true
        player.play()
        playPauseBtn.setImageResource(R.drawable.baseline_pause_24)
        tvVideoTitle.isSelected=true
    }

    private fun pauseVideo() {
        isPlaying=false
        player.pause()
        playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_24)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun createPlayer() {
        player = ExoPlayer.Builder(this).build()
        val mediaItem=MediaItem.fromUri(playerList[videoPosition].playUri)
        binding.playerView.player=player
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
        moveToNextVideoOnCompletion()
        tvVideoTitle.text = playerList[videoPosition].title
        playInFullScreen(isFullScreen)
        seekBarFeature()

    }

    private fun moveToNextVideoOnCompletion() {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState : Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    player.release()
                    preNext()
                }
                if (playbackState == ExoPlayer.STATE_BUFFERING) showProgress() else hideProgress()
            }
        })
    }

    private fun hideProgress() {
        progressBar.visibility=View.INVISIBLE
    }

    private fun showProgress() {
        progressBar.visibility=View.VISIBLE
    }

    private fun preNext(isNext : Boolean=true) {
        if ( isNext ) {
            player.release()
            incrementPosition()
            createPlayer()
        } else {
            player.release()
            incrementPosition(false)
            createPlayer()
        }
    }

    private fun incrementPosition(increment : Boolean=true) {
        if (! repeat) {
            if (increment) {
                if ((playerList.size - 1) == videoPosition) videoPosition=0 else videoPosition ++
            } else {
                if (videoPosition == 0) videoPosition=(playerList.size - 1) else videoPosition --
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun playInFullScreen(enabled : Boolean) {
        if (enabled) {
            binding.playerView.resizeMode=AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            player.videoScalingMode=C.VIDEO_SCALING_MODE_DEFAULT
            videoScalingBtn.setImageResource(R.drawable.baseline_zoom_in_map_24)
        } else {
            binding.playerView.resizeMode=AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode=C.VIDEO_SCALING_MODE_DEFAULT
            videoScalingBtn.setImageResource(R.drawable.baseline_zoom_out_map_24)
        }
    }

    private fun changeSpeed(increment : Boolean=true) {
        if (increment) {
            if (speed <= 2.9f) speed+=0.10f
        } else {
            if (speed > 0.20f) speed-=0.10f
        }
        player.setPlaybackSpeed(speed)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.pause()
        audioManager?.abandonAudioFocus(this)
    }

    override fun onAudioFocusChange(focus : Int) {
        if (focus <= 0) pauseVideo()
    }

    override fun onResume() {
        super.onResume()
        setFullScreen()
        screenOrientation()
        if (audioManager == null) audioManager=getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager !!.requestAudioFocus(this , AudioManager.STREAM_MUSIC , AudioManager.AUDIOFOCUS_GAIN)

        if (! player.isPlaying) {
            playVideo()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        player.release()
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun seekBarFeature() {
        findViewById<DefaultTimeBar>(R.id.exo_progress).addListener(object :
            TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar : TimeBar , position : Long) {
                pauseVideo()
            }

            override fun onScrubMove(timeBar : TimeBar , position : Long) {
                player.seekTo(position)
            }

            override fun onScrubStop(timeBar : TimeBar , position : Long , canceled : Boolean) {
                playVideo()
            }
        })
    }

    private fun setFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window , false)
        WindowInsetsControllerCompat(window , binding.playerView).let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior=WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        if ( requestCode == 2022 ) {
            if ( resultCode == RESULT_OK ) return
        }
    }

    @SuppressLint("UnsafeOptInUsageError" , "SourceLockedOrientationActivity")
    override fun performItemClick(position : Int) {
        when (position) {
            5 -> {
                val equalizerIntent = Intent( AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL )
                if ( equalizerIntent.resolveActivity( packageManager ) != null  ) {
                    equalizerIntent.putExtra( AudioEffect.EXTRA_AUDIO_SESSION, player.audioSessionId )
                    equalizerIntent.putExtra( AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName )
                    equalizerIntent.putExtra( AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    startActivityForResult( equalizerIntent, 2022 )
                } else {
                    Toast.makeText( applicationContext, "No built in equalizer was found on this device!.", LENGTH_LONG  ).show()
                }
                playbacksIconAdapter.notifyDataSetChanged()
            }
            6 -> {
                Toast.makeText( applicationContext, "Will implement this one later!", Toast.LENGTH_SHORT ).show()
            }
            4 -> {
                if ( repeatOn ) {
                    repeatOn = false
                    iconsList[position ] = Icon( R.drawable.baseline_repeat_one_24, "Repeat on" )
                    player.repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    playbacksIconAdapter.notifyDataSetChanged()
                } else {
                    repeatOn = true
                    iconsList[position ] = Icon( R.drawable.baseline_repeat_24, "Repeat" )
                    player.repeatMode = ExoPlayer.REPEAT_MODE_OFF
                    playbacksIconAdapter.notifyDataSetChanged()
                }
            }
            3 -> {
                if (  resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT ) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }
            2 -> {
                if ( !muted ) {
                    muted = true
                    player.volume = 0F
                    iconsList[position ] = Icon( R.drawable.baseline_volume_up_24, "Unmute" )
                    playbacksIconAdapter.notifyDataSetChanged()
                } else {
                    muted = false
                    player.volume = 1F
                    iconsList[ position ] = Icon( R.drawable.volume_off, "Mute" )
                    playbacksIconAdapter.notifyDataSetChanged()
                }
            }
            1 -> {
                if ( nightModeOn ) {
                    nightModeOn= false
                    binding.nightMode.visibility = View.VISIBLE
                    iconsList[position]=Icon(R.drawable.light_mode_24 , "Day")
                    playbacksIconAdapter.notifyDataSetChanged()
                } else {
                    nightModeOn=true
                    binding.nightMode.visibility = View.GONE
                    iconsList[position]=Icon(R.drawable.night_mode , "Night")
                    playbacksIconAdapter.notifyDataSetChanged()
                }
            }
            0 -> {
                binding.playerView.showController()
                if (! expanded) {
                    expanded=true
                    if (iconsList.size == 5 ) {
                        iconsList.add(Icon(R.drawable.equalizer , "Equalizer"))
                        iconsList.add(Icon(R.drawable.baseline_picture_in_picture , "Pip"))
                    }
                    iconsList[position]=Icon(R.drawable.chevron_left_24 , "")
                    playbacksIconAdapter.notifyDataSetChanged()
                } else {
                    expanded=false
                    iconsList.clear()
                    iconsList.add(Icon(R.drawable.baseline_keyboard_arrow_right_24 , ""))
                    if ( binding.nightMode.visibility  == View.VISIBLE ) iconsList.add(Icon(R.drawable.light_mode_24 , "Day")) else iconsList.add(Icon(R.drawable.night_mode , "Night"))
                    if ( player.volume == 0F ) iconsList.add( Icon(R.drawable.baseline_volume_up_24 , "Unmute")) else iconsList.add( Icon(R.drawable.volume_off , "Mute"))
                    iconsList.add(Icon(R.drawable.baseline_screen_rotation , "Rotate"))
                    if ( player.repeatMode == ExoPlayer.REPEAT_MODE_ONE ) iconsList.add(  Icon( R.drawable.baseline_repeat_one_24, "Repeat on" ) )  else  iconsList.add(Icon(R.drawable.baseline_repeat_24 , "Repeat"))
                    playbacksIconAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun screenOrientation() {
       try {
           val videoPath = playerList[ videoPosition ].path
           val uri = Uri.parse( videoPath )

           val retriever = MediaMetadataRetriever()
           retriever.setDataSource( this, uri )

           val bitmap = retriever.frameAtTime

           val videoWidth = bitmap!!.width
           val videoHeight = bitmap.height

           if ( videoWidth > videoHeight) {
               requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
           }  else {
               requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
           }

       } catch ( ex: Exception ) { return }

    }

    override fun playlistItemClicked( position : Int) {
        bottomSheetDialog.dismiss()
        setFullScreen()
        videoPosition = position
        if ( player.isPlaying ) {
            player.release()
            createPlayer()
        }
    }
}