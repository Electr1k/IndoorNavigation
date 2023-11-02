package com.trifonov.indoor_navigation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.adapter.AudienceAdapter

class SearchFragment: CustomFragment() {
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
        val recyclerView = view.findViewById<RecyclerView>(R.id.list_item)
        val list = mutableListOf<String>()
        repeat(25) {
            list.add(it, "Г - $it")
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AudienceAdapter(list)
    }
}