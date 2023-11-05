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
import com.trifonov.indoor_navigation.adapter.AudienceRouteAdapter
import com.trifonov.indoor_navigation.adapter.AudienceTypeAdapter

class RouteFragment: CustomFragment() {

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
        val typesRV = view.findViewById<RecyclerView>(R.id.audience_types)
        val audienceRV = view.findViewById<RecyclerView>(R.id.result_search)
        typesRV.isNestedScrollingEnabled = false
        audienceRV.isNestedScrollingEnabled = false
        typesRV.setHasFixedSize(true)
        audienceRV.setHasFixedSize(true)
        val list = listOf(
            "Туалет",
            "Аудитория",
            "Лекционный зал",
            "Кафе",
            "Зона отдыха"
        )
        val listResult = listOf("Офис", "Туалет", "Офис","Офис", "Туалет","Другое", "Офис",)
        audienceRV.adapter = AudienceRouteAdapter(listResult)
        typesRV.adapter = AudienceTypeAdapter(list)
    }
}