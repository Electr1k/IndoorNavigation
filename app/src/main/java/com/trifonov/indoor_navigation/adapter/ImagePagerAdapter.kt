package com.trifonov.indoor_navigation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.viewpager.widget.PagerAdapter
import coil.ImageLoader
import coil.load
import coil.request.CachePolicy
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.mapView.MapConstants
import com.trifonov.indoor_navigation.mapView.MapConstants.baseUrl
import java.util.Objects

class ImagePagerAdapter(
    val context: Context,
    private val location: String,
    private val images: List<String>
) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.point_image_item, container, false)
        val imageView = view.findViewById<ImageView>(R.id.image_item)
        val imagePosition = position % images.size
        println("${baseUrl}location/$location/photos${images[imagePosition]}")
        val imgLoader = ImageLoader.Builder(context)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()

        imageView.load("${baseUrl}location/$location/photos${images[imagePosition]}", imgLoader){
            error(R.drawable.bad_connection_icon) // Замените R.drawable.placeholder на свой ресурс запасного изображения
//            listener(onError = { request, throwable ->
//                // Логика обработки ошибки загрузки
//                // Попытка перезагрузки изображения
//                imgLoader.enqueue(request)
//            })
        }

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