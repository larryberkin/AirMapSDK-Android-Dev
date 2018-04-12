package com.airmap.airmapsdktest

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat

import com.airmap.airmapsdk.models.Coordinate
import com.mapbox.mapboxsdk.geometry.LatLng

class Utils : com.airmap.airmapsdk.util.Utils() {


    companion object {

        private val compassDirections = intArrayOf(
                com.airmap.airmapsdk.R.string.cardinal_direction_n,
                com.airmap.airmapsdk.R.string.cardinal_direction_nnne,
                com.airmap.airmapsdk.R.string.cardinal_direction_ne,
                com.airmap.airmapsdk.R.string.cardinal_direction_ene,
                com.airmap.airmapsdk.R.string.cardinal_direction_e,
                com.airmap.airmapsdk.R.string.cardinal_direction_ese,
                com.airmap.airmapsdk.R.string.cardinal_direction_se,
                com.airmap.airmapsdk.R.string.cardinal_direction_sse,
                com.airmap.airmapsdk.R.string.cardinal_direction_s,
                com.airmap.airmapsdk.R.string.cardinal_direction_ssw,
                com.airmap.airmapsdk.R.string.cardinal_direction_sw,
                com.airmap.airmapsdk.R.string.cardinal_direction_wsw,
                com.airmap.airmapsdk.R.string.cardinal_direction_w,
                com.airmap.airmapsdk.R.string.cardinal_direction_wnw,
                com.airmap.airmapsdk.R.string.cardinal_direction_nw,
                com.airmap.airmapsdk.R.string.cardinal_direction_nnw
        )

        @JvmStatic
        fun directionFromBearing(context: Context, bearing: Double) = context.resources.getString(compassDirections[(bearing / 22.5 + 0.5).toInt() % 16])!!

        @JvmStatic
        fun minutesToMinSec(context: Context, minutes: Double): String {
            val min = minutes.toInt()
            val sec = ((minutes - min) * 60).toInt()
            return context.getString(R.string.minutes_seconds, min, sec)
        }

        @JvmStatic
        fun ktsToMph(kts: Double) = kts * 1.151

        @JvmStatic
        fun getBitmap(context: Context, @DrawableRes drawableResId: Int): Bitmap {
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            return when (drawable) {
                is BitmapDrawable -> drawable.bitmap
                is VectorDrawable -> getBitmap(drawable)
                else -> throw IllegalArgumentException("Unsupported drawable type")
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private fun getBitmap(vectorDrawable: VectorDrawable): Bitmap {
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }
    }
}
