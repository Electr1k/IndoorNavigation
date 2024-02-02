package com.trifonov.indoor_navigation.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_SETTLING
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.ImagePagerAdapter
import com.trifonov.indoor_navigation.map.Map
import com.trifonov.indoor_navigation.map.MapConstants.dotList
import com.trifonov.indoor_navigation.map.MapConstants.finishNode
import com.trifonov.indoor_navigation.map.MapConstants.mapConnector
import com.trifonov.indoor_navigation.map.MapConstants.startNode
import java.lang.Float.max


class SelectedPointFragment: CustomFragment() {
    private var currentState: Int = BottomSheetBehavior.STATE_COLLAPSED
    private val peekHeight = 420
    private lateinit var viewPager: ViewPager
    private lateinit var cardView: CardView
    private lateinit var linearIndicator: LinearLayout
    private lateinit var countImages: TextView
    private lateinit var linearMainContent: LinearLayout
    private lateinit var navigateMenu: LinearLayout
    private lateinit var title: TextView
    private lateinit var description: TextView
    private var heightNavigationMenu: Int = 0
    private var progress = 0f
    private lateinit var selectedPoint: Map.Dot
    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.selected_point_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = arguments?.getInt("id", -1)
        selectedPoint = dotList.find { id == it.getId() }!!

        countImages = view.findViewById(R.id.countImages)
        cardView = view.findViewById(R.id.card)
        viewPager = view.findViewById(R.id.imagesPager)
        linearIndicator = view.findViewById(R.id.linearIndicator)
        linearMainContent = view.findViewById(R.id.main_content)
        navigateMenu = view.findViewById(R.id.navigateMenu)
        title = view.findViewById(R.id.title)
        description = view.findViewById(R.id.description)
        initBottomSheet(view)

