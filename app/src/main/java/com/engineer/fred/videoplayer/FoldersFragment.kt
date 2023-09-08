package com.engineer.fred.videoplayer

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.engineer.fred.videoplayer.adapters.FoldersAdapter
import com.engineer.fred.videoplayer.adapters.VideosAdapter
import com.engineer.fred.videoplayer.data.Folder
import com.engineer.fred.videoplayer.databinding.FragmentFoldersBinding
import com.engineer.fred.videoplayer.databinding.FragmentVideosBinding


class FoldersFragment : Fragment() {

    private lateinit var binding: FragmentFoldersBinding
    private lateinit var foldersAdapter: FoldersAdapter

    companion object {
        var foldersList = ArrayList<Folder>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext().theme.applyStyle (MainActivity.currentTheme[  MainActivity.themeIndex ] , true )
        binding = FragmentFoldersBinding.inflate( inflater,container, false )
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialiseFFUi()
    }

    @SuppressLint("SetTextI18n")
    private fun initialiseFFUi() {
        if (  MainActivity.themeIndex == 1 ) setDarkModeUi()
        foldersAdapter = FoldersAdapter(requireActivity(), foldersList )
        binding.tvTotalFoldersNumberCount.text = "(${foldersAdapter.itemCount})"
        setUpVideosRecyclerView()
    }

    private fun setUpVideosRecyclerView() {
        binding.foldersRv.apply {
            adapter = foldersAdapter
            layoutManager = LinearLayoutManager( requireActivity() )
            setHasFixedSize( true )
            setItemViewCacheSize( 13 )
        }
    }

    private fun setDarkModeUi() {
        binding.tvTotalFolders.setTextColor(  ContextCompat.getColor( requireContext(),   R.color.white ) )
        binding.tvTotalFoldersNumberCount.setTextColor(  ContextCompat.getColor( requireContext(),   R.color.white ) )
        binding.root.setBackgroundColor( ContextCompat.getColor( requireContext(),   R.color.dark_bg ) )
    }

}