package com.trifonov.indoor_navigation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.map.FileHelper
import com.trifonov.indoor_navigation.map.MapConnector
import com.trifonov.indoor_navigation.map.MapConstants
import com.trifonov.indoor_navigation.map.MapConstants.zoomLevelCount
import com.trifonov.indoor_navigation.map.Navigation

class DownloadFragment : Fragment() {
    private lateinit var fileHelper: FileHelper
    private var navigation: Navigation = Navigation()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.download_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Thread {
            val mapConnector = MapConnector(requireActivity(), view, "Korpus_G")
            MapConstants.startNode++
            activity?.runOnUiThread { mapConnector.updatePath(136) }
        }.start()
    }
}