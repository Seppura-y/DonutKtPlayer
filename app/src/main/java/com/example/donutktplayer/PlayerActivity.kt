package com.example.donutktplayer

import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.donutktplayer.databinding.ActivityPlayerBinding
import com.example.donutktplayer.databinding.BoosterBinding
import com.example.donutktplayer.databinding.MoreFeaturesBinding
import com.example.donutktplayer.databinding.SpeedDialogBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable
    private var isSubtitle: Boolean = true

    companion object{
        private lateinit var player: SimpleExoPlayer
        lateinit var playerList: ArrayList<VideoData>
        var position: Int = -1
        private var repeat: Boolean = false
        private var isFullscreen: Boolean = false
        private var isLocked: Boolean = false
        private lateinit var trackSelector: DefaultTrackSelector
        private lateinit var loudnessEnhancer: LoudnessEnhancer

        private var speed: Float = 1.0f
        private var timer: Timer? = null

        var pipStatus: Int = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 应用自定义主题
        // 需要在setContentView前调用
        setTheme(R.style.playerActivityTheme)

        // 隐藏标题栏
        // 需要在setContentView前调用
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 内容延伸至刘海区域
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 沉浸式 immersive mode
        // 需要在setContentView后调用
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let{controller->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            // 用户可以通过滑动屏幕边缘来临时显示系统栏
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        initializeLayout()
        initializeBinding()
    }

    private fun initializeLayout(){
        when(intent.getStringExtra("class")){
            "AllVideos"->{
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
                createPlayer()
            }
            "FolderActivity"->{
                playerList = ArrayList()
                playerList.addAll(FolderActivity.currentFolderVideos)
                createPlayer()
            }
        }

        if(repeat) binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
        else binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)
    }

    private fun initializeBinding(){
        binding.backBtn.setOnClickListener{
            finish()
        }
        binding.playPauseBtn.setOnClickListener {
            if(player.isPlaying) pauseVideo()
            else playVideo()
        }
        binding.previousBtn.setOnClickListener {
            nextPrevVideo(isNext = false)
        }
        binding.nextBtn.setOnClickListener {
            nextPrevVideo(isNext = true)
        }

        binding.repeatBtn.setOnClickListener {
            if(repeat){
                repeat = false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)
            }
            else{
                repeat = true
                player.repeatMode = Player.REPEAT_MODE_ALL
                binding.repeatBtn.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
            }
        }

        binding.fullscreenBtn.setOnClickListener {
            if(isFullscreen){
                isFullscreen = false
                playInFullscreen(false)
            }else{
                isFullscreen = true
                playInFullscreen(true)
            }
        }

        binding.lockBtn.setOnClickListener {
            if(!isLocked){
                isLocked = true;
                binding.playerView.hideController()
                binding.playerView.useController = false
                binding.lockBtn.setImageResource(R.drawable.lock_icon)
            }
            else{
                isLocked = false;
                binding.playerView.showController()
                binding.playerView.useController = true
                binding.lockBtn.setImageResource(R.drawable.unlock_icon)
            }
        }

        binding.moreFeaturesBtn.setOnClickListener {
            pauseVideo()
            val customDialog = LayoutInflater.from(this).inflate(R.layout.more_features, binding.root, false)
            val bindingMF = MoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener{ playVideo()}
                .setBackground(ColorDrawable(0x803700b3.toInt()))
                .create()
            dialog.show()

            bindingMF.audioTrack.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val audioTrack = java.util.ArrayList<String>()
                val audioList = java.util.ArrayList<String>()
                for(group in player.currentTracksInfo.trackGroupInfos){
                    if(group.trackType == C.TRACK_TYPE_AUDIO){
                        val groupInfo = group.trackGroup
                        for (i in 0 until groupInfo.length){
                            audioTrack.add(groupInfo.getFormat(i).language.toString())
                            audioList.add("${audioList.size + 1}. " + Locale(groupInfo.getFormat(i).language.toString()).displayLanguage
                                    + " (${groupInfo.getFormat(i).label})")
                        }
                    }
                }

                if(audioList[0].contains("null")) audioList[0] = "1. Default Track"

                val tempTracks = audioList.toArray(arrayOfNulls<CharSequence>(audioList.size))
                val audioDialog = MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select Language")
                    .setOnCancelListener { playVideo() }
                    .setPositiveButton("Off Audio"){ self, _ ->
                        trackSelector.setParameters(trackSelector.buildUponParameters().setRendererDisabled(
                            C.TRACK_TYPE_AUDIO, true
                        ))
                        self.dismiss()
                    }
                    .setItems(tempTracks){_, position ->
                        Snackbar.make(binding.root, audioList[position] + " Selected", 3000).show()
                        trackSelector.setParameters(trackSelector.buildUponParameters()
                            .setRendererDisabled(C.TRACK_TYPE_AUDIO, false)
                            .setPreferredAudioLanguage(audioTrack[position]))
                    }
                    .create()
                audioDialog.show()
                audioDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
                audioDialog.window?.setBackgroundDrawable(ColorDrawable(0x99000000.toInt()))
            }

            bindingMF.subtitleBtn.setOnClickListener {
                if(isSubtitle){
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this)
                        .setRendererDisabled(C.TRACK_TYPE_VIDEO, true)
                        .build()

                    Toast.makeText(this, "Subtitles off", Toast.LENGTH_SHORT).show()
                    isSubtitle = false
                }else{
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this)
                        .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                        .build()

                    Toast.makeText(this, "Subtitles on", Toast.LENGTH_SHORT).show()
                    isSubtitle = true
                }

                dialog.dismiss()
                playVideo()
            }

            bindingMF.audioBoosterBtn.setOnClickListener {
                dialog.dismiss()
                val customDialog = LayoutInflater.from(this).inflate(R.layout.booster, binding.root, false)
                val bindingBooster = BoosterBinding.bind(customDialog)
                val dialogBooster = MaterialAlertDialogBuilder(this).setView(customDialog)
                    .setOnCancelListener{ playVideo()}
                    .setPositiveButton("OK"){self, _->
                        loudnessEnhancer.setTargetGain(bindingBooster.verticalBar.progress * 100)
                        playVideo()
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x803700b3.toInt()))
                    .create()
                dialogBooster.show()
                bindingBooster.verticalBar.progress = loudnessEnhancer.targetGain.toInt() / 100
                bindingBooster.progressText.text = "Audio Boost\n\n${loudnessEnhancer.targetGain.toInt() / 10}%"
                bindingBooster.verticalBar.setOnProgressChangeListener {
                    bindingBooster.progressText.text = "Audio Boost\n\n${it*10}%"
                }
            }

            bindingMF.speedBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()

                val speedDialogView = LayoutInflater.from(this).inflate(R.layout.speed_dialog, binding.root, false)
                val bindingSpeed = SpeedDialogBinding.bind(speedDialogView)
                val speedDialog = MaterialAlertDialogBuilder(this).setView(speedDialogView)
                    .setCancelable(false)
                    .setPositiveButton("OK"){self, _->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x803700b3.toInt()))
                    .create()

                speedDialog.show()

                bindingSpeed.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                bindingSpeed.minusBtn.setOnClickListener {
                    changeSpeed(isIncrement = false)
                    bindingSpeed.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }

                bindingSpeed.plusBtn.setOnClickListener {
                    changeSpeed(isIncrement = true)
                    bindingSpeed.speedText.text = "${DecimalFormat("#.##").format(speed)} X"
                }
            }


            bindingMF.sleepTimerBtn.setOnClickListener {
                dialog.dismiss()
                if(timer != null) Toast.makeText(this, "Timer Already Running\nClose App to Reset TImer.", Toast.LENGTH_SHORT).show()
                else {
                    var sleepTime = 15

                    val speedDialogView = LayoutInflater.from(this).inflate(R.layout.speed_dialog, binding.root, false)
                    val bindingSpeed = SpeedDialogBinding.bind(speedDialogView)
                    val speedDialog = MaterialAlertDialogBuilder(this).setView(speedDialogView)
                        .setCancelable(false)
                        .setPositiveButton("OK"){self, _->
                            timer = Timer()
                            val task = object: TimerTask(){
                                override fun run() {
                                    moveTaskToBack(true)
                                    exitProcess(1)
                                }
                            }
                            timer!!.schedule(task, sleepTime * 1000.toLong())
                            self.dismiss()
                            playVideo()
                        }
                        .setBackground(ColorDrawable(0x803700b3.toInt()))
                        .create()

                    speedDialog.show()

                    bindingSpeed.speedText.text = "$sleepTime Min"
                    bindingSpeed.minusBtn.setOnClickListener {
                        if(sleepTime > 5) sleepTime -= 5
                        bindingSpeed.speedText.text = "$sleepTime Min"
                    }

                    bindingSpeed.plusBtn.setOnClickListener {
                        if(sleepTime < 120) sleepTime += 5
                        bindingSpeed.speedText.text = "$sleepTime Min"
                    }}
            } // bindingMF.sleepTimerBtn.setOnClickListener

            bindingMF.pipModeBtn.setOnClickListener(View.OnClickListener {
                val appOpsManager = getSystemService(APP_OPS_SERVICE) as AppOpsManager
                var status = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    status = appOpsManager.checkOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                        Process.myUid(),
                        packageName
                    ) == AppOpsManager.MODE_ALLOWED
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (status) {
                        enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        dialog.dismiss()
                        binding.playerView.hideController()
                        playVideo()
                        pipStatus = 0
                    } else {
                        val intent = Intent(
                            "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(applicationContext, "Feature Not Supported", Toast.LENGTH_SHORT)
                        .show()
                    dialog.dismiss()
                    playVideo()
                }
            }) // mf_binding.pipModeBtn.setOnClickListener

        }
    }
    private fun createPlayer(){
        try{player.release()}catch (e: Exception){}
        speed = 1.0f
        trackSelector = DefaultTrackSelector(this)
        binding.videoTitle.text = playerList[position].title
        // isSelected设置为true，才能开启滚动效果
        binding.videoTitle.isSelected = true
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player

        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
//        player.play()

        player.addListener(object : Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int){
                super.onPlaybackStateChanged(playbackState)
                if(playbackState == Player.STATE_ENDED && repeat) nextPrevVideo()
            }
        })

        playVideo()
        playInFullscreen(enable = isFullscreen)
        setVisibility()

        loudnessEnhancer = LoudnessEnhancer(player.audioSessionId)
        loudnessEnhancer.enabled = true
    }

    private fun playVideo(){
        binding.playPauseBtn.setImageResource(R.drawable.pause_icon)
        player.play()
    }

    private fun pauseVideo(){
        binding.playPauseBtn.setImageResource(R.drawable.play_icon)
        player.pause()
    }

    private fun nextPrevVideo(isNext: Boolean = true){
        if(isNext) setPosition(isIncrement = true)
        else setPosition(isIncrement = false)

        createPlayer()
    }

    private fun changeSpeed(isIncrement: Boolean){
        if(isIncrement){
            if(speed <= 2.9f){
                speed += 0.10f
            }
        }else{
            if(speed >= 0.2f) {
                speed -= 0.10f
            }
        }

        player.setPlaybackSpeed(speed)
    }

    private fun setPosition(isIncrement: Boolean = true){
        if(repeat){
            return
        }
        if(isIncrement)
        {
            if(playerList.size - 1 == position)
            {
                position = 0
            }
            else
            {
                ++position
            }
        }
        else
        {
          if(position == 0)
          {
              position = playerList.size - 1
          }
          else
          {
            --position
          }
        }
    }

    private fun playInFullscreen(enable: Boolean){
        if(enable){
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullscreenBtn.setImageResource(R.drawable.fullscreen_exit_icon)
        }
        else{
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullscreenBtn.setImageResource(R.drawable.fullscreen_icon)
        }
    }

    private fun setVisibility(){
        runnable = Runnable {
            if(binding.playerView.isControllerVisible){
                changeVisibility(View.VISIBLE)
            }
            else{
                changeVisibility(View.INVISIBLE)
            }

            // 继续调用runnable
            Handler(Looper.getMainLooper()).postDelayed(runnable, 30)
        }

        // 开始调用runnable
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun changeVisibility(visibility: Int){
        binding.topContorller.visibility = visibility
        binding.bottomContorller.visibility = visibility
        binding.playPauseBtn.visibility = visibility
        if(isLocked) binding.lockBtn.visibility = View.VISIBLE
        else binding.lockBtn.visibility = visibility
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (pipStatus != 0) {
            finish()
            val intent = Intent(applicationContext, PlayerActivity::class.java)
            when (pipStatus) {
                1 -> intent.putExtra("class", "FolderActivity")
                2 -> intent.putExtra("class", "AllVideos")
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        }
        else{
            playVideo()
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
        binding.playerView.hideController()
    }

    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}