        initPager()
    }

    override fun onStart() {
        super.onStart()
        mBottomSheet.visibility = View.VISIBLE
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
    }

    /**
     * Настраивает работу нижнего листа
     * @Param [view] view фрагмент
     */
    private fun initBottomSheet(view: View){
        mBottomSheetBehavior.skipCollapsed = false
        mBottomSheetBehavior.peekHeight = peekHeight
        val density = requireContext().resources.displayMetrics.density
        title.text = selectedPoint.getType() + " " + selectedPoint.getName()
        description.text = selectedPoint.getDescription()
        view.findViewById<CardView>(R.id.route_to).setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            finishNode = selectedPoint.getId()
            val bundle = Bundle()
            bundle.putBoolean("isFromPoint", true)
            view.findNavController().navigate(R.id.action_scan_to_route, bundle)
            mapConnector.updatePath(finishNode)
        }
        view.findViewById<CardView>(R.id.route_from).setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            startNode = selectedPoint.getId()
            val bundle = Bundle()
            bundle.putBoolean("isFromPoint", true)
            view.findNavController().navigate(R.id.action_scan_to_route, bundle)
            mapConnector.updatePath(finish = finishNode, start = startNode)
        }
        mBottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState !in listOf(BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING) ) currentState = newState
                    if (newState == BottomSheetBehavior.STATE_EXPANDED){
                        cardView.radius = 0F
                    }
                    else{
                        cardView.radius = 20f * density
                    }
                    println("Current offset: $currentState $newState")
                }
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    view.findViewById<LinearLayout>(R.id.linearLayout).translationY = max(-1 * 260 * density * (1 - slideOffset), -1 * 260 * density)
                    println("Current slideOffset: $slideOffset ${mBottomSheetBehavior.state}")
                    if (slideOffset >= 0) navigateMenu.translationY = - heightNavigationMenu * (slideOffset - 1)
                }
            }

        )

        // Высчитываем высоту меню с кнопками выстроения маршрута и добавляем спейсер после текста,
        // чтобы не загараживать его
        navigateMenu.post {
            val spacer = View(requireContext())
            heightNavigationMenu = navigateMenu.height + 30
            spacer.layoutParams = ViewGroup.LayoutParams(0, heightNavigationMenu)
            linearMainContent.addView(spacer)
            navigateMenu.translationY = heightNavigationMenu * 1f
        }
    }


    /**
     * Настраивает работу ViewPager и индикатора
     */
    private fun initPager(){

        /**
         * Для создания циклического ViewPager пришлось придумать неочевидную структру
         * Размер списка, который подается в адаптер, на 2 элемента больше исходного:
         * В голову списка копируем последний элемент, а в хвост первый
         * Указатель пейджера устанавливаем на 1 и когда пользователь перелистнет пейджер на 0 элемент
         * мы без анимации передвигаем указатель на N элемент списка (N - количество исходных фото или же n-2, где n - уже изменный список),
         * а если пользователь перелистнет последнее фото исходного списка, он попадет на первое фото(которое копировали) и в этот момент мы
         * сдвигаем указатель на 1 индекс
         * Таким образом мы "подменяем" картинки и появляется возможность смотреть пейджер по кругу
         * Но остается проблема с индикатором, так как количество реальных фото на 2 меньше размера списка в адаптере пришлось местами изголяться
         * https://medium.com/@ali.muzaffar/looping-infinite-viewpager-with-page-indicator-in-android-ce741f25702a
         * */

        /**
         * Исходный список
         */
        val imageList = mutableListOf(
            R.drawable.pager_1,
            R.drawable.pager_2,
            R.drawable.pager_3,
            R.drawable.pager_4,
            R.drawable.pager_5,
            R.drawable.pager_6,
            R.drawable.pager_7,
            R.drawable.pager_8,
            R.drawable.pager_9,
            R.drawable.pager_10,
        )

        countImages.text = imageList.size.toString()

        // Копируем первый и последний элемент в хвост и голову соответственно
        imageList.add(0, imageList.takeLast(1)[0])
        imageList.add(imageList.size, imageList[1])

        // Создаем индикаторы
        createLinearIndicator(imageList.size - 2)

        val viewPagerAdapter = ImagePagerAdapter(requireContext(), imageList)
        viewPager.adapter = viewPagerAdapter

        viewPager.currentItem = 1 // Устанавливаем указатель на 0 элемент "исходного" списка

        // Создаем отдельный поток,в котором будет изменять индикаторы и двигать пейджер
        Thread{
            var stopOnException = false
            while(true && !stopOnException){
                progress = 0f
                while (progress != 100f){
                    Thread.sleep(40) // 40* 100 = 4000 ms
                    progress += 1f // 100 iteration
                    try {
                        requireActivity().runOnUiThread {
                            updateLinearIndicator(getIndexByPosition(viewPager.currentItem))
                        }
                    }
                    catch (e: Exception){
                        stopOnException = true
                        break
                    }
                }
                try {
                    requireActivity().runOnUiThread {
                        viewPager.setCurrentItem( viewPager.currentItem + 1,true)
                    }
                }
                catch (e: Exception){
                    stopOnException = true
                    break
                }
            }
        }.start()

        // Чтобы не обрывать анимацию перелистывания у пользователя,
        // когда подменяем первый или последний элемент
        // мы ждем конец анимации (изменится стейт) и сдвигаем указатель
        var skipTo: Int? = null
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                // Размер списка на 2 элемента больше количества фото и индикаторов,
                // поэтому корректируем позицию текущего индикатора
                val positionForIndicator: Int
                skipTo = when (position) {
                    0 -> {
                        positionForIndicator = viewPagerAdapter.count - 2
                        viewPagerAdapter.count - 2
                    }
                    viewPagerAdapter.count - 1 -> {
                        positionForIndicator = 0
                        1
                    }
                    else -> {
                        positionForIndicator = position - 1
                        null
                    }
                }
                // Обновляем цвета индикаторов
                updateLinearIndicator(positionForIndicator)
            }
            override fun onPageScrollStateChanged(state: Int) {
                if (state == SCROLL_STATE_SETTLING) progress = 0f
                // Когда анимация закончена и если есть необходимость, то сдвигаем указатель
                if (state == 0 && skipTo != null){
                    viewPager.setCurrentItem(skipTo!!, false)
                    skipTo = null
                }
            }
        })
    }

    /**
     * Создает индикатор для каждой картинки
     * @Param [count] count Количество картинок
     */
    private fun createLinearIndicator(count: Int){

        for(i in 0 until count){
            val linear = LinearProgressIndicator(requireContext())
            val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            layoutParams.marginStart = 7
            layoutParams.marginEnd = 7
            linear.trackCornerRadius = 50
            linear.layoutParams = layoutParams
            linear.trackColor = resources.getColor(R.color.grey)
            linear.setIndicatorColor(resources.getColor(R.color.white))
            linear.indicatorDirection = LinearProgressIndicator.INDICATOR_DIRECTION_START_TO_END
            linear.setProgressCompat(if (i < viewPager.currentItem) 100 else{ if (i == viewPager.currentItem) progress.toInt() else 0}, true)
            linearIndicator.addView(linear)
        }

        updateLinearIndicator(0)
    }

    /**
     * Обновляет индикаторы
     * @Param [position] position индекс текущей картинки
     */
    private fun updateLinearIndicator(position: Int){

        for(i in 0 until linearIndicator.childCount){
            val linear: LinearProgressIndicator = linearIndicator.getChildAt(i) as LinearProgressIndicator
            linear.setProgressCompat(if (i < position) 100 else{ if (i == position) progress.toInt() else 0}, i <= position)
        }
    }

    /**
     * Вычисляет индекс индикатора/картинки от поизиции педжера
     * @Param [position] position указатель пейджера
     * @return индекс индикатора/картинки
     */
    private fun getIndexByPosition(position: Int): Int{
        return when (position) {
            0 -> {
                viewPager.adapter!!.count - 2
            }
            viewPager.adapter!!.count - 1 -> {
                0
            }
            else -> {
                position - 1
            }
        }
    }

    /**
     * Переопределяем метод разрушения view, удаляем маркер
     */
    override fun onDestroy() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        super.onDestroy()
        val marker = requireActivity().findViewById<RelativeLayout>(R.id.marker)
        (marker?.parent as ViewGroup?)?.removeView(marker)
    }
}