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

        val tempList = ArrayList<String>()
        tempList.add("a folder")
        tempList.add("b folder")
        tempList.add("c folder")
        tempList.add("d folder")
        tempList.add("e folder")

        binding.folderRecyclerView.setHasFixedSize(true)
        binding.folderRecyclerView.setItemViewCacheSize(10)
        binding.folderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.folderRecyclerView.adapter = FolderAdapter(requireContext(), tempList)
        return binding.root
    }

}