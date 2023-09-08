package com.engineer.fred.videoplayer.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.engineer.fred.videoplayer.FoldersActivity
import com.engineer.fred.videoplayer.MainActivity
import com.engineer.fred.videoplayer.R
import com.engineer.fred.videoplayer.data.Folder
import com.engineer.fred.videoplayer.databinding.FolderViewBinding

class FoldersAdapter(  private val  context: Context, private var foldersList: ArrayList<Folder>) : Adapter<FoldersAdapter.FolderViewHolder>() {

    inner class FolderViewHolder(private val binding: FolderViewBinding) :
        ViewHolder(binding.root) {
        fun bind(currentFolder: Folder, position: Int) {
            binding.tvFolderName.text = currentFolder.name
            binding.root.setOnClickListener {
                val i = Intent(context, FoldersActivity::class.java )
                i.putExtra("folderPos", position )
                ContextCompat.startActivity( context, i, null )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = FolderViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if ( MainActivity.themeIndex == 1 ) {
            binding.tvFolderName.setTextColor( ContextCompat.getColor( context, R.color.white  ) )
            binding.videoImage.imageTintList = ColorStateList( arrayOf(intArrayOf()), intArrayOf( Color.WHITE ))
            binding.root.background = AppCompatResources.getDrawable( context, R.drawable.dark_ripple_effect )
        }
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val currentFolder = foldersList[position]
        holder.bind(currentFolder, position )
    }

    override fun getItemCount(): Int {
        return foldersList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFoldersList( newList: ArrayList<Folder>) {
        foldersList = ArrayList()
        foldersList = newList
        notifyDataSetChanged()
    }
}

