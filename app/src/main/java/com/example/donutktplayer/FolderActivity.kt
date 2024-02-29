package com.example.donutktplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.example.donutktplayer.databinding.ActivityFolderBinding

class FolderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFolderBinding.inflate(layoutInflater)

        setTheme(R.style.coolPinkNav)
        setContentView(binding.root)

        val position = intent.getIntExtra("position", 0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = MainActivity.folderList[position].folderName
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true //返回true，表示菜单项的选择已经被处理完毕
    }
}