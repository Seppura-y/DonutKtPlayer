package com.example.donutktplayer

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.donutktplayer.databinding.VideoViewBinding

//写法1
class VideoAdapter(private val context: Context, private var videoList: ArrayList<VideoData>) : RecyclerView.Adapter<VideoAdapter.VideoHolder>() {
    class VideoHolder(binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoName
        val folder = binding.folderName
        val duration = binding.duration
        val image = binding.videoImage
    }

    // 写法1
    // parent: 指的是回收器视图
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoAdapter.VideoHolder {
        return VideoHolder(VideoViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }


    override fun onBindViewHolder(holder: VideoAdapter.VideoHolder, position: Int) {
        holder.title.text = videoList[position].title
        holder.folder.text = videoList[position].folderName
        holder.duration.text = DateUtils.formatElapsedTime(videoList[position].duration/1000)
        Glide.with(context)
            .asBitmap()
            .load(videoList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.ic_video_player).centerCrop())
            .into(holder.image)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }
}

// 写法2
//class VideoAdapter(private val context: Context, private var videoList: ArrayList<String>) : RecyclerView.Adapter<VideoAdapter.VideoHolder>() {
//    class VideoHolder(binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root) {
//        val title = binding.videoName
//
//        companion object {
//            fun inflateFrom(parent: ViewGroup): VideoHolder {
//                val layoutInflater = LayoutInflater.from(parent.context)
//                val binding = VideoViewBinding.inflate(layoutInflater, parent, false)
//                return VideoHolder(binding)
//            }
//        }
//    }
//
//    // parent: 指的是回收器视图
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder = VideoHolder.inflateFrom(parent)
//
//    override fun onBindViewHolder(holder: VideoAdapter.VideoHolder, position: Int) {
//        holder.title.text = videoList[position]
//    }
//
//    override fun getItemCount(): Int {
//        return videoList.size
//    }
//}