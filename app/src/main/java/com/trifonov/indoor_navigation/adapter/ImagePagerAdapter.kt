package com.trifonov.indoor_navigation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import com.trifonov.indoor_navigation.R
import java.util.Objects

class ImagePagerAdapter(
    val context: Context,
    private val images: List<Int>
) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.point_image_item, container, false)
        val imageView = view.findViewById<ImageView>(R.id.image_item)
        val imagePosition = position % images.size
        imageView.setImageResource(images[imagePosition])
        container.addView(view)
        return view
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}