package com.engineer.fred.videoplayer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.engineer.fred.videoplayer.adapters.VideosAdapter
import com.engineer.fred.videoplayer.data.Video
import com.engineer.fred.videoplayer.databinding.FragmentVideosBinding


class VideosFragment : Fragment() {

    private lateinit var binding: FragmentVideosBinding
    lateinit var videosAdapter: VideosAdapter
    private lateinit var videos : GetAllVideos

    companion object {
        var videosList = ArrayList<Video>()
        var searchList = ArrayList<Video>()
        var isSearching = false
        var dataChanged = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu( true )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireContext().theme.applyStyle (MainActivity.currentTheme[  MainActivity.themeIndex ] , true )
        binding = FragmentVideosBinding.inflate( inflater,container, false )
        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseFVUi()
    }

    @SuppressLint("SetTextI18n")
    private fun initialiseFVUi() {
        videos = GetAllVideos( requireContext() )

        videosList = videos.getAllVideos()

        if ( MainActivity.themeIndex == 1 ) setDarkModeUi()
        videosAdapter = VideosAdapter(requireActivity(), videosList )
        binding.tvTotalVideosNumberCount.text = "(${videosAdapter.itemCount})"
        setUpVideosRecyclerView()

        binding.videoRefresh.setOnRefreshListener{
            videosList = videos.getAllVideos()
            videosAdapter.updateVideoList( videosList )
            binding.tvTotalVideosNumberCount.text = "(${videosAdapter.itemCount})"
            binding.videoRefresh.isRefreshing = false
        }
    }

    private fun setUpVideosRecyclerView() {
        binding.videosRv.apply {
            adapter = videosAdapter
            layoutManager = LinearLayoutManager( requireActivity() )
            setHasFixedSize( true )
            setItemViewCacheSize( 13 )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate( R.menu.menu, menu )
            if ( MainActivity.themeIndex == 1 ) menu.findItem( R.id.searchView ).icon = AppCompatResources.getDrawable( requireContext(), R.drawable.ic_search_dark )
            val searchView = menu.findItem( R.id.searchView )?.actionView as? SearchView
            searchView?.setOnQueryTextListener(  object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true
                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let {
                        searchList = ArrayList()
                        for (  video in videosList ) {
                            if ( video.title.lowercase().contains( newText.lowercase() ) ) searchList.add(video)
                        }
                        isSearching = true
                        videosAdapter.updateVideoList( searchList )
                    }
                    return true
                }
            })
        super.onCreateOptionsMenu(menu, menuInflater)
    }

    override fun onResume() {
        super.onResume()
        if ( dataChanged ) videosAdapter.notifyDataSetChanged()
        dataChanged = false
    }

    private fun setDarkModeUi() {
        binding.tvTotalVideos.setTextColor(  ContextCompat.getColor( requireContext(),   R.color.white ) )
        binding.tvTotalVideosNumberCount.setTextColor(  ContextCompat.getColor( requireContext(),   R.color.white ) )
        binding.root.setBackgroundColor( ContextCompat.getColor( requireContext(),   R.color.dark_bg ) )
    }

}