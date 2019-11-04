package com.rm.freedrawview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.AsyncTask
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import java.util.*

/**
 * Created by Riccardo Moro on 9/10/2016.
 */
class FreeDrawView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), View.OnTouchListener {

    private var mCurrentPaint: Paint? = null
    private var mCurrentPath: Path? = null

    /**
     * Get the current behaviour on view resize
     */
    /**
     * Set what to do when the view is resized (on rotation if its dimensions are not fixed)
     * [ResizeBehaviour]
     */
    var resizeBehaviour: ResizeBehaviour? = null

    private var mPoints = ArrayList<Point>()
    private var mPaths: ArrayList<HistoryPath> = ArrayList()
    private var mCanceledPaths: ArrayList<HistoryPath> = ArrayList()

    @ColorInt
    private var mPaintColor = DEFAULT_COLOR
    @IntRange(from = 0, to = 255)
    private var mPaintAlpha = DEFAULT_ALPHA

    private var mLastDimensionW = -1
    private var mLastDimensionH = -1

    private var mFinishPath = false

    private var mPathDrawnListener: PathDrawnListener? = null
    private var mPathRedoUndoCountChangeListener: PathRedoUndoCountChangeListener? = null

    /**
     * Get the current paint color without it's alpha
     */
    /**
     * Set the paint color
     *
     * @param color The now color to be applied to the
     */
    // Restore the previous alpha
    var paintColor: Int
        @ColorInt
        get() = mPaintColor
        set(@ColorInt color) {

            invalidate()

            mPaintColor = color

            mCurrentPaint!!.color = mPaintColor
            mCurrentPaint!!.alpha = mPaintAlpha
        }

    /**
     * Get the current color with the current alpha
     */
    val paintColorWithAlpha: Int
        @ColorInt
        get() = mCurrentPaint!!.color

    /**
     * [.getPaintWidth]
     */
    val paintWidth: Float
        @FloatRange(from = 0.0)
        get() = getPaintWidth(false)

    /**
     * Get the current paint alpha
     */
    /**
     * Set the paint opacity, must be between 0 and 1
     *
     * @param alpha The alpha to apply to the paint
     */
    // Finish current path and redraw, so that the new setting is applied only to the next path
    var paintAlpha: Int
        @IntRange(from = 0, to = 255)
        get() = mPaintAlpha
        set(@IntRange(from = 0, to = 255) alpha) {
            invalidate()

            mPaintAlpha = alpha
            mCurrentPaint!!.alpha = mPaintAlpha
        }

    /**
     * Get how many undo operations are available
     */
    val undoCount: Int
        get() = mPaths!!.size

    /**
     * Get how many redo operations are available
     */
    val redoCount: Int
        get() = mCanceledPaths!!.size

    /**
     * Get a serializable object with all the needed info about the current draw and state
     *
     * @return A [FreeDrawSerializableState] containing all the needed data
     */
    val currentViewStateAsSerializable: FreeDrawSerializableState
        get() = FreeDrawSerializableState(
            mCanceledPaths, mPaths, paintColor,
            paintAlpha, paintWidth, resizeBehaviour,
            mLastDimensionW, mLastDimensionH
        )

