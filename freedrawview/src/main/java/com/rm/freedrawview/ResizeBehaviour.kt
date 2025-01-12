package com.rm.freedrawview

/**
 * Created by Riccardo Moro on 11/6/2016.
 */

/**
 * When RMFreeDrawView dimensions are changed, you can apply one of the following behaviours
 * [.CLEAR] - It just clear the View from every previous paint
 * [.FIT_XY] - It stretch the content to fit the new dimensions
 * [.CROP] - Keep the exact position of the previous point, if the dimensions changes, there
 * may be points outside the view and not visible
 */
enum class ResizeBehaviour {
    CLEAR,
    FIT_XY,
    CROP
}
