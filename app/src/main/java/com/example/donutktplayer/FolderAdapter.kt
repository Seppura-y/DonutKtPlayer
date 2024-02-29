package com.example.donutktplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.donutktplayer.databinding.FolderViewBinding

class FolderAdapter(private val context: Context, private var folderList: ArrayList<FolderData>) : RecyclerView.Adapter<FolderAdapter.FolderHolder>() {
    class FolderHolder(binding: FolderViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val folderName = binding.folderNameFV
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderAdapter.FolderHolder {
        return FolderHolder(FolderViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }


    override fun onBindViewHolder(holder: FolderAdapter.FolderHolder, position: Int) {
        holder.folderName.text = folderList[position].folderName
    }

    override fun getItemCount(): Int {
        return folderList.size
    }
}