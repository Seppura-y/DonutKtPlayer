package com.example.donutktplayer

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.donutktplayer.databinding.ActivityPlayerBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    companion object{
        private lateinit var player: SimpleExoPlayer
        lateinit var playerList: ArrayList<VideoData>
        var position: Int = -1
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

        playVideo()
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

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}