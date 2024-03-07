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

           (requireActivity() as MainActivity).mapView.setMyPosition(link?.let { Integer.parseInt(it) }!!)
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