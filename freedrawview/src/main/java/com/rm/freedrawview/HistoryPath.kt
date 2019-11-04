package com.rm.freedrawview

import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.*

/**
 * Created by Riccardo Moro on 9/27/2016.
 */

class HistoryPath : Parcelable, Serializable {

    var points: ArrayList<Point>? = ArrayList()
    var paintColor: Int = 0
    var paintAlpha: Int = 0
    var paintWidth: Float = 0.toFloat()
    var originX: Float = 0.toFloat()
    var originY: Float = 0.toFloat()
    var isPoint: Boolean = false

    @Transient
    var path: Path? = null
        get() {
            if (field == null) {
                generatePath()
            }

            return field
        }

    @Transient
    var paint: Paint? = null
        get() {
            if (field == null) {
                generatePaint()
            }

            return field
        }

    constructor(points: ArrayList<Point>, paint: Paint) {
        this.points = ArrayList(points)
        this.paintColor = paint.color
        this.paintAlpha = paint.alpha
        this.paintWidth = paint.strokeWidth
        this.originX = points[0].x
        this.originY = points[0].y
        this.isPoint = FreeDrawHelper.isAPoint(points)

        generatePath()
        generatePaint()
    }

    fun generatePath() {

        path = Path()

        if (points != null) {
            var first = true

            for (i in points!!.indices) {

                val point = points!![i]

                if (first) {
                    path!!.moveTo(point.x, point.y)
                    first = false
                } else {
                    path!!.lineTo(point.x, point.y)
                }
            }
        }
    }

    private fun generatePaint() {

        paint = FreeDrawHelper.createPaintAndInitialize(
            paintColor, paintAlpha, paintWidth,
            isPoint
        )
    }

    // Parcelable stuff
    private constructor(`in`: Parcel) {
        `in`.readTypedList(points, Point.CREATOR)

        paintColor = `in`.readInt()
        paintAlpha = `in`.readInt()
        paintWidth = `in`.readFloat()

        originX = `in`.readFloat()
        originY = `in`.readFloat()

        isPoint = `in`.readByte().toInt() != 0

        generatePath()
        generatePaint()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(points)

        dest.writeInt(paintColor)
        dest.writeInt(paintAlpha)
        dest.writeFloat(paintWidth)

        dest.writeFloat(originX)
        dest.writeFloat(originY)

        dest.writeByte((if (isPoint) 1 else 0).toByte())
    }

    override fun toString(): String {
        return "Point: " + isPoint + "\n" +
                "Points: " + points + "\n" +
                "Color: " + paintColor + "\n" +
                "Alpha: " + paintAlpha + "\n" +
                "Width: " + paintWidth
    }

    companion object CREATOR : Parcelable.Creator<HistoryPath> {

        const val serialVersionUID = 41L

        private val TAG = HistoryPath::class.java!!.getSimpleName()

        override fun createFromParcel(parcel: Parcel): HistoryPath {
            return HistoryPath(parcel)
        }

        override fun newArray(size: Int): Array<HistoryPath?> {
            return arrayOfNulls(size)
        }
    }
}
