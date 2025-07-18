package dev.icerock.education.practicetask.utils

import android.content.Context
import android.widget.ImageView
import com.m2mobi.markymarkandroid.ImageLoader
import com.squareup.picasso.Picasso

class PicassoImageLoader(private val context: Context) : ImageLoader {
    override fun loadImage(targetImageView: ImageView, url: String) {
        Picasso.Builder(context)
            .build()
            .load(url)
            .into(targetImageView)
    }
}