package com.rm.freedrawview

/**
 * Created by Riccardo on 22/11/16.
 */

interface PathRedoUndoCountChangeListener {
    fun onUndoCountChanged(undoCount: Int)

    fun onRedoCountChanged(redoCount: Int)
}
