package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.QR_Scanner
import com.trifonov.indoor_navigation.QR_Scanner.Companion.link
import com.trifonov.indoor_navigation.mapView.Dot
import com.trifonov.indoor_navigation.mapView.RouteService


class ScanFragment: Fragment() {

    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.scan_fragment, container, false)
        val intent = Intent(context, QR_Scanner::class.java)
        startActivityForResult(intent, CODE)
        return view
    }

    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    val CODE = 3
    var root: View? = null
    var sPref: SharedPreferences? = null


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 3) {
            val link = data!!.getStringExtra("link")

            val baseActivity = requireActivity() as MainActivity
            val point: Dot = baseActivity.mapData.dotList.find { Integer.parseInt(link) == it.getId() }!!
            val myPosition = point.copy().also { it.setId(point.getId()); it.setName("Моё местоположение"); it.setLevel(point.getLevel()) }
            baseActivity.mapView.moveMyPosition(myPosition)
            val routeService = RouteService.getInstance(mapView = baseActivity.mapView)
            try {
                routeService.startDot = myPosition.getId()
                routeService.buildMainRoute()
            }catch (_: Exception){}
            if (myPosition.getLevel() != baseActivity.levelNumber.toInt()){
                baseActivity.levelNumber = myPosition.getLevel().toString()
                baseActivity.configureMap()
            }
            baseActivity.mapView.moveCameraToDot(myPosition)

            Toast.makeText(requireContext(), "Местоположение определено", Toast.LENGTH_SHORT).show();
        }
        findNavController().popBackStack()
    }

    fun saveText(link: Int) {
        sPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val ed = sPref?.edit()
        ed?.putInt("MY_POS", link)
        ed?.apply()
    }
}