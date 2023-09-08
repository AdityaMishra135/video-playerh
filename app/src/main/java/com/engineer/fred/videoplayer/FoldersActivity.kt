package com.engineer.fred.videoplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.engineer.fred.videoplayer.adapters.VideosAdapter
import com.engineer.fred.videoplayer.data.Folder
import com.engineer.fred.videoplayer.data.Video
import com.engineer.fred.videoplayer.databinding.ActivityFoldersBinding
import java.io.File

class FoldersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoldersBinding
    lateinit var videosAdapter: VideosAdapter

    companion object {
        var currentFolderVideos = ArrayList<Video>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme( MainActivity.currentTheme[ MainActivity.themeIndex ] )
        binding = ActivityFoldersBinding.inflate( layoutInflater )
        setContentView( binding.root )

        initialise()
    }

    private fun initialise(){
        if ( MainActivity.themeIndex == 1 ) setDarkModeUi()
        val folderPosition = intent.getIntExtra("folderPos", 0)
        currentFolderVideos = getAllVideos( FoldersFragment.foldersList[ folderPosition ].id )
        supportActionBar?.setDisplayHomeAsUpEnabled( true )
        supportActionBar?.title = FoldersFragment.foldersList[ folderPosition ].name
        videosAdapter = VideosAdapter( this,  currentFolderVideos, isFolder = true )
        binding.tvTotalVideosNumberCount.text = videosAdapter.itemCount.toString()
        setUpVideosRecyclerView()
    }

    private fun setUpVideosRecyclerView() {
        binding.videosRv.apply {
            adapter = videosAdapter
            layoutManager = LinearLayoutManager( this@FoldersActivity )
            setHasFixedSize( true )
            setItemViewCacheSize( 13 )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    @SuppressLint("Range")
    private fun getAllVideos( foldersId: String ): ArrayList<Video> {
        val tempVideosList = ArrayList<Video>()
        val selection = MediaStore.Video.Media.BUCKET_ID + " like? "
        val projection = arrayOf( MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA, MediaStore.Video.Media.DISPLAY_NAME  )

        val cursor = this.contentResolver.query( MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, arrayOf( foldersId ),
            MediaStore.Video.Media.DATE_ADDED + " DESC", null )
        cursor?.let {
            if (  it.moveToNext() ) {
                do {

                    val title =it.getString( it.getColumnIndex(  MediaStore.Video.Media.TITLE ))
                    val id =it.getString( it.getColumnIndex(  MediaStore.Video.Media._ID))
                    val folderName =it.getString( it.getColumnIndex(  MediaStore.Video.Media.BUCKET_DISPLAY_NAME ))
                    val duration =it.getLong( it.getColumnIndex(  MediaStore.Video.Media.DURATION)).toLong()
                    val size =it.getLong( it.getColumnIndex(  MediaStore.Video.Media.SIZE ))
                    val displayName = it.getString( it.getColumnIndex( MediaStore.Video.Media.DISPLAY_NAME ) )
                    val path =it.getString( it.getColumnIndex(  MediaStore.Video.Media.DATA ))
                    try {
                        val file = File( path )
                        val artUri = Uri.fromFile( file )
                        val video = Video( id, title, displayName, folderName, duration, size, path, artUri )
                        if ( file.exists() ) tempVideosList.add( video )
                    } catch (_:Exception) { }

                } while ( it.moveToNext() )
                it.close()
            }
        }
        return tempVideosList
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        videosAdapter.onResult( requestCode, resultCode )
    }

    private fun setDarkModeUi() {
        binding.tvTotalVideos.setTextColor(  ContextCompat.getColor( baseContext,   R.color.white ) )
        binding.tvTotalVideosNumberCount.setTextColor(  ContextCompat.getColor( baseContext,   R.color.white ) )
        binding.root.setBackgroundColor( ContextCompat.getColor( this,   R.color.dark_bg ) )
    }

}