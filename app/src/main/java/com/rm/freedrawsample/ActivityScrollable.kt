package com.rm.freedrawsample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.rm.freedrawview.FreeDrawView

/**
 * Created by Riccardo on 01/12/16.
 */

class ActivityScrollable : AppCompatActivity() {

    private var mDrawSignature: FreeDrawView? = null
    private var mDrawSmile: FreeDrawView? = null
    private var mDrawSad: FreeDrawView? = null
    private var mDrawLeaf: FreeDrawView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scrollable)

        mDrawSignature = findViewById<FreeDrawView>(R.id.draw_signature)
        mDrawSmile = findViewById<FreeDrawView>(R.id.draw_smile)
        mDrawSad = findViewById<FreeDrawView>(R.id.draw_sad)
        mDrawLeaf = findViewById<FreeDrawView>(R.id.draw_leaf)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.menu_main_lite, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        if (item.itemId == R.id.menu_github) {
            IntentHelper.openUrl(this, getString(R.string.github_url))
        }

        if (item.itemId == R.id.menu_delete) {
            mDrawSignature!!.clearDrawAndHistory()
            mDrawSmile!!.clearDrawAndHistory()
            mDrawSad!!.clearDrawAndHistory()
            mDrawLeaf!!.clearDrawAndHistory()
        }

        return super.onOptionsItemSelected(item)
    }
}
