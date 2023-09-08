import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

//package com.engineer.fred.videoplayer
//
//val view=LayoutInflater.from(this).inflate(R.layout.more_features , binding.root , false)
//val bindingMF=MoreFeaturesBinding.bind(view)
//val dialog=MaterialAlertDialogBuilder(this)
//    .setView(view)
//    .setBackground(ColorDrawable(0x80191A1C.toInt()))
//    .create()
//dialog.show()
//bindingMF.audioTrackBtn.setOnClickListener {
//    dialog.dismiss()
//
//    val audioTracks=ArrayList<String>()
//    val audioList=ArrayList<String>()
//
//    for (group in player.currentTracks.groups) {
//        if (group.type == C.TRACK_TYPE_AUDIO) {
//            val groupInfo=group.mediaTrackGroup
//            for (i in 0 until groupInfo.length) {
//                audioTracks.add(groupInfo.getFormat(i).language.toString())
//                audioList.add(
//                    "${audioList.size + 1}. ${Locale(groupInfo.getFormat(i).language.toString()).displayLanguage} ( ${
//                        groupInfo.getFormat(
//                            i
//                        ).label
//                    } )"
//                )
//            }
//        }
//    }
//
//    if (audioList[0].contains("null")) audioList[0]="1. Default Track"
//    val tempTracks=audioList.toArray(arrayOfNulls<CharSequence>(audioList.size))
//    val audioTrackD=MaterialAlertDialogBuilder(this , R.style.AlertDialogTheme)
//        .setTitle("Select Language ")
//        .setOnCancelListener {
//            playVideo()
//        }
//        .setPositiveButton("Off Audio") { dialog , _ ->
//            defaultTrackSelector.setParameters(
//                defaultTrackSelector.buildUponParameters()
//                    .setRendererDisabled(C.TRACK_TYPE_AUDIO , true)
//            )
//            dialog.dismiss()
//        }
//        .setItems(tempTracks) { _ , position ->
////                       Toast.makeText(this, "${ audioTracks[position]} selected!", Toast.LENGTH_LONG ).show()
//            defaultTrackSelector.setParameters(
//                defaultTrackSelector.buildUponParameters()
//                    .setRendererDisabled(C.TRACK_TYPE_AUDIO , false)
//                    .setPreferredAudioLanguage(audioTracks[position])
//            )
//        }
//        .setBackground(ColorDrawable(0x80191A1C.toInt()))
//        .create()
//    audioTrackD.show()
//    audioTrackD.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
//}
//
//bindingMF.subtitlesBtn.setOnClickListener {
//    dialog.dismiss()
//
//    val subtitles=ArrayList<String>()
//    val subtitlesList=ArrayList<String>()
//    for (group in player.currentTracks.groups) {
//        if (group.type == C.TRACK_TYPE_VIDEO) {
//            val groupInfo=group.mediaTrackGroup
//            for (i in 0 until groupInfo.length) {
//                subtitles.add(groupInfo.getFormat(i).language.toString())
//                subtitlesList.add(
//                    "${subtitlesList.size + 1}. ${
//                        Locale(
//                            groupInfo.getFormat(
//                                i
//                            ).language.toString()
//                        ).displayLanguage
//                    } ( ${groupInfo.getFormat(i).label} )"
//                )
//            }
//        }
//    }
//    if (subtitlesList[0].contains("null")) subtitlesList[0]="No Subtitles"
//    val tempSubtitles=
//        subtitlesList.toArray(arrayOfNulls<CharSequence>(subtitlesList.size))
//    val sD=MaterialAlertDialogBuilder(this , R.style.AlertDialogTheme)
//        .setTitle("Select Subtitle ")
//        .setItems(tempSubtitles) { _ , position ->
////                        Snackbar.make( binding.root, "${ subtitles[position]} selected!", Snackbar.LENGTH_LONG ).show()
//            defaultTrackSelector.setParameters(
//                defaultTrackSelector.buildUponParameters()
//                    .setRendererDisabled(C.TRACK_TYPE_VIDEO , false)
//                    .setPreferredTextLanguage(subtitles[position])
//            )
//        }
//        .setPositiveButton("Off Subtitles") { dialog , _ ->
//            defaultTrackSelector.setParameters(
//                defaultTrackSelector.buildUponParameters()
//                    .setRendererDisabled(C.TRACK_TYPE_VIDEO , true)
//            )
//            dialog.dismiss()
//        }
//        .setBackground(ColorDrawable(0x80191A1C.toInt()))
//        .create()
//    sD.show()
//    sD.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
//
//}
//
//bindingMF.audioBoosterBtn.setOnClickListener {
//    dialog.dismiss()
//    playVideo()
//    val boosterView=
//        LayoutInflater.from(this).inflate(R.layout.audio_booster , binding.root , false)
//    val boosterBinding=AudioBoosterBinding.bind(boosterView)
//    boosterBinding.verticalBar.progress=loudnessEnhancer.targetGain.toInt() / 100
//    boosterBinding.progressText.text=
//        "Audio Boost\n\n${loudnessEnhancer.targetGain.toInt() / 10} %"
//    boosterBinding.verticalBar.setOnProgressChangeListener {
//        boosterBinding.progressText.text="Audio Boost\n\n${it * 10} %"
//        loudnessEnhancer.setTargetGain(boosterBinding.verticalBar.progress * 100)
//    }
//    val boosterDialog=MaterialAlertDialogBuilder(this)
//        .setView(boosterView)
//        .setOnCancelListener {
////                        hideSystemUIOnWindowFocusChange( binding.root )
//        }
//        .setPositiveButton("OK") { dialog , _ ->
//            dialog.dismiss()
////                        hideSystemUIOnWindowFocusChange( binding.root )
//        }
//        .setBackground(ColorDrawable(0x80191A1C.toInt()))
//        .create()
//    boosterDialog.show()
//    boosterDialog.getButton(AlertDialog.BUTTON_POSITIVE)
//        .setBackgroundColor(ContextCompat.getColor(this , R.color.copperfield))
//    boosterDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
//}
//
//bindingMF.speedBtn.setOnClickListener {
//    dialog.dismiss()
//    playVideo()
//    val speedView=
//        LayoutInflater.from(this).inflate(R.layout.speed_dialog , binding.root , false)
//    val speedVBinding=SpeedDialogBinding.bind(speedView)
////                speedVBinding.tvSpeed.text = "${DecimalFormat("#.##").format(speed)} x" //initlialising the speed text view with the current speed
//
//    speedVBinding.minusBtn.setOnClickListener {
//        changeSpeed(false)
//        speedVBinding.tvSpeed.text="${DecimalFormat("#.##").format(speed)} x"
//    }
//    speedVBinding.addBtn.setOnClickListener {
//        changeSpeed()
//        speedVBinding.tvSpeed.text="${DecimalFormat("#.##").format(speed)} x"
//    }
//
//    val speedDialog=MaterialAlertDialogBuilder(this)
//        .setView(speedView)
//        .setOnCancelListener {
////                        hideSystemUIOnWindowFocusChange( binding.root )
//            playVideo()
//        }
//        .setPositiveButton("OK") { dialog , _ ->
//            dialog.dismiss()
////                        hideSystemUIOnWindowFocusChange( binding.root )
//        }
//        .setBackground(ColorDrawable(0x80191A1C.toInt()))
//        .create()
//    speedDialog.show()
//    speedDialog.getButton(AlertDialog.BUTTON_POSITIVE)
//        .setBackgroundColor(ContextCompat.getColor(this , R.color.copperfield))
//    speedDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
//    speedVBinding.tvSpeed.text="${DecimalFormat("#.##").format(speed)} x"
//}
//
//bindingMF.sleepTimerBtn.setOnClickListener {
//    if (timer != null) {
//        Toast.makeText(this , "Timer already running!" , LENGTH_LONG).show()
//        dialog.dismiss()
//        playVideo()
////                    hideSystemUIOnWindowFocusChange( binding.root )
//    } else {
//        dialog.dismiss()
//        var sleepTime=15
//        val sleepView=LayoutInflater.from(this)
//            .inflate(R.layout.speed_dialog , binding.root , false)
//        val sleepVBinding=SpeedDialogBinding.bind(sleepView)
//
//        sleepVBinding.tvSpeed.text="$sleepTime min "
//
//        sleepVBinding.minusBtn.setOnClickListener {
//            if (sleepTime > 15) {
//                sleepTime-=15
//                sleepVBinding.tvSpeed.text="$sleepTime min "
//            }
//        }
//        sleepVBinding.addBtn.setOnClickListener {
//            if (sleepTime < 120) {
//                sleepTime+=15
//                sleepVBinding.tvSpeed.text="$sleepTime min "
//            }
//        }
//
//        val sleepDialog=MaterialAlertDialogBuilder(this)
//            .setView(sleepView)
//            .setPositiveButton("OK") { dialog , _ ->
//                timer=Timer()
//                val task=object : TimerTask() {
//                    override fun run() {
//                        moveTaskToBack(true)
//                        exitProcess(1)
//                    }
//                }
//                timer?.schedule(task , sleepTime * 60 * 1000.toLong())
//                dialog.dismiss()
//                playVideo()
//            }
//            .setBackground(ColorDrawable(0x80191A1C.toInt()))
//            .create()
//        sleepDialog.show()
//        sleepDialog.getButton(AlertDialog.BUTTON_POSITIVE)
//            .setBackgroundColor(ContextCompat.getColor(this , R.color.copperfield))
//        sleepDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
//    }
//}
//
//bindingMF.pipModeBtn.setOnClickListener {
//    val appOps=getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//    val status=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        appOps.checkOpNoThrow(
//            AppOpsManager.OPSTR_PICTURE_IN_PICTURE ,
//            android.os.Process.myUid() ,
//            packageName
//        ) == AppOpsManager.MODE_ALLOWED
//    } else {
//        true
//    }
//
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        if (status) {
//            this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
//            dialog.dismiss()
//            binding.playerView.hideController()
//            playVideo()
//            pipStatus=0
//        } else {
//            val i=Intent(
//                "android.settings.PICTURE_IN_PICTURE_SETTINGS" ,
//                Uri.parse("package:${packageName}")
//            )
//            startActivity(i)
//        }
//    } else {
//        Toast.makeText(
//            applicationContext ,
//            "Your device doesn't support this feature!" ,
//            LENGTH_LONG
//        ).show()
//    }
//}


//    override fun onPictureInPictureModeChanged(
//        isInPictureInPictureMode : Boolean ,
//        newConfig : Configuration
//    ) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            super.onPictureInPictureModeChanged(isInPictureInPictureMode , newConfig)
//        }
//        if (pipStatus != 0) {
//            finish()
//            val i=Intent(this , PlayerActivity::class.java)
//            when (pipStatus) {
//                1 -> intent.putExtra("class" , "FoldersActivity")
//                2 -> intent.putExtra("class" , "SearchedVideos")
//                3 -> intent.putExtra("class" , "AllVideos")
//            }
//            startActivity(i)
//        }
//        if (! isInPictureInPictureMode) pauseVideo()
//    }


//******
//if ( grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
//    Toast.makeText( this, "Permission Granted!", Toast.LENGTH_LONG ).show()
//    initializeMALayout()
//} else {
//    MaterialAlertDialogBuilder( this )
//        .setTitle("Grant Permission")
//        .setMessage("Permission is required inorder to load your video files!")
//        .setPositiveButton("Grant Permission"){ dialog, _ ->
//            ActivityCompat.requestPermissions( this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), storageRequestCode )
//            dialog.dismiss()
//        }
//}
