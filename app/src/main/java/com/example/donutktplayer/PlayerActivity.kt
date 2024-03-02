package com.example.donutktplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
    }
    private fun createPlayer(){
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

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}