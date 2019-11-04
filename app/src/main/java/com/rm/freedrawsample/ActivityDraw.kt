package com.rm.freedrawsample

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.rm.freedrawview.FreeDrawSerializableState
import com.rm.freedrawview.FreeDrawView
import com.rm.freedrawview.PathDrawnListener
import com.rm.freedrawview.PathRedoUndoCountChangeListener

class ActivityDraw : AppCompatActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener, PathRedoUndoCountChangeListener,
    FreeDrawView.DrawCreatorListener, PathDrawnListener {

    private var mRoot: LinearLayout? = null
    private var mFreeDrawView: FreeDrawView? = null
    private var mSideView: View? = null
    private var mBtnRandomColor: Button? = null
    private var mBtnUndo: Button? = null
    private var mBtnRedo: Button? = null
    private var mBtnClearAll: Button? = null
    private var mThicknessBar: SeekBar? = null
    private var mAlphaBar: SeekBar? = null
    private var mTxtRedoCount: TextView? = null
    private var mTxtUndoCount: TextView? = null
    private var mProgressBar: ProgressBar? = null

    private var mImgScreen: ImageView? = null
    private var mMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)

        mRoot = findViewById<View>(R.id.root) as LinearLayout

        mImgScreen = findViewById<View>(R.id.img_screen) as ImageView

        mTxtRedoCount = findViewById<View>(R.id.txt_redo_count) as TextView
        mTxtUndoCount = findViewById<View>(R.id.txt_undo_count) as TextView

        mProgressBar = findViewById<View>(R.id.progress) as ProgressBar

        mFreeDrawView = findViewById<View>(R.id.free_draw_view) as FreeDrawView
        mFreeDrawView!!.setOnPathDrawnListener(this)
        mFreeDrawView!!.setPathRedoUndoCountChangeListener(this)

        mSideView = findViewById(R.id.side_view)
        mBtnRandomColor = findViewById<View>(R.id.btn_color) as Button
        mBtnUndo = findViewById<View>(R.id.btn_undo) as Button
        mBtnRedo = findViewById<View>(R.id.btn_redo) as Button
        mBtnClearAll = findViewById<View>(R.id.btn_clear_all) as Button
        mAlphaBar = findViewById<View>(R.id.slider_alpha) as SeekBar
        mThicknessBar = findViewById<View>(R.id.slider_thickness) as SeekBar

        mAlphaBar!!.setOnSeekBarChangeListener(null)
        mThicknessBar!!.setOnSeekBarChangeListener(null)

        mBtnRandomColor!!.setOnClickListener(this)
        mBtnUndo!!.setOnClickListener(this)
        mBtnRedo!!.setOnClickListener(this)
        mBtnClearAll!!.setOnClickListener(this)

        if (savedInstanceState == null) {

            showLoadingSpinner()

            // Restore the previous saved state
            FileHelper.getSavedStoreFromFile(this,
                object : FileHelper.StateExtractorInterface {
                    override fun onStateExtracted(state: FreeDrawSerializableState) {
                        mFreeDrawView!!.restoreStateFromSerializable(state)

                        hideLoadingSpinner()
                    }

                    override fun onStateExtractionError() {
                        hideLoadingSpinner()
                    }
                })
        }

        mAlphaBar!!.max = (ALPHA_MAX - ALPHA_MIN) / ALPHA_STEP
        val alphaProgress = (mFreeDrawView!!.paintAlpha - ALPHA_MIN) / ALPHA_STEP
        mAlphaBar!!.progress = alphaProgress
        mAlphaBar!!.setOnSeekBarChangeListener(this)

        mThicknessBar!!.max = (THICKNESS_MAX - THICKNESS_MIN) / THICKNESS_STEP
        val thicknessProgress = ((mFreeDrawView!!.paintWidth - THICKNESS_MIN) / THICKNESS_STEP).toInt()
        mThicknessBar!!.progress = thicknessProgress
        mThicknessBar!!.setOnSeekBarChangeListener(this)
        mSideView!!.setBackgroundColor(mFreeDrawView!!.paintColor)
    }

    private fun showLoadingSpinner() {

        TransitionManager.beginDelayedTransition(mRoot!!)
        mProgressBar!!.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {

        mProgressBar!!.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main, menu)

        mMenu = menu

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.menu_screen) {
            takeAndShowScreenshot()
            return true
        }

        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        if (item.itemId == R.id.menu_delete) {
            mFreeDrawView!!.clearDrawAndHistory()
            FileHelper.deleteSavedStateFile(this)
        }

        if (item.itemId == R.id.menu_github) {
            IntentHelper.openUrl(this, getString(R.string.github_url))
        }

        return super.onOptionsItemSelected(item)
    }

    private fun takeAndShowScreenshot() {

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mFreeDrawView!!.getDrawScreenshot(this)
    }

    override fun onPause() {
        super.onPause()

        FileHelper.saveStateIntoFile(this, mFreeDrawView!!.currentViewStateAsSerializable, null)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mSideView!!.setBackgroundColor(mFreeDrawView!!.paintColor)
    }

    private fun changeColor() {
        val color = ColorHelper.getRandomMaterialColor(this)

        mFreeDrawView!!.paintColor = color

        mSideView!!.setBackgroundColor(mFreeDrawView!!.paintColor)
    }

    override fun onClick(v: View) {
        val id = v.id

        if (id == mBtnRandomColor!!.id) {
            changeColor()
        }

        if (id == mBtnUndo!!.id) {
            mFreeDrawView!!.undoLast()
        }

        if (id == mBtnRedo!!.id) {
            mFreeDrawView!!.redoLast()
        }

        if (id == mBtnClearAll!!.id) {
            mFreeDrawView!!.undoAll()
        }
    }

    // SliderListener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (seekBar.id == mThicknessBar!!.id) {
            mFreeDrawView!!.setPaintWidthPx((THICKNESS_MIN + progress * THICKNESS_STEP).toFloat())
        } else {
            mFreeDrawView!!.paintAlpha = ALPHA_MIN + progress * ALPHA_STEP
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onBackPressed() {
        if (mImgScreen!!.visibility == View.VISIBLE) {
            mMenu!!.findItem(R.id.menu_screen).isVisible = true
            mMenu!!.findItem(R.id.menu_delete).isVisible = true
            mImgScreen!!.setImageBitmap(null)
            mImgScreen!!.visibility = View.GONE

            mFreeDrawView!!.visibility = View.VISIBLE
            mSideView!!.visibility = View.VISIBLE

            supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        } else {
            super.onBackPressed()
        }
    }

    // PathRedoUndoCountChangeListener.
    override fun onUndoCountChanged(undoCount: Int) {
        mTxtUndoCount!!.text = undoCount.toString()
    }

    override fun onRedoCountChanged(redoCount: Int) {
        mTxtRedoCount!!.text = redoCount.toString()
    }

    // PathDrawnListener
    override fun onNewPathDrawn() {
        // The user has finished drawing a path
    }

    override fun onPathStart() {
        // The user has started drawing a path
    }


    // DrawCreatorListener
    override fun onDrawCreated(draw: Bitmap?) {
        mSideView!!.visibility = View.GONE
        mFreeDrawView!!.visibility = View.GONE

        mMenu!!.findItem(R.id.menu_screen).isVisible = false
        mMenu!!.findItem(R.id.menu_delete).isVisible = false

        mImgScreen!!.visibility = View.VISIBLE

        mImgScreen!!.setImageBitmap(draw)
    }

    override fun onDrawCreationError() {
        Toast.makeText(this, "Error, cannot create bitmap", Toast.LENGTH_SHORT).show()
    }

    companion object {

        private val TAG = ActivityDraw::class.java.simpleName

        private val THICKNESS_STEP = 2
        private val THICKNESS_MAX = 80
        private val THICKNESS_MIN = 15

        private val ALPHA_STEP = 1
        private val ALPHA_MAX = 255
        private val ALPHA_MIN = 0
    }
}
