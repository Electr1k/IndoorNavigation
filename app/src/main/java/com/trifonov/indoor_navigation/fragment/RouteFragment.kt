package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceRouteAdapter
import com.trifonov.indoor_navigation.adapter.AudienceTypeAdapter
import com.trifonov.indoor_navigation.databinding.RouteFragmentBinding
import com.trifonov.indoor_navigation.mapView.Dot
import com.trifonov.indoor_navigation.mapView.RouteService
import kotlin.math.abs

class RouteFragment: CustomFragment() {
    private lateinit var routeService: RouteService

    private var _binding: RouteFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var typesRV: RecyclerView
    private lateinit var audienceRV: RecyclerView
    private lateinit var swapImage: ImageView
    private lateinit var resultNestedScroll: NestedScrollView
    private lateinit var pointA: EditText
    private lateinit var pointB: EditText
    private lateinit var btnContainer: LinearLayout
    private lateinit var textRouteBuild: TextView
    private lateinit var resultSearchLinearLayout: LinearLayout
    private lateinit var clearPointA: ImageView
    private lateinit var clearPointB: ImageView
    private lateinit var buildRouteBtn: CardView
    private lateinit var fragment: View
    private var peekHeight = 0
    private var btnHeight = 0
    private val myPositionName = "Моё местоположение"
    private val filterList = mutableListOf<String>()
    private var resultList = mutableListOf<Dot>()
    private var currentState = BottomSheetBehavior.STATE_EXPANDED
    private lateinit var adapterResultDot: AudienceRouteAdapter

    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        _binding = RouteFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        routeService = RouteService.getInstance(baseActivity.mapView)
        fragment = view
        typesRV = binding.audienceTypes
        audienceRV = binding.resultSearch
        resultSearchLinearLayout = binding.resultSearchLL
        swapImage = binding.swapImage
        pointA = binding.routeFrom
        pointB = binding.routeTo
        btnContainer = binding.btnContainer
        textRouteBuild = binding.textBuild
        clearPointA = binding.clearPointA
        clearPointB = binding.clearPointB
        resultNestedScroll = binding.resultNestedScroll
        buildRouteBtn = binding.buildRoute
        mBottomSheet.visibility = View.VISIBLE
        if (arguments?.getBoolean("isFromPoint") == true){
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            textRouteBuild.text = "Пройти"
            currentState = BottomSheetBehavior.STATE_COLLAPSED
        }
        else {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            currentState = BottomSheetBehavior.STATE_EXPANDED
            textRouteBuild.text = "Показать маршрут"
            pointA.requestFocus()
        }
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        btnContainer.translationY = 0f
        btnContainer.startAnimation(slideUpAnimation)
        initBottomSheet(view)
    }

    override fun onStart() {
        super.onStart()
        val viewCollapsed = fragment.findViewById<LinearLayout>(R.id.view_collapsed)
        viewCollapsed.post {
            btnHeight = btnContainer.height
            peekHeight = viewCollapsed.height + btnContainer.height
            val view = View(activity)
            mBottomSheetBehavior.peekHeight = peekHeight + 5
            view.layoutParams = LinearLayout.LayoutParams(0, btnContainer.height + 40)
            resultSearchLinearLayout.addView(view)
        }
        resultList = baseActivity.mapData.dotList.filter { (it.getType() in filterList || filterList.isEmpty()) && it.getName().isNotEmpty()  } as MutableList<Dot>
        val dot = baseActivity.mapData.dotList.find { it.getId() == baseActivity.mapView.getMyPosition() }!!.copy()
        dot.setName(myPositionName)
        dot.setId(baseActivity.mapView.getMyPosition())
        resultList.add(0, dot)
        adapterResultDot = AudienceRouteAdapter(resultList) { dot ->
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(fragment.windowToken, 0)
            if (pointA.isFocused){
                (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
                pointA.setText(dot.getName())
                pointB.requestFocus()
                pointB.setSelection(pointB.text.toString().length)
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
        val openBottomSheet = {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        pointB.setOnFocusChangeListener { _, focus -> if (focus){ openBottomSheet(); adapterResultDot.updateList(resultList)} }
        pointA.setOnFocusChangeListener { _, focus -> if (focus){ openBottomSheet(); adapterResultDot.updateList(resultList)} }
        pointA.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                adapterResultDot.updateList(resultList.filter { pointA.text.toString().trim().lowercase() in it.getName().trim().lowercase() })
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                if (!pointB.hasFocus()){ adapterResultDot.updateList(resultList) }
            }
        }
        pointB.onFocusChangeListener = View.OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                adapterResultDot.updateList(resultList.filter { pointB.text.toString().trim().lowercase() in it.getName().trim().lowercase() })
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                if (!pointA.hasFocus()){ adapterResultDot.updateList(resultList) }
            }
        }
    }

    private fun getDotById(list: List<Dot>, id: Int): Dot?{
        return list.find { id == it.getId() }
    }

    private fun findDotByName(startName: String, endName: String): List<Dot?>{
        val start = if (startName.equals(myPositionName, ignoreCase = true)) resultList.find { it.getId() == baseActivity.mapView.getMyPosition() }
        else resultList.find { it.getName().equals(startName, ignoreCase = true) }
        val end =
            if (endName.equals(myPositionName, ignoreCase = true)) resultList.find { it.getId() == baseActivity.mapView.getMyPosition() }
            else resultList.find { it.getName()
                .equals(endName, ignoreCase = true) }
        return listOf(start, end)
    }

    /**
     * Настраивает работу нижнего листа
     * @Param [view] view фрагмент
     */
    private fun initBottomSheet(view: View){
        mBottomSheetBehavior.skipCollapsed = false
        if (arguments?.getBoolean("isFromPoint", false) == true || routeService.startDot != null && routeService.endDot != null) {
            val dotStart = getDotById(baseActivity.mapData.dotList, if (routeService.startDot != null && routeService.currentRouteIsMain) routeService.startDot!! else baseActivity.mapView.getStartPosition())
            val endDot = getDotById(baseActivity.mapData.dotList, if (routeService.endDot != null && routeService.currentRouteIsMain) routeService.endDot!! else baseActivity.mapView.getFinishPosition())
            pointA.setText(dotStart?.getName() ?: "")
            if (dotStart?.getId() == baseActivity.mapView.getMyPosition()){
                pointA.setText(myPositionName)
            }
            pointB.setText(endDot?.getName() ?: "")
            if (endDot?.getId() == baseActivity.mapView.getMyPosition()){
                pointB.setText(myPositionName)
            }
        }

        clearPointA.setOnClickListener { pointA.setText(""); pointA.requestFocus() }
        clearPointB.setOnClickListener { pointB.setText(""); pointB.requestFocus() }
        pointA.clearFocus()
        pointB.clearFocus()
        pointA.addTextChangedListener{
            (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
            adapterResultDot.updateList(resultList.filter { pointA.text.toString().trim().lowercase() in it.getName().trim().lowercase() })
        }
        pointB.addTextChangedListener{
            (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
            adapterResultDot.updateList(resultList.filter { pointB.text.toString().trim().lowercase() in it.getName().trim().lowercase() })
        }

        buildRouteBtn.setOnClickListener {
            try{

                val result = findDotByName(pointA.text.toString(), pointB.text.toString())
                val start = result[0]
                val end = result[1]
                if (start == null || end == null){
                    if (start == null) (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.red)
                    if (end == null) (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.red)
                    return@setOnClickListener
                }

                if (currentState == BottomSheetBehavior.STATE_COLLAPSED) {
                    routeService.saveTempRouteAsMain()
                    routeService.buildMainRoute()
                    baseActivity.mapView.moveCameraToDot(resultList.find { routeService.startDot == it.getId() }!!)
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                else{
//                        /** Ищем оригинальное название "Моего местоположение, чтобы подставить название аудитории*/
//                        if (start.getId() == myPosition) start = getDotById(dotList, myPosition);
//                        if (end.getId() == myPosition) end = getDotById(dotList, myPosition);
                    pointA.setText(start.getName())
                    pointB.setText(end.getName())
                    pointA.setSelection(start.getName().length)
                    pointB.setSelection(end.getName().length)

                    routeService.buildTempRoute(start.getId(), end.getId())

                    baseActivity.mapView.moveCameraToDot(start)
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    (pointA.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
                    (pointB.parent as MaterialCardView).strokeColor = getColor(requireContext(), R.color.light_gray)
                }
            }
            catch(e: Exception){
                println(e.message)
            }
        }
        mBottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (mBottomSheetBehavior.state in listOf(5,4,3)) {
                    currentState = newState
                }
                if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                    pointA.clearFocus()
                    pointB.clearFocus()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                textRouteBuild.alpha = abs(2*slideOffset-1)
                textRouteBuild.alpha = abs(2*slideOffset-1)
                if (slideOffset > 0.5f){
                    textRouteBuild.text = "Показать маршрут"
                }
                else{
                    textRouteBuild.text = "Пройти"
                    if (slideOffset <= 0){
                        btnContainer.translationY = btnHeight * abs(slideOffset)
                    }
                }
            }
        })
        typesRV.setHasFixedSize(true)
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        resultNestedScroll.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            inputMethodManager.hideSoftInputFromWindow(fragment.windowToken, 0)
        }

        audienceRV.setHasFixedSize(true)
        swapImage.setOnClickListener{
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED){
                if(routeService.currentRouteIsMain){
                    routeService.buildTempRoute(routeService.endDot!!, routeService.startDot!!)
                }
                else routeService.buildTempRoute(routeService.endDotTemp!!, routeService.startDotTemp!!)
            }
            pointA.text = pointB.text.also { pointB.text = pointA.text } // Swap
            if (pointA.isFocused){
                pointA.setSelection(pointA.text.toString().length)
                adapterResultDot.updateList(resultList.filter { pointA.text.toString().trim().lowercase() in it.getName().trim().lowercase() })
            }
            if (pointB.isFocused){
                pointB.setSelection(pointB.text.toString().length)
                adapterResultDot.updateList(resultList.filter { pointB.text.toString().trim().lowercase() in it.getName().trim().lowercase() })
            }

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
            resultList = baseActivity.mapData.dotList.filter { (it.getType() in filterList || filterList.isEmpty()) && it.getName().isNotEmpty()  } as MutableList<Dot>
            val dot = getDotById(baseActivity.mapData.dotList, baseActivity.mapView.getMyPosition())!!.copy()
            dot.setName(myPositionName)
            resultList.add(0, dot)
            adapterResultDot.updateList( resultList )
        }
    }

    override fun onDestroy() {
        if (!routeService.currentRouteIsMain){
            if (null !in listOf(routeService.startDot, routeService.endDot)) routeService.buildMainRoute()
            else{
                baseActivity.mapView.removePath(true)
            }
            routeService.deleteTempDots()
        }
        super.onDestroy()
        _binding = null
    }


}