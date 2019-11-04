package com.rm.freedrawview

import java.io.Serializable
import java.util.*

/**
 * Created by Riccardo on 23/05/2017.
 */

class FreeDrawSerializableState(
    var canceledPaths: ArrayList<HistoryPath> = arrayListOf(),
    var paths: ArrayList<HistoryPath> = arrayListOf(), var paintColor: Int = 0, var paintAlpha: Int = 0,
    var paintWidth: Float = 0f, var resizeBehaviour: ResizeBehaviour? = null,
    lastW: Int, lastH: Int
) : Serializable {


    var lastDimensionW: Int = 0
    var lastDimensionH: Int = 0

    init {
        canceledPaths = canceledPaths ?: ArrayList()
        paths = paths ?: ArrayList()
        paintWidth = if (paintWidth >= 0) paintWidth else 0f
        lastDimensionW = if (lastW >= 0) lastW else 0
        lastDimensionH = if (lastH >= 0) lastH else 0
    }

    companion object {

        internal const val serialVersionUID = 40L
    }
}
