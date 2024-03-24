package com.trifonov.indoor_navigation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import coil.load
import com.facebook.shimmer.ShimmerFrameLayout
import com.trifonov.indoor_navigation.MainActivity
import com.trifonov.indoor_navigation.R
import com.trifonov.indoor_navigation.data.dto.Location
import com.trifonov.indoor_navigation.mapView.MapConstants.baseUrl

class ImagePagerAdapter(
    val activity: MainActivity,
    private val location: Location,
    private val images: List<String>,
    private val stopProgress: () -> Unit,
    private val resumeProgress: () -> Unit,
) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.point_image_item, container, false)
        val imageView = view.findViewById<ImageView>(R.id.image_item)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_image)
        val imagePosition = position % images.size
        shimmer.startShimmer()
        imageView.load("${baseUrl}locations/${location.id}/photos${images[imagePosition]}", activity.imageLoader){
            error(R.drawable.bad_connection_icon) // Замените R.drawable.placeholder на свой ресурс запасного изображения
            listener(
                onSuccess = { _, _ ->
                shimmer.stopShimmer()
                shimmer.hideShimmer()
                resumeProgress()
                },
            onError = { _, _ ->
                shimmer.stopShimmer()
                shimmer.hideShimmer()
                resumeProgress()
            },
                onStart = {
                    stopProgress()
                })
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