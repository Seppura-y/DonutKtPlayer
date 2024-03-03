package com.example.donutktplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.donutktplayer.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var runnable: Runnable

    companion object{
        private lateinit var player: SimpleExoPlayer
        lateinit var playerList: ArrayList<VideoData>
        var position: Int = -1
        var repeat: Boolean = false
        var isFullscreen: Boolean = false
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
    }
    private fun createPlayer(){
        try{player.release()}catch (e: Exception){}
        binding.videoTitle.text = playerList[position].title
        // isSelected设置为true，才能开启滚动效果
        binding.videoTitle.isSelected = true
        player = SimpleExoPlayer.Builder(this).build()
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
            Handler(Looper.getMainLooper()).postDelayed(runnable, 300)
        }

        // 开始调用runnable
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    private fun changeVisibility(visibility: Int){
        binding.topContorller.visibility = visibility
        binding.bottomContorller.visibility = visibility
        binding.playPauseBtn.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}