    init {

        setOnTouchListener(this)

        var a: TypedArray? = null
        try {

            a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.FreeDrawView,
                defStyleAttr, 0
            )

            initPaints(a)
        } finally {
            a?.recycle()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {

        // Get the superclass parcelable state
        val superState = super.onSaveInstanceState()

        if (mPoints.size > 0) {// Currently doing a line, save it's current path
            createHistoryPathFromPoints()
        }

        return FreeDrawSavedState(
            superState, mPaths, mCanceledPaths,
            paintWidth, paintColor, paintAlpha,
            resizeBehaviour, mLastDimensionW, mLastDimensionH
        )
    }

    override fun onRestoreInstanceState(state: Parcelable) {

        // If not instance of my state, let the superclass handle it
        if (state !is FreeDrawSavedState) {
            super.onRestoreInstanceState(state)
            return
        }

// Superclass restore state
        super.onRestoreInstanceState(state.superState)

        // My state restore
        mPaths = state.paths
        mCanceledPaths = state.canceledPaths
        mCurrentPaint = state.currentPaint

        setPaintWidthPx(state.currentPaintWidth)
        paintColor = state.paintColor
        paintAlpha = state.paintAlpha

        resizeBehaviour = state.resizeBehaviour

        // Restore the last dimensions, so that in onSizeChanged i can calculate the
        // height and width change factor and multiply every point x or y to it, so that if the
        // View is resized, it adapt automatically it's points to the new width/height
        mLastDimensionW = state.lastDimensionW
        mLastDimensionH = state.lastDimensionH

        notifyRedoUndoCountChanged()
    }


    /**
     * Set the paint width in px
     *
     * @param widthPx The new weight in px, must be > 0
     */
    fun setPaintWidthPx(@FloatRange(from = 0.0) widthPx: Float) {
        if (widthPx > 0) {

            invalidate()

            mCurrentPaint!!.strokeWidth = widthPx
        }
    }

    /**
     * Set the paint width in dp
     *
     * @param dp The new weight in dp, must be > 0
     */
    fun setPaintWidthDp(dp: Float) {
        setPaintWidthPx(FreeDrawHelper.convertDpToPixels(dp))
    }

    /**
     * Get the current paint with in dp or pixel
     */
    @FloatRange(from = 0.0)
    fun getPaintWidth(inDp: Boolean): Float {
        return if (inDp) {
            FreeDrawHelper.convertPixelsToDp(mCurrentPaint!!.strokeWidth)
        } else {
            mCurrentPaint!!.strokeWidth
        }
    }


    /**
     * Cancel the last drawn segment
     */
    fun undoLast() {

        if (mPaths!!.size > 0) {
            // End current path
            mFinishPath = true
            invalidate()

            // Cancel the last one and redraw
            mCanceledPaths!!.add(mPaths!![mPaths!!.size - 1])
            mPaths!!.removeAt(mPaths!!.size - 1)
            invalidate()

            notifyRedoUndoCountChanged()
        }
    }

    /**
     * Re-add the first removed path and redraw
     */
    fun redoLast() {

        if (mCanceledPaths!!.size > 0) {
            mPaths!!.add(mCanceledPaths!![mCanceledPaths!!.size - 1])
            mCanceledPaths!!.removeAt(mCanceledPaths!!.size - 1)
            invalidate()

            notifyRedoUndoCountChanged()
        }
    }

    /**
     * Remove all the paths and redraw (can be undone with [.redoLast])
     */
    fun undoAll() {
        Collections.reverse(mPaths)
        mCanceledPaths!!.addAll(mPaths!!)
        mPaths = ArrayList()
        invalidate()

        notifyRedoUndoCountChanged()
    }

    /**
     * Re-add all the removed paths and redraw
     */
    fun redoAll() {

        if (mCanceledPaths!!.size > 0) {
            mPaths!!.addAll(mCanceledPaths!!)
            mCanceledPaths = ArrayList()
            invalidate()

            notifyRedoUndoCountChanged()
        }
    }

    /**
     * Get how many paths are drawn on this FreeDrawView
     *
     * @param includeCurrentlyDrawingPath Include the path that is currently been drawn
     * @return The number of paths drawn
     */
    fun getPathCount(includeCurrentlyDrawingPath: Boolean): Int {
        var size = mPaths!!.size

        if (includeCurrentlyDrawingPath && mPoints.size > 0) {
            size++
        }
        return size
    }

    /**
     * Set a path drawn listener, will be called every time a new path is drawn
     */
    fun setOnPathDrawnListener(listener: PathDrawnListener) {
        mPathDrawnListener = listener
    }

    /**
     * Remove the path drawn listener
     */
    fun removePathDrawnListener() {
        mPathDrawnListener = null
    }

    /**
     * Clear the current draw and the history
     */
    fun clearDrawAndHistory() {

        clearDraw(false)
        clearHistory(true)
    }

    /**
     * Clear the current draw
     */
    fun clearDraw() {

        clearDraw(true)
    }

    private fun clearDraw(invalidate: Boolean) {
        mPoints = ArrayList()
        mPaths = ArrayList()

        notifyRedoUndoCountChanged()

        if (invalidate) {
            invalidate()
        }
    }

    /**
     * Clear the history (paths that can be redone)
     */
    fun clearHistory() {
        clearHistory(true)
    }

    private fun clearHistory(invalidate: Boolean) {
        mCanceledPaths = ArrayList()

        notifyRedoUndoCountChanged()

        if (invalidate) {
            invalidate()
        }
    }

    /**
     * Set a redo-undo count change listener, this will be called every time undo or redo count
     * changes
     */
    fun setPathRedoUndoCountChangeListener(listener: PathRedoUndoCountChangeListener) {
        mPathRedoUndoCountChangeListener = listener
    }

    /**
     * Remove the redo-undo count listener
     */
    fun removePathRedoUndoCountChangeListener() {
        mPathRedoUndoCountChangeListener = null
    }

    /**
     * Restore the state of the draw from the given serializable state
     *
     * @param state A [FreeDrawSerializableState] containing all the draw and paint info,
     * if null, nothing will be restored. Null sub fields will be ignored
     */
    fun restoreStateFromSerializable(state: FreeDrawSerializableState) {

        mCanceledPaths = state.canceledPaths

        mPaths = state.paths

        mPaintColor = state.paintColor
        mPaintAlpha = state.paintAlpha

        mCurrentPaint!!.color = state.paintColor
        mCurrentPaint!!.alpha = state.paintAlpha
        setPaintWidthPx(state.paintWidth)

        resizeBehaviour = state.resizeBehaviour

        if (state.lastDimensionW >= 0) {
            mLastDimensionW = state.lastDimensionW
        }

        if (state.lastDimensionH >= 0) {
            mLastDimensionH = state.lastDimensionH
        }

        notifyRedoUndoCountChanged()
        invalidate()
    }

    /**
     * Create a Bitmap with the content drawn inside the view
     */
    fun getDrawScreenshot(listener: DrawCreatorListener) {
        TakeScreenShotAsyncTask(listener).execute()
    }


    // Internal methods
    private fun notifyPathStart() {
        if (mPathDrawnListener != null) {
            mPathDrawnListener!!.onPathStart()
        }
    }

    private fun notifyPathDrawn() {
        if (mPathDrawnListener != null) {
            mPathDrawnListener!!.onNewPathDrawn()
        }
    }

    private fun notifyRedoUndoCountChanged() {
        if (mPathRedoUndoCountChangeListener != null) {
            mPathRedoUndoCountChangeListener!!.onRedoCountChanged(redoCount)
            mPathRedoUndoCountChangeListener!!.onUndoCountChanged(undoCount)
        }
    }

    private fun initPaints(a: TypedArray?) {
        mCurrentPaint = FreeDrawHelper.createPaint()

        mCurrentPaint!!.color = a?.getColor(
            R.styleable.FreeDrawView_paintColor,
            mPaintColor
        ) ?: mPaintColor
        mCurrentPaint!!.alpha = a?.getInt(R.styleable.FreeDrawView_paintAlpha, mPaintAlpha) ?: mPaintAlpha
        mCurrentPaint!!.setStrokeWidth(
            a?.getDimensionPixelSize(
                R.styleable.FreeDrawView_paintWidth,
                FreeDrawHelper.convertDpToPixels(DEFAULT_STROKE_WIDTH).toInt()
            )?.toFloat()
                ?: FreeDrawHelper.convertDpToPixels(DEFAULT_STROKE_WIDTH)
        )

        FreeDrawHelper.setupStrokePaint(mCurrentPaint!!)

        if (a != null) {
            val resizeBehaviour = a.getInt(R.styleable.FreeDrawView_resizeBehaviour, -1)
            this.resizeBehaviour = if (resizeBehaviour == 0)
                ResizeBehaviour.CLEAR
            else if (resizeBehaviour == 1)
                ResizeBehaviour.FIT_XY
            else if (resizeBehaviour == 2)
                ResizeBehaviour.CROP
            else
                ResizeBehaviour.CROP
        }
    }

    private fun createAndCopyColorAndAlphaForFillPaint(from: Paint, copyWidth: Boolean): Paint {
        val paint = FreeDrawHelper.createPaint()
        FreeDrawHelper.setupFillPaint(paint)
        paint.color = from.color
        paint.alpha = from.alpha
        if (copyWidth) {
            paint.strokeWidth = from.strokeWidth
        }
        return paint
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        if (mPaths!!.size == 0 && mPoints.size == 0) {
            return
        }

        // Avoid concurrency errors by first setting the finished path variable to false
        val finishedPath = mFinishPath
        mFinishPath = false

        for (currentPath in mPaths!!) {

            // If the path is just a single point, draw as a point
            if (currentPath.isPoint) {

                canvas.drawCircle(
                    currentPath.originX, currentPath.originY,
                    currentPath.paint!!.strokeWidth / 2, currentPath.paint!!
                )
            } else {// Else draw the complete path

                canvas.drawPath(currentPath.path!!, currentPath.paint!!)
            }
        }

        // Initialize the current path
        if (mCurrentPath == null)
            mCurrentPath = Path()
        else
            mCurrentPath!!.rewind()

        // If a single point, add a circle to the path
        if (mPoints.size == 1 || FreeDrawHelper.isAPoint(mPoints)) {

            canvas.drawCircle(
                mPoints[0].x, mPoints[0].y,
                mCurrentPaint!!.strokeWidth / 2,
                createAndCopyColorAndAlphaForFillPaint(mCurrentPaint!!, false)
            )
        } else if (mPoints.size != 0) {// Else draw the complete series of points

            var first = true

            for (point in mPoints) {

                if (first) {
                    mCurrentPath!!.moveTo(point.x, point.y)
                    first = false
                } else {
                    mCurrentPath!!.lineTo(point.x, point.y)
                }
            }

            canvas.drawPath(mCurrentPath!!, mCurrentPaint!!)
        }

        // If the path is finished, add it to the history
        if (finishedPath && mPoints.size > 0) {
            createHistoryPathFromPoints()
        }
    }

    // Create a path from the current points
    private fun createHistoryPathFromPoints() {
        mPaths!!.add(HistoryPath(mPoints, Paint(mCurrentPaint)))

        mPoints = ArrayList()

        notifyPathDrawn()
        notifyRedoUndoCountChanged()
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {

        if (motionEvent.action == MotionEvent.ACTION_DOWN) {
            notifyPathStart()
        }
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        // Clear all the history when restarting to draw
        mCanceledPaths = ArrayList()

        if (motionEvent.action != MotionEvent.ACTION_UP && motionEvent.action != MotionEvent.ACTION_CANCEL) {
            var point: Point
            for (i in 0 until motionEvent.historySize) {
                point = Point()
                point.x = motionEvent.getHistoricalX(i)
                point.y = motionEvent.getHistoricalY(i)
                mPoints.add(point)
            }
            point = Point()
            point.x = motionEvent.x
            point.y = motionEvent.y
            mPoints.add(point)
            mFinishPath = false
        } else
            mFinishPath = true

        invalidate()
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        var xMultiplyFactor = 1f
        var yMultiplyFactor = 1f


        if (mLastDimensionW == -1) {
            mLastDimensionW = w
        }

        if (mLastDimensionH == -1) {
            mLastDimensionH = h
        }

        if (w >= 0 && w != oldw && w != mLastDimensionW) {
            xMultiplyFactor = w.toFloat() / mLastDimensionW
            mLastDimensionW = w
        }

        if (h >= 0 && h != oldh && h != mLastDimensionH) {
            yMultiplyFactor = h.toFloat() / mLastDimensionH
            mLastDimensionH = h
        }

        multiplyPathsAndPoints(xMultiplyFactor, yMultiplyFactor)
    }

    // Translate all the paths, used every time that this view size is changed
    private fun multiplyPathsAndPoints(xMultiplyFactor: Float, yMultiplyFactor: Float) {
        var xMultiplyFactor = xMultiplyFactor
        var yMultiplyFactor = yMultiplyFactor

        // If both factors == 1 or <= 0 or no paths/points to apply things, just return
        if (xMultiplyFactor == 1f && yMultiplyFactor == 1f
            || xMultiplyFactor <= 0 || yMultiplyFactor <= 0 ||
            mPaths!!.size == 0 && mCanceledPaths!!.size == 0 && mPoints.size == 0
        ) {
            return
        }

        if (resizeBehaviour == ResizeBehaviour.CLEAR) {// If clear, clear all and return
            mPaths = ArrayList()
            mCanceledPaths = ArrayList()
            mPoints = ArrayList()
            return
        } else if (resizeBehaviour == ResizeBehaviour.CROP) {
            yMultiplyFactor = 1f
            xMultiplyFactor = yMultiplyFactor
        }

        // Adapt drawn paths
        for (historyPath in mPaths!!) {

            if (historyPath.isPoint) {
                historyPath.originX = historyPath.originX * xMultiplyFactor
                historyPath.originY = historyPath.originY * yMultiplyFactor
            } else {
                for (point in historyPath.points!!) {
                    point.x *= xMultiplyFactor
                    point.y *= yMultiplyFactor
                }
            }

            historyPath.generatePath()
        }

        // Adapt canceled paths
        for (historyPath in mCanceledPaths!!) {

            if (historyPath.isPoint) {
                historyPath.originX = historyPath.originX * xMultiplyFactor
                historyPath.originY = historyPath.originY * yMultiplyFactor
            } else {
                for (point in historyPath.points!!) {
                    point.x *= xMultiplyFactor
                    point.y *= yMultiplyFactor
                }
            }

            historyPath.generatePath()
        }

        // Adapt drawn points
        for (point in mPoints) {
            point.x *= xMultiplyFactor
            point.y *= yMultiplyFactor
        }
    }

    interface DrawCreatorListener {
        fun onDrawCreated(draw: Bitmap?)

        fun onDrawCreationError()
    }

    private inner class TakeScreenShotAsyncTask(private val mListener: DrawCreatorListener?) : AsyncTask<Void, Void, Void>() {
        private var mWidth: Int = 0
        private var mHeight: Int = 0
        private var mCanvas: Canvas? = null
        private var mBitmap: Bitmap? = null

        override fun onPreExecute() {
            super.onPreExecute()
            mWidth = width
            mHeight = height
        }

        override fun doInBackground(vararg params: Void): Void? {

            try {
                mBitmap = Bitmap.createBitmap(
                    mWidth, mHeight, Bitmap.Config.ARGB_8888
                )
                mCanvas = Canvas(mBitmap!!)
            } catch (e: Exception) {
                e.printStackTrace()
                cancel(true)
            }

            return null
        }

        override fun onCancelled() {
            super.onCancelled()

            mListener?.onDrawCreationError()
        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)

            draw(mCanvas)

            mListener?.onDrawCreated(mBitmap)
        }
    }

    companion object {
        private val TAG = FreeDrawView::class.java!!.getSimpleName()

        private val DEFAULT_STROKE_WIDTH = 4f
        private val DEFAULT_COLOR = Color.BLACK
        private val DEFAULT_ALPHA = 255
    }
}
