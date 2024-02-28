package com.example.donutktplayer

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.icu.text.CaseMap.Fold
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.donutktplayer.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    companion object {
        // Used to load the 'donutktplayer' library on application startup.
        init {
            System.loadLibrary("donutktplayer")
        }

        lateinit var videoList: ArrayList<com.example.donutktplayer.Video>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.coolPinkNav)
        setContentView(binding.root)

//        requestRuntimePermission()

        // 程序一开始就打开drawer
//        binding.root.openDrawer(GravityCompat.START);

        //创建toggle对象，并给DrawerLayout添加toggle对象作为监听器，
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        //ActionBarDrawerToggle对象，它可以监听DrawerLayout的状态变化，并改变切换按钮的图标和描述
        binding.root.addDrawerListener(toggle)

        //根据DrawerLayout的当前状态，设置切换按钮的图标和描述
        toggle.syncState()

        //设置ActionBar的显示选项, true表示显示返回按钮
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(requestRuntimePermission() == true){
            videoList = getAllVideos()
            setFragment(VideosFragment())
        }


        binding.bottomNav.setOnItemSelectedListener { item ->
            Toast.makeText(this@MainActivity, "Item Clicked", Toast.LENGTH_SHORT).show()
            when (item.itemId) {
                R.id.videoView -> setFragment(VideosFragment())
                R.id.foldersView -> setFragment(FoldersFragment())
                else -> setFragment(Fragment())
            }
            return@setOnItemSelectedListener true
        }

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.feedBackNav -> Toast.makeText(this, "Feedback", Toast.LENGTH_SHORT).show()
                R.id.themesNav -> Toast.makeText(this, "Theme", Toast.LENGTH_SHORT).show()
                R.id.sortOrderNav -> Toast.makeText(this, "SortOrder", Toast.LENGTH_SHORT).show()
                R.id.aboutNav -> Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                R.id.exitNav -> exitProcess(1)
            }

            return@setNavigationItemSelectedListener true
        }
    }

    private fun setFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(binding.fragmentFrameLayout.id, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

    private fun requestRuntimePermission(): Boolean? {
        if (ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            val p = arrayOf(permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, p, 13)
            // 返回false，表示没有权限。
            return false
        }

        // 返回true，表示已经拥有权限
        return true
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
        if (requestCode == 13) {
            //检查授权结果数组（grantResults）是否为空，以及第一个元素是否等于PackageManager.PERMISSION_GRANTED。
            //如果是，表示用户同意了权限请求，使用Toast类，向用户显示提示信息“Permission Granted”。
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                //如果不是，表示用户拒绝了权限请求，它再次创建一个字符串数组，包含要请求的权限，然后使用ActivityCompat.requestPermissions方法，向用户再次发起权限请求。
                //这是为了让用户有机会重新考虑，或者在设置中手动开启权限。
                val p = arrayOf(permission.WRITE_EXTERNAL_STORAGE)
                ActivityCompat.requestPermissions(this, p, 13)
            }
        }
    }

    //活动的选项菜单被点击时触发
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Recycle", "InlineApi","Range")
//    @SuppressLint("Range")
    private fun getAllVideos(): ArrayList<com.example.donutktplayer.Video>{
        val tempList = ArrayList<com.example.donutktplayer.Video>()

        val projection = arrayOf(MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION)

        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
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
                        val video = com.example.donutktplayer.Video(title = titleC, id = idC, folderName = folderC, duration = durationC, size = sizeC,
                            path = pathC, artUri = artUriC)
                        if(file.exists()) tempList.add(video)

                    }catch (e:Exception){}

                }while (cursor.moveToNext())

                cursor?.close()
        return  tempList
    }
    external fun stringFromJNI(): String
}