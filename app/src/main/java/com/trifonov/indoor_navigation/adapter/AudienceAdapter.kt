package com.trifonov.indoor_navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R

class AudienceAdapter(
    private val audienceList: List<String>
): RecyclerView.Adapter<AudienceAdapter.AudienceViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudienceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audience_item, parent,false)
        return  AudienceViewHolder(view)
    }
    override fun getItemCount(): Int {
        return audienceList.size
    }

    override fun onBindViewHolder(holder: AudienceViewHolder, position: Int) {
        holder.name.text = audienceList[position]
        holder.itemView.setOnClickListener{
            println("Click")
        }
    }
    class AudienceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name: TextView
        init {
            name = itemView.findViewById(R.id.name)
        }
    }
}