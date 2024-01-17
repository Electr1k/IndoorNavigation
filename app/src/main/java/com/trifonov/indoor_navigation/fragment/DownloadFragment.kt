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
            val locationName = "Korpus_G"
            fileHelper = FileHelper(requireActivity(), view, locationName)
            val json = fileHelper.getJsonMap(locationName)
            if (json != "empty location") {
                loadFromString(json)
                activity?.runOnUiThread {
                    view.findNavController().navigate(R.id.action_download_to_head)
                }
            }
        }.start()
    }
    private fun loadFromString(json: String) {
        zoomLevelCount = fileHelper.getLevelCount("tiles1") - 1
        navigation.loadMapFromJson(json)
    }
}