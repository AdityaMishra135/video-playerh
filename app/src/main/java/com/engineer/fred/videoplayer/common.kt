package com.engineer.fred.videoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.engineer.fred.videoplayer.data.Folder
import com.engineer.fred.videoplayer.data.Video
import java.io.File


class GetAllVideos( private val context : Context ) {
    @SuppressLint("Range")
    fun getAllVideos(): ArrayList<Video> {

        val sortList = arrayOf( MediaStore.Video.Media.DATE_ADDED + " DESC",  MediaStore.Video.Media.DATE_ADDED,  MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.TITLE + " DESC", MediaStore.Video.Media.SIZE,  MediaStore.Video.Media.SIZE + " DESC" )

        MainActivity.sortValue =  context.getSharedPreferences("SortOrder", AppCompatActivity.MODE_PRIVATE).getInt("sortValue", 0)

        val tempVideosList = ArrayList<Video>()
        val tempFoldersList = ArrayList<String>()
        val projection = arrayOf( MediaStore.Video.Media.TITLE, MediaStore.Video.Media.SIZE, MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DATA, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.DISPLAY_NAME  )

        val cursor = context.contentResolver.query( MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null,
            sortList[MainActivity.sortValue], null )
        cursor?.let {
            if (  it.moveToNext() ) {
                do {

                    val title =it.getString( it.getColumnIndex(  MediaStore.Video.Media.TITLE ))
                    val id =it.getString( it.getColumnIndex(  MediaStore.Video.Media._ID))
                    val folderName =it.getString( it.getColumnIndex(  MediaStore.Video.Media.BUCKET_DISPLAY_NAME ))
                    val folderId =it.getString( it.getColumnIndex(  MediaStore.Video.Media.BUCKET_ID ))
                    val duration =it.getLong( it.getColumnIndex(  MediaStore.Video.Media.DURATION))
                    val displayName = it.getString( it.getColumnIndex( MediaStore.Video.Media.DISPLAY_NAME ) )
                    val size =it.getLong( it.getColumnIndex(  MediaStore.Video.Media.SIZE ))
                    val path =it.getString( it.getColumnIndex(  MediaStore.Video.Media.DATA ))
                    try {
                        val file = File( path )
                        val artUri = Uri.fromFile( file )
                        val video = Video( id, title, displayName, folderName, duration, size, path, artUri )
                        if ( file.exists() ) tempVideosList.add( video )

                        //for adding folders
                        if ( !tempFoldersList.contains( folderName ) ) {
                            tempFoldersList.add( folderName )
                            val folder = Folder( folderId, folderName )
                            if ( !FoldersFragment.foldersList.contains( folder ) ) FoldersFragment.foldersList.add( folder )
                        }

                    } catch (_:Exception) { }

                } while ( it.moveToNext() )
                it.close()
            }
        }
        return tempVideosList
    }
}