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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceRouteAdapter
import com.trifonov.indoor_navigation.adapter.AudienceTypeAdapter
import com.trifonov.indoor_navigation.map.MapConstants
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector

class RouteFragment: CustomFragment() {
    private lateinit var typesRV: RecyclerView
    private lateinit var audienceRV: RecyclerView
    private lateinit var swapImage: ImageView
    private lateinit var pointA: EditText
    private lateinit var pointB: EditText
    private lateinit var btnContainer: LinearLayout
    private lateinit var resultSearchLinearLayout: LinearLayout
    private lateinit var fragment: View
    private var peekHeight = 0
    private val filterList = mutableListOf(
        "Туалет",
        "Аудитория",
        "Лекционный зал",
        "Кафе",
        "Зона отдыха"
    )
    private lateinit var adapterResultDot: AudienceRouteAdapter

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
        resultSearchLinearLayout = view.findViewById(R.id.result_search_LL)
        swapImage = view.findViewById(R.id.swap_image)
        pointA = view.findViewById(R.id.route_from)
        pointB = view.findViewById(R.id.route_to)
        btnContainer = view.findViewById(R.id.btnContainer)
        initBottomSheet(view)
    }

    override fun onStart() {
        super.onStart()
        mBottomSheet.visibility = View.VISIBLE
        if (arguments?.getBoolean("isFromPoint") == true) mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        btnContainer.translationY = 0f
        btnContainer.startAnimation(slideUpAnimation)
        val viewCollapsed = fragment.findViewById<LinearLayout>(R.id.view_collapsed)
        viewCollapsed.post {
            println("Height btn ${btnContainer.height}")
            peekHeight = viewCollapsed.height + btnContainer.height
            mBottomSheetBehavior.peekHeight = peekHeight
            val view = View(activity)
            view.layoutParams = LinearLayout.LayoutParams(0, btnContainer.height + 40)
            resultSearchLinearLayout.addView(view)
        }
        val resultList = dotList.filter { it.getType() in filterList && it.getName().isNotEmpty() }
        adapterResultDot = AudienceRouteAdapter(resultList) { dot ->
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapConnector.moveCameraToDot(dot)
            val bundle = Bundle()
            bundle.putInt("id", dot.getId())
            findNavController().navigate(R.id.action_route_to_scan, bundle)
        }
        audienceRV.adapter = adapterResultDot
    }

    /**
     * Настраивает работу нижнего листа
     * @Param [view] view фрагмент
     */
    private fun initBottomSheet(view: View){
        mBottomSheetBehavior.skipCollapsed = false
        val dotStart = MapConstants.dotList.find { it.getId() == MapConstants.startNode }
        val endDot = MapConstants.dotList.find { it.getId() == MapConstants.finishNode }
//        val isFromPoint = arguments?.getBoolean("isFromPoint") ?: false
//        val isToPoint = arguments?.getBoolean("isToPoint") ?: false
//        if (isFromPoint) {
//            pointA.setText(arguments?.getString("nameFrom"))
//            pointB.setText("Моё местоположение")
//        }
//        if (isToPoint){
//            pointA.setText("Моё местоположение")
//            pointB.setText(arguments?.getString("nameTo"))
//        }
        pointA.setText(dotStart?.getName() ?: "")
        pointB.setText(endDot?.getName() ?: "")
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
        val typeList = listOf(
            "Туалет",
            "Аудитория",
            "Лекционный зал",
            "Кафе",
            "Зона отдыха"
        )

        typesRV.adapter = AudienceTypeAdapter(typeList) { type, view ->
            if (type in filterList) {
                filterList.remove(type)
                view.setCardBackgroundColor(resources.getColor(R.color.light_blue))
            } else {
                filterList.add(type)
                view.setCardBackgroundColor(resources.getColor(R.color.lighting_blue))
            }
            adapterResultDot.updateList( dotList.filter { it.getType() in filterList && it.getName().isNotEmpty() } )
        }
    }
}