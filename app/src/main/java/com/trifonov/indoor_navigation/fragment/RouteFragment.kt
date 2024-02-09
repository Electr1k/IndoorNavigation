package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.media.Image
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceRouteAdapter
import com.trifonov.indoor_navigation.adapter.AudienceTypeAdapter
import com.trifonov.indoor_navigation.map.Map
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.finishNode
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector
import com.trifonov.indoor_navigation.map.MapConstants.saveRoute
import com.trifonov.indoor_navigation.map.MapConstants.startNode
import kotlin.math.abs

class RouteFragment: CustomFragment() {
    private lateinit var typesRV: RecyclerView
    private lateinit var audienceRV: RecyclerView
    private lateinit var swapImage: ImageView
    private lateinit var pointA: EditText
    private lateinit var pointB: EditText
    private lateinit var btnContainer: LinearLayout
    private lateinit var textRouteBuild: TextView
    private lateinit var resultSearchLinearLayout: LinearLayout
    private lateinit var clearPointA: ImageView
    private lateinit var clearPointB: ImageView
    private lateinit var fragment: View
    private var peekHeight = 0
    private var btnHeight = 0
    private val filterList = mutableListOf<String>()
    private var resultList = mutableListOf<Map.Dot>()
    private var currentState = BottomSheetBehavior.STATE_EXPANDED
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
        textRouteBuild = view.findViewById(R.id.textBuild)
        clearPointA = view.findViewById(R.id.clear_point_a)
        clearPointB = view.findViewById(R.id.clear_point_b)
        initBottomSheet(view)
    }

    override fun onStart() {
        super.onStart()
        mBottomSheet.visibility = View.VISIBLE
        if (arguments?.getBoolean("isFromPoint") == true){
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            textRouteBuild.text = "Пройти"
            currentState = BottomSheetBehavior.STATE_COLLAPSED
        }
        else {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            currentState = BottomSheetBehavior.STATE_EXPANDED
            textRouteBuild.text = "Построить маршрут"
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
            mBottomSheetBehavior.peekHeight = peekHeight + if (currentState==4) 5 else -5;
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
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(fragment.windowToken, 0)
            if (pointA.isFocused){
                (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
                pointA.setText(dot.getName())
                pointA.setSelection(dot.getName().length)
            }
            else{
                if (pointB.isFocused) {
                    (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
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
        if (saveRoute || arguments?.getBoolean("isFromPoint", false) == true) {
            val dotStart = dotList.find { it.getId() == startNode }
            val endDot = dotList.find { it.getId() == finishNode }
            pointA.setText(dotStart?.getName() ?: "")
            pointB.setText(endDot?.getName() ?: "")
        }
        val openBottomSheet = {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        clearPointA.setOnClickListener { pointA.setText("") }
        clearPointB.setOnClickListener { pointB.setText("") }
        pointA.clearFocus()
        pointB.clearFocus()
        pointA.addTextChangedListener{
            (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
            adapterResultDot.updateList(resultList.filter { pointA.text.toString() in it.getName() })
        }
        pointB.addTextChangedListener{
            (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
            adapterResultDot.updateList(resultList.filter { pointB.text.toString() in it.getName() })
        }

        pointA.setOnFocusChangeListener { _, focus -> openBottomSheet(); if (!focus) adapterResultDot.updateList(resultList) }
        pointB.setOnFocusChangeListener { _, focus -> openBottomSheet(); if (!focus) adapterResultDot.updateList(resultList) }
        view.findViewById<CardView>(R.id.build_route).setOnClickListener {
            try{
                if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
                    saveRoute = true
                    var start = resultList.find { it.getName() == pointA.text.toString() }
                    var end = resultList.find { it.getName() == pointB.text.toString() }
                    // TODO: ИСПРАВИТЬ КРИНЖОВУЮ ЛОГИКУ ПРИ ДОБАВЛЕНИИ ОПЕРЕДЕЛЕНИЯ МЕСТОПОЛОЖЕНИЯ
                    if (start!!.getName() == "Моё местоположение") start =
                        dotList.find { it.getId() == startNode }
                    if (end!!.getName() == "Моё местоположение") end =
                        dotList.find { it.getId() == finishNode }
                    mapConnector.updatePath(start = start!!.getId(), finish = end!!.getId())
                    mapConnector.moveCameraToDot(start)
                    mBottomSheetBehavior.addBottomSheetCallback(object :
                        BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {}
                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            btnContainer.translationY = btnHeight * (1 - slideOffset)
                        }
                    })
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                else{
                    val start = resultList.find { it.getName() == pointA.text.toString() }
                    val end = resultList.find { it.getName() == pointB.text.toString() }
                    if (start == null || end == null){
                        if (start == null) (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.red)
                        if (end == null) (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.red)
                    }
                    else{
                        (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
                        (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
                        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }
            catch(e: Exception){
                println(e.message)
            }
        }
        mBottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                println("state $newState")
                if (mBottomSheetBehavior.state in listOf(5,4,3)) {
                    currentState = newState
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                println("slide $slideOffset")
                textRouteBuild.alpha = abs(2*slideOffset-1)
                if (slideOffset > 0.5){
                    textRouteBuild.text = "Построить маршрут"
                }
                else{
                    textRouteBuild.text = "Пройти"
                }

            }

        })
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