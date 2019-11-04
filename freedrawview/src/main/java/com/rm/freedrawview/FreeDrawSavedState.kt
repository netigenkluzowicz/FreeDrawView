package com.rm.freedrawview

import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.view.View

import androidx.annotation.ColorInt
import androidx.annotation.IntRange

import java.util.ArrayList

/**
 * Created by Riccardo Moro on 11/4/2016.
 */

internal class FreeDrawSavedState : View.BaseSavedState {

    var paths = ArrayList<HistoryPath>()
    var canceledPaths = ArrayList<HistoryPath>()

    @get:ColorInt
    var paintColor: Int = 0
        private set
    @get:IntRange(from = 0, to = 255)
    var paintAlpha: Int = 0
        private set
    var currentPaintWidth: Float = 0.toFloat()
        private set

    var resizeBehaviour: ResizeBehaviour? = null
        private set

    var lastDimensionW: Int = 0
        private set
    var lastDimensionH: Int = 0
        private set

    val currentPaint: Paint
        get() {

            val paint = FreeDrawHelper.createPaint()
            FreeDrawHelper.setupStrokePaint(paint)
            FreeDrawHelper.copyFromValues(paint, paintColor, paintAlpha, currentPaintWidth, true)
            return paint
        }

    constructor(
        superState: Parcelable, paths: ArrayList<HistoryPath>,
        canceledPaths: ArrayList<HistoryPath>, paintWidth: Float,
        paintColor: Int, paintAlpha: Int, resizeBehaviour: ResizeBehaviour?,
        lastDimensionW: Int, lastDimensionH: Int
    ) : super(superState) {

        this.paths = paths
        this.canceledPaths = canceledPaths
        currentPaintWidth = paintWidth

        this.paintColor = paintColor
        this.paintAlpha = paintAlpha

        this.resizeBehaviour = resizeBehaviour

        this.lastDimensionW = lastDimensionW
        this.lastDimensionH = lastDimensionH
    }

    // Parcelable stuff
    private constructor(`in`: Parcel) : super(`in`) {

        `in`.readTypedList(paths, HistoryPath.CREATOR)
        `in`.readTypedList(canceledPaths, HistoryPath.CREATOR)

        paintColor = `in`.readInt()
        paintAlpha = `in`.readInt()
        currentPaintWidth = `in`.readFloat()

        resizeBehaviour = `in`.readSerializable() as ResizeBehaviour

        lastDimensionW = `in`.readInt()
        lastDimensionH = `in`.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)

        out.writeTypedList(paths)
        out.writeTypedList(canceledPaths)

        out.writeInt(paintColor)
        out.writeInt(paintAlpha)
        out.writeFloat(currentPaintWidth)

        out.writeSerializable(resizeBehaviour)

        out.writeInt(lastDimensionW)
        out.writeInt(lastDimensionH)
    }


    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FreeDrawSavedState> {
        override fun createFromParcel(parcel: Parcel): FreeDrawSavedState {
            return FreeDrawSavedState(parcel)
        }

        override fun newArray(size: Int): Array<FreeDrawSavedState?> {
            return arrayOfNulls(size)
        }
    }
}
