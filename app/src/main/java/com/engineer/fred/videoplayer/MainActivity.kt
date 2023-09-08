package com.engineer.fred.videoplayer

import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.lang.UCharacter.GraphemeClusterBreak.V
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.engineer.fred.videoplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var currentFragment: Fragment
    private lateinit var videos: GetAllVideos

    private val storageRequestCode = 2023

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityMainBinding
        var sortValue = 0
        var themeIndex = 0
        val currentTheme = arrayOf( R.style.Theme_VideoPlayer, R.style.Dark_mode )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeIndex = getSharedPreferences( "THEMES", MODE_PRIVATE ).getInt("themeIndex", 0 )
        setTheme( currentTheme[ themeIndex ] )
        binding = ActivityMainBinding.inflate( layoutInflater )
        setContentView(  binding.root  )

        if (  reqRuntimePerms() ) {
            initializeMALayout()
        }
    }

    private fun setFragment( fragment: Fragment ){
        currentFragment = fragment
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace( R.id.fragmentFL, fragment )
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
    
    private fun initializeMALayout() {
        videos = GetAllVideos( this )
        if ( themeIndex == 1 ) setDarkModeUi()
        setFragment( VideosFragment() ) //starting fragment
        setUpNavDrawer()
        binding.bottomNav.setOnItemSelectedListener {
            if (  VideosFragment.dataChanged  ) VideosFragment.videosList = videos.getAllVideos()
            when (it.itemId) {
                R.id.all_videos -> setFragment(VideosFragment())
                R.id.all_folders -> setFragment(FoldersFragment())
            }
            return@setOnItemSelectedListener true
        }
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.feedback ->  Toast.makeText( this, "Feature not yet implemented!", Toast.LENGTH_LONG ).show()
                R.id.themes -> {
                    val listItems = arrayOf( "Light", "Dark")
                    val dialog = MaterialAlertDialogBuilder( this )
                        .setTitle( "Choose theme" )
                        .setSingleChoiceItems( listItems, themeIndex ){_, position ->
                            themeIndex = position
                        }
                        .setPositiveButton("OK"){_,_ ->
                            when( themeIndex ) {
                                0 -> saveTheme(0)
                                1 -> saveTheme(1)
                            }
                        }
                        .setNegativeButton("Cancel") {d,_ -> d.dismiss()}
                        .create()
                    dialog.show()
                    dialog.getButton( AlertDialog.BUTTON_POSITIVE ).setTextColor( Color.BLACK )
                }
                R.id.sort_order -> {
                    val listItems = arrayOf( "Latest", "Oldest", "Name(A to Z)", "Name(Z to A)", "Size(Smallest first)", "Size(Largest first)" )
                    val dialog = MaterialAlertDialogBuilder( this )
                        .setTitle( "Sort Videos By" )
                        .setSingleChoiceItems( listItems, sortValue ){_, position ->
                            sortValue = position
                        }
                        .setPositiveButton("OK"){_,_ ->
                            getSharedPreferences("SortOrder", MODE_PRIVATE ).edit()
                                .putInt("sortValue", sortValue )
                                .apply()
                            //restarting the app
                            finish()
                            startActivity( intent )
                        }
                        .create()
                    dialog.show()
                    dialog.getButton( AlertDialog.BUTTON_POSITIVE ).setTextColor( Color.BLACK )
                }
                R.id.about -> {
                    val dialog = MaterialAlertDialogBuilder( this )
                        .setTitle( "About App" )
                        .setMessage("Developed by Engineer Fred!\n\n@Busitema")
                        .setPositiveButton("OK"){dialog,_ ->
                            dialog.dismiss()
                        }
                        .create()
                    dialog.show()
                    dialog.getButton( AlertDialog.BUTTON_POSITIVE ).setTextColor( Color.BLACK )
                }
                R.id.exit -> {
                    val dialog = MaterialAlertDialogBuilder( this )
                        .setTitle( "Close App?" )
                        .setCancelable( false )
                        .setPositiveButton("OK"){ _, _ ->
                            exitProcess(1)
                        }
                        .setNegativeButton("No"){d,_ -> d.dismiss()}
                        .create()
                    dialog.show()
                    dialog.getButton(  AlertDialog.BUTTON_POSITIVE ).setBackgroundColor( ContextCompat.getColor( this, R.color.copperfield ) )
                    dialog.getButton( AlertDialog.BUTTON_POSITIVE ).setTextColor( Color.WHITE )
                    dialog.getButton(  AlertDialog.BUTTON_NEGATIVE).setBackgroundColor( ContextCompat.getColor( this, R.color.copperfield ) )
                    dialog.getButton( AlertDialog.BUTTON_NEGATIVE ).setTextColor( Color.WHITE )
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun saveTheme(index: Int) {
        getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            .putInt("themeIndex", index)
            .apply()
        //restarting the app
        finish()
        startActivity( intent )
    }

    private fun setUpNavDrawer() {
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if ( toggle.onOptionsItemSelected( item ) ) return true
        return super.onOptionsItemSelected(item)
    }

    //check run time permission
    private fun reqRuntimePerms() : Boolean {
        if (  Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU ) {
            if (ActivityCompat.checkSelfPermission(this , READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this , arrayOf(READ_MEDIA_VIDEO) , storageRequestCode)
                return false
            } else {
                return true
            }
        } else {
            if ( ActivityCompat.checkSelfPermission(this , WRITE_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this , arrayOf(WRITE_EXTERNAL_STORAGE ) , storageRequestCode)
                return false
            } else {
                return true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.R ) {
            if ( ActivityCompat.checkSelfPermission( this, WRITE_EXTERNAL_STORAGE ) == PackageManager.PERMISSION_GRANTED ) {
                initializeMALayout()
            } else {
                Toast.makeText( this, "Permission was not granted!!", Toast.LENGTH_LONG ).show()
            }
        } else {
            if ( Environment.isExternalStorageManager() ) initializeMALayout() else Toast.makeText( this, "Permission was not granted!!", Toast.LENGTH_LONG ).show()
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when( requestCode ) {
            storageRequestCode -> {
                for ( i in 0 until permissions.size ) {
                    val permission = permissions[i]
                    if ( grantResults[ i ] == PackageManager.PERMISSION_DENIED ) {
                        val showRationale = shouldShowRequestPermissionRationale( permission )
                        if ( !showRationale ) {
                            //user clicked on neverAskAgain
                            val alertDialog = AlertDialog.Builder( this )
                                .setTitle( "App Permission!" )
                                .setMessage("For playing videos, you must allow this app to access video files on this device!\n\n" +
                                        "Simply follow the steps below to get started!\n\n" +
                                        "1. Open settings by clicking on the button below.\n" +
                                        "2. Click On the permissions tab.\n" +
                                        "3. Click Allow Access for storage.")
                                .setPositiveButton("Open Settings") {_,_ ->
                                    val intent = Intent( Settings.ACTION_APPLICATION_DETAILS_SETTINGS )
                                    val uri = Uri.fromParts("package", packageName, null )
                                    intent.setData( uri )
                                    startActivityForResult( intent, 2000 )
                                }.create()
                            alertDialog.show()
                            alertDialog.getButton( AlertDialog.BUTTON_POSITIVE ).setTextColor( ContextCompat.getColor( this, R.color.black ) )
                        } else {
                            //user clicked on denyButton
                            ActivityCompat.requestPermissions( this, arrayOf( WRITE_EXTERNAL_STORAGE ), storageRequestCode )
                        }
                    } else {
                        //user clicked on accept
                        Toast.makeText( this, "Permission Granted!", Toast.LENGTH_LONG ).show()
                        initializeMALayout()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode : Int , resultCode : Int , data : Intent?) {
        super.onActivityResult(requestCode , resultCode , data)
        (currentFragment as VideosFragment).videosAdapter.onResult( requestCode, resultCode )
        if ( requestCode == 321 ) {
            if ( resultCode == RESULT_OK ) {
                initializeMALayout()
            }
        }
    }

    private fun setDarkModeUi() {
        binding.bottomNav.itemTextColor = ColorStateList( arrayOf(intArrayOf(  )), intArrayOf( Color.WHITE ))
        binding.bottomNav.itemIconTintList = ColorStateList( arrayOf(intArrayOf()), intArrayOf( Color.WHITE ))
        binding.navView.itemTextColor = ColorStateList( arrayOf(intArrayOf()), intArrayOf( Color.WHITE ))
        binding.navView.itemIconTintList = ColorStateList( arrayOf(intArrayOf()), intArrayOf( Color.WHITE ))
}}