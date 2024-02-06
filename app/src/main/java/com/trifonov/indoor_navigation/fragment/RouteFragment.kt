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
import com.trifonov.indoor_navigation.map.Map
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.finishNode
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector
import com.trifonov.indoor_navigation.map.MapConstants.startNode

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
    private var btnHeight = 0
    private val filterList = mutableListOf<String>()
    private var resultList = mutableListOf<Map.Dot>()
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
        else {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            pointA.requestFocus()
        }
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        btnContainer.translationY = 0f
        btnContainer.startAnimation(slideUpAnimation)
        val viewCollapsed = fragment.findViewById<LinearLayout>(R.id.view_collapsed)
        viewCollapsed.post {
            btnHeight = btnContainer.height
            peekHeight = viewCollapsed.height + btnContainer.height
            mBottomSheetBehavior.peekHeight = peekHeight
            val view = View(activity)
            view.layoutParams = LinearLayout.LayoutParams(0, btnContainer.height + 40)
            resultSearchLinearLayout.addView(view)
        }
        resultList = dotList.filter { (it.getType() in filterList || filterList.isEmpty()) && it.getName().isNotEmpty()  } as MutableList<Map.Dot>
        val dot = dotList.find { it.getId() == startNode }!!.copy()
        dot.setName("Моё местоположение")
        resultList.add(0, dot)
        println(dotList)
        adapterResultDot = AudienceRouteAdapter(resultList) { dot ->
            pointA.isActivated
            if (pointA.isFocused){
                pointA.setText(dot.getName())
                pointA.setSelection(dot.getName().length)
            }
            else{
                if (pointB.isFocused) {
                    pointB.setText(dot.getName())
                    pointB.setSelection(dot.getName().length)
                }
            }
        }
        audienceRV.adapter = adapterResultDot
    }

    /**
     * Настраивает работу нижнего листа
     * @Param [view] view фрагмент
     */
    private fun initBottomSheet(view: View){
        mBottomSheetBehavior.skipCollapsed = false
        val dotStart = dotList.find { it.getId() == startNode }
        val endDot = dotList.find { it.getId() == finishNode }
        pointA.setText(dotStart?.getName() ?: "")
        pointB.setText(endDot?.getName() ?: "")
        val openBottomSheet = {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        pointA.clearFocus()
        pointB.clearFocus()
        pointA.setOnFocusChangeListener { _, _ -> openBottomSheet()}
        pointB.setOnFocusChangeListener { _, _ -> openBottomSheet()}
        view.findViewById<CardView>(R.id.build_route).setOnClickListener {
            try{
                var start = resultList.find { it.getName() == pointA.text.toString() }
                var end = resultList.find { it.getName() == pointB.text.toString() }
                // TODO: ИСПРАВИТЬ КРИНЖОВУЮ ЛОГИКУ ПРИ ДОБАВЛЕНИИ ОПЕРЕДЕЛЕНИЯ МЕСТОПОЛОЖЕНИЯ
                if (start!!.getName() == "Моё местоположение") start = dotList.find { it.getId() == startNode }
                if (end!!.getName() == "Моё местоположение") end = dotList.find { it.getId() == finishNode }
                mapConnector.updatePath(start = start!!.getId(), finish = end!!.getId())
                mapConnector.moveCameraToDot(start)
                mBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {}
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        btnContainer.translationY = btnHeight * (1 - slideOffset)
                    }
                })
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
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
            resultList = dotList.filter { (it.getType() in filterList || filterList.isEmpty()) && it.getName().isNotEmpty()  } as MutableList<Map.Dot>
            val dot = dotList.find { it.getId() == startNode }!!.copy()
            dot.setName("Моё местоположение")
            resultList.add(0, dot)
            adapterResultDot.updateList( resultList )
        }
    }
}