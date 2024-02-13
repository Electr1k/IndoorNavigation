package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceSearchAdapter
import com.trifonov.indoor_navigation.map.MapConstants
import com.trifonov.indoor_navigation.map.MapConstants.dotList

class SearchFragment: CustomFragment() {
    private lateinit var search: EditText
    private lateinit var RV: RecyclerView
    private lateinit var adapter: AudienceSearchAdapter

    @Nullable
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_fragment, container, false)
        return view
    }
    @MainThread
    @SuppressLint("KotlinNullnessAnnotation")
    override fun onViewCreated(@NonNull view: View, @Nullable savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RV = view.findViewById(R.id.list_item)
        search = view.findViewById(R.id.search_input)
        val list = dotList.filter { it.getName().isNotEmpty() }
        adapter = AudienceSearchAdapter(list) { dot ->
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            MapConstants.mapConnector.moveCameraToDot(dot)
            val bundle = Bundle()
            bundle.putInt("id", dot.getId())
            findNavController().navigate(R.id.action_search_to_scan, bundle)
        }
        RV.adapter = adapter
        search.addTextChangedListener {
            if (it.toString().isEmpty()) adapter.updateList(dotList.filter { dot -> dot.getName().isNotEmpty() })
            else{
                adapter.updateList(dotList.filter { dot -> dot.getName().contains(it.toString().trim(), ignoreCase = true) })
            }
        }
    }

    override fun onStart() {
        mBottomSheet.visibility = View.VISIBLE
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val slideUpAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        mBottomSheet.startAnimation(slideUpAnimation)
        super.onStart()
    }
}