package com.example.donutktplayer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.donutktplayer.databinding.ActivityFolderBinding
import java.io.File
import java.lang.Exception

class FolderActivity : AppCompatActivity() {

    companion object{
        lateinit var currentFolderVideos: ArrayList<VideoData>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFolderBinding.inflate(layoutInflater)

        setTheme(R.style.coolPinkNav)
        setContentView(binding.root)


        val position = intent.getIntExtra("position", 0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = MainActivity.folderList[position].folderName

        currentFolderVideos = getAllVideos(MainActivity.folderList[position].id)

        // init RecyclerView
        binding.videoRecyclerViewFragmentActivity.setHasFixedSize(true)
        binding.videoRecyclerViewFragmentActivity.setItemViewCacheSize(10)
        binding.videoRecyclerViewFragmentActivity.layoutManager = LinearLayoutManager(this@FolderActivity)
        binding.videoRecyclerViewFragmentActivity.adapter = VideoAdapter(this, currentFolderVideos)
        binding.totalVideos.text = "Total Videos: ${currentFolderVideos.size}"
        //LinearLayoutManager(this):
            //  这里的this关键字指的是当前的Context对象，通常是Activity的实例。
        //LinearLayoutManager(this@FolderActivity):
            // 这里的this@FolderActivity是一个限定的this，它明确指出this是FolderActivity的一个实例。
            // 在Kotlin中，当你在一个内部类或者匿名类中，需要引用外部类的this时，你会使用这种语法。
        //在Activity类的直接上下文中，这两种写法是等价的。
        // 但是，如果你在一个内部类或匿名类中，你需要使用this@FolderActivity来引用外部类的实例。如果你只写this，它会引用最近的类或者函数作用域。
        //这段代码是在FolderActivity类的直接上下文中，那么这两种写法没有区别。
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true //返回true，表示菜单项的选择已经被处理完毕
    }

    @SuppressLint("Recycle", "InlineApi","Range")
//    @SuppressLint("Range")
    private fun getAllVideos(folderId: String): ArrayList<VideoData>{
        val tempList = ArrayList<VideoData>()

        //selection提供了一个包含通配符的ID字符串，用于匹配任何包含"特定ID"的BUCKET_ID
        //创建了一个用于数据库查询的选择字符串。MediaStore.Video.Media.BUCKET_ID是Android媒体库数据库中的一个列名，它代表视频文件所在的文件夹的ID。
        // " like?"是一个SQL查询片段，用于指定一个模糊匹配条件。
        //当这个表达式用于contentResolver.query()或其他数据库查询方法时，你需要提供一个与" like?"相匹配的参数。这个参数通常是一个字符串，用于搜索与之相似的BUCKET_ID值。
        val selection = MediaStore.Video.Media.BUCKET_ID + " like?"

        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.BUCKET_ID)

        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, arrayOf(folderId),
            MediaStore.Video.Media.DATE_ADDED + " DESC")

        if(cursor != null)
            if(cursor.moveToNext())
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()

                    try{
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = VideoData(title = titleC, id = idC, folderName = folderC, duration = durationC, size = sizeC,
                            path = pathC, artUri = artUriC)
                        if(file.exists()) tempList.add(video)

                    }catch (e: Exception){}

                }while (cursor.moveToNext())

        cursor?.close()
        return  tempList
    }
}