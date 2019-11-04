package com.rm.freedrawview

import android.content.res.Resources
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import android.graphics.Paint

/**
 * Created by Riccardo Moro on 10/23/2016.
 */

object FreeDrawHelper {

    /**
     * Function used to check whenever a list of points is a line or a path to draw
     */
    internal fun isAPoint(points: List<Point>): Boolean {
        if (points.size == 0)
            return false

        if (points.size == 1)
            return true

        for (i in 1 until points.size) {
            if (points[i - 1].x != points[i].x || points[i - 1].y != points[i].y)
                return false
        }

        return true
    }

    /**
     * Create, initialize and setup a paint
     */
    internal fun createPaintAndInitialize(
        paintColor: Int, paintAlpha: Int,
        paintWidth: Float, fill: Boolean
    ): Paint {

        val paint = createPaint()

        initializePaint(paint, paintColor, paintAlpha, paintWidth, fill)

        return paint
    }

    internal fun createPaint(): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG)
    }

    internal fun initializePaint(
        paint: Paint, paintColor: Int, paintAlpha: Int, paintWidth: Float,
        fill: Boolean
    ) {

        if (fill) {
            setupFillPaint(paint)
        } else {
            setupStrokePaint(paint)
        }

        paint.strokeWidth = paintWidth
        paint.color = paintColor
        paint.alpha = paintAlpha
    }

    internal fun setupFillPaint(paint: Paint) {
        paint.style = Paint.Style.FILL
    }

    internal fun setupStrokePaint(paint: Paint) {
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.pathEffect = ComposePathEffect(
            CornerPathEffect(100f),
            CornerPathEffect(100f)
        )
        paint.style = Paint.Style.STROKE
    }

    internal fun copyFromPaint(from: Paint, to: Paint, copyWidth: Boolean) {

        to.color = from.color
        to.alpha = from.alpha

        if (copyWidth) {
            to.strokeWidth = from.strokeWidth
        }
    }

    internal fun copyFromValues(
        to: Paint, color: Int, alpha: Int, strokeWidth: Float,
        copyWidth: Boolean
    ) {

        to.color = color
        to.alpha = alpha

        if (copyWidth) {
            to.strokeWidth = strokeWidth
        }
    }

    /**
     * Converts a given dp number to it's pixel corresponding number
     */
    fun convertDpToPixels(dp: Float): Float {
        return dp * Resources.getSystem().displayMetrics.density
    }

    /**
     * Converts a given pixel number to it's dp corresponding number
     */
    fun convertPixelsToDp(px: Float): Float {
        return px / Resources.getSystem().displayMetrics.density
    }
}
