package com.rm.freedrawsample

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button

import androidx.appcompat.app.AppCompatActivity

/**
 * Created by Riccardo on 01/12/16.
 */

class ActivityChooser : AppCompatActivity(), View.OnClickListener {

    private var mBtnDraw: Button? = null
    private var mBtnScrollable: Button? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chooser)

        mBtnDraw = findViewById<View>(R.id.draw_sample) as Button
        mBtnScrollable = findViewById<View>(R.id.scrollable_sample) as Button

        mBtnDraw!!.setOnClickListener(this)
        mBtnScrollable!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id

        if (id == mBtnDraw!!.id) {
            startActivity(Intent(this, ActivityDraw::class.java))
        }

        if (id == mBtnScrollable!!.id) {
            startActivity(Intent(this, ActivityScrollable::class.java))
        }
    }
}
