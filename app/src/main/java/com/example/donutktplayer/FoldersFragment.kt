package com.example.donutktplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.donutktplayer.databinding.FragmentFoldersBinding

class FoldersFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentFoldersBinding.inflate(inflater, container, false)

        binding.folderRecyclerView.setHasFixedSize(true)
        binding.folderRecyclerView.setItemViewCacheSize(10)
        binding.folderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.folderRecyclerView.adapter = FolderAdapter(requireContext(), MainActivity.folderList)
        binding.totalFolders.text = "Total Folders: ${MainActivity.folderList.size}"
        return binding.root
    }

}