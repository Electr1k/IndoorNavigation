package com.trifonov.indoor_navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R

class AudienceTypeAdapter(
    private val audienceTypesList: List<String>
): RecyclerView.Adapter<AudienceTypeAdapter.AudienceTypeViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudienceTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audience_type_item, parent,false)
        return AudienceTypeViewHolder(view)
    }
    override fun getItemCount(): Int {
        return audienceTypesList.size
    }

    override fun onBindViewHolder(holder: AudienceTypeViewHolder, position: Int) {
        holder.title.text = audienceTypesList[position]
        holder.itemView.setOnClickListener{
            println("Click")
        }
    }
    class AudienceTypeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title: TextView
        init {
            title = itemView.findViewById(R.id.title)
        }
    }
}