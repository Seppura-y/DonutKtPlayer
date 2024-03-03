package com.example.donutktplayer

import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.donutktplayer.databinding.ActivityPlayerBinding
import com.example.donutktplayer.databinding.MoreFeaturesBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    companion object{
        private lateinit var player: SimpleExoPlayer
        lateinit var playerList: ArrayList<VideoData>
        var position: Int = -1
        var repeat: Boolean = false
        var isFullscreen: Boolean = false
        var isLocked: Boolean = false
        lateinit var trackSelector: DefaultTrackSelector
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

                val audioTrack = ArrayList<String>()
                for(i in 0 until player.currentTrackGroups.length){
                    if(player.currentTrackGroups.get(i).getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT){
                        audioTrack.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }

                val tempTracks = audioTrack.toArray(arrayOfNulls<CharSequence>(audioTrack.size))
                MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select a Audio Track")
                    .setOnCancelListener{ playVideo()}
                    .setBackground(ColorDrawable(0x803700b3.toInt()))
                    .setItems(tempTracks){_, position->
                        Toast.makeText(this, audioTrack[position] + "Selected", Toast.LENGTH_SHORT).show()
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTrack[position]))
                    }
                    .create()
                    .show()
            }
        }
    }
    private fun createPlayer(){
        try{player.release()}catch (e: Exception){}
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

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}