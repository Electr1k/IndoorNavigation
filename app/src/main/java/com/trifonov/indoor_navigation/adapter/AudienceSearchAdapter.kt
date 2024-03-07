package com.trifonov.indoor_navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.mapView.Dot

class AudienceSearchAdapter(
    private var audienceList: List<Dot>,
    private val action: (Dot) -> Unit
): RecyclerView.Adapter<AudienceSearchAdapter.AudienceSearchViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudienceSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.audience_item, parent,false)
        return  AudienceSearchViewHolder(view)
    }
    override fun getItemCount(): Int {
        return audienceList.size
    }

    override fun onBindViewHolder(holder: AudienceSearchViewHolder, position: Int) {
        holder.title.text = audienceList[position].getName()
        holder.itemView.setOnClickListener{
            action(audienceList[position])
        }
    }

    internal fun updateList(newList: List<Dot>){
        audienceList = newList
        notifyDataSetChanged()
    }

    class AudienceSearchViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title: TextView
        init {
            title = itemView.findViewById(R.id.title)
        }
    }
}