package com.trifonov.indoor_navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R

class AudienceRouteAdapter(
    private val audienceList: List<String>
): RecyclerView.Adapter<AudienceRouteAdapter.AudienceRouteViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudienceRouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audience_route_item, parent,false)
        return AudienceRouteViewHolder(view)
    }
    override fun getItemCount(): Int {
        return audienceList.size
    }

    override fun onBindViewHolder(holder: AudienceRouteViewHolder, position: Int) {
        holder.title.text = audienceList[position]
        holder.subtitle.text = audienceList[position]
        holder.itemView.setOnClickListener{
            println("Click")
        }
    }
    class AudienceRouteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title: TextView
        val subtitle: TextView
        init {
            title = itemView.findViewById(R.id.title)
            subtitle = itemView.findViewById(R.id.subtitle)
        }
    }
}