package com.example.donutktplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.donutktplayer.databinding.FragmentVideosBinding

class VideosFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_videos, container, false)
//        val binding = FragmentVideosBinding.bind(view)

        val binding = FragmentVideosBinding.inflate(inflater, container, false)
        binding.videoRecyclerView.setHasFixedSize(true)
        binding.videoRecyclerView.setItemViewCacheSize(10)
        binding.videoRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.videoRecyclerView.adapter = VideoAdapter(requireContext(), MainActivity.videoList)
        return binding.root
//        return view
    }
}