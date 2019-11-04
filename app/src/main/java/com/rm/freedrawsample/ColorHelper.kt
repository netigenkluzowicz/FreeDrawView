package com.rm.freedrawsample

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color

import androidx.annotation.ColorInt

/**
 * Created by Riccardo Moro on 10/23/2016.
 */

object ColorHelper {

    @ColorInt
    fun getRandomMaterialColor(context: Context): Int {
        val colors = context.resources.obtainTypedArray(R.array.material_colors)
        val index = (Math.random() * colors.length()).toInt()
        val color = colors.getColor(index, Color.BLACK)
        colors.recycle()
        return color
    }
}
