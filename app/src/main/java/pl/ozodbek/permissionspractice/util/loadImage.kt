package pl.ozodbek.permissionspractice.util

import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.size.ViewSizeResolver
import pl.ozodbek.permissionspractice.R

fun <T> ImageView.loadImage(image: T?) {
    this.load(image.takeIf { it?.toString()?.isNotBlank() == true } ?: R.drawable.ic_error_placeholder) {
        crossfade(true)
        placeholder(R.drawable.ic_error_placeholder)
        error(R.drawable.ic_error_placeholder)
        size(ViewSizeResolver(this@loadImage))
        memoryCachePolicy(CachePolicy.ENABLED)
        diskCachePolicy(CachePolicy.ENABLED)
    }


    /** IMAGE LOADING CACHE

    val imageLoader = ImageLoader.Builder(context)
    .respectCacheHeaders(false)
    .build()
    Coil.setImageLoader(imageLoader)


     */
}