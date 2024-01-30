package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceRouteAdapter
import com.trifonov.indoor_navigation.adapter.AudienceTypeAdapter
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector

class RouteFragment: CustomFragment() {
    private lateinit var typesRV: RecyclerView
    private lateinit var audienceRV: RecyclerView
    private lateinit var swapImage: ImageView
    private lateinit var pointA: EditText
    private lateinit var pointB: EditText
    private lateinit var fragment: View

    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.route_fragment, container, false)
        return view
    }

    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragment = view
        typesRV = view.findViewById(R.id.audience_types)
        audienceRV = view.findViewById(R.id.result_search)
        swapImage = view.findViewById(R.id.swap_image)
        pointA = view.findViewById(R.id.route_from)
        pointB = view.findViewById(R.id.route_to)
        initBottomSheet(view)
    }

    override fun onStart() {
        super.onStart()
        mBottomSheet.visibility = View.VISIBLE
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        val viewCollapsed = fragment.findViewById<LinearLayout>(R.id.view_collapsed)
        viewCollapsed.post {
            mBottomSheetBehavior.peekHeight = viewCollapsed.height
        }
    }

    /**
     * Настраивает работу нижнего листа
     * @Param [view] view фрагмент
     */
    private fun initBottomSheet(view: View){
        mBottomSheetBehavior.skipCollapsed = false
        val isFromPoint = arguments?.getBoolean("isFromPoint") ?: false
        val isToPoint = arguments?.getBoolean("isToPoint") ?: false
        if (isFromPoint) {
            pointA.setText(arguments?.getString("nameFrom"))
            pointB.setText("Моё местоположение")
        }
        if (isToPoint){
            pointA.setText("Моё местоположение")
            pointB.setText(arguments?.getString("nameTo"))
        }
        view.findViewById<CardView>(R.id.build_route).setOnClickListener {
            try{
                val start = pointA.text.toString().toInt()
                val end = pointB.text.toString().toInt()
                mapConnector.updatePath(start = start, finish = end)
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            catch(e: Exception){
                println(e.message)
            }
        }
        typesRV.setHasFixedSize(true)
        audienceRV.setHasFixedSize(true)
        swapImage.setOnClickListener{
            pointA.text = pointB.text.also { pointB.text = pointA.text } // Swap
        }
        val list = listOf(
            "Туалет",
            "Аудитория",
            "Лекционный зал",
            "Кафе",
            "Зона отдыха"
        )
        val listResult = listOf("Офис", "Туалет", "Офис","Офис", "Туалет","Другое", "Офис", "Туалет","Другое", "Офис")
        audienceRV.adapter = AudienceRouteAdapter(listResult)
        typesRV.adapter = AudienceTypeAdapter(list)
    }
}