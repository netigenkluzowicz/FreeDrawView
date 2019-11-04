package com.rm.freedrawview

import android.os.Parcel
import android.os.Parcelable

import java.io.Serializable

/**
 * Created by Riccardo Moro on 9/25/2016.
 */

class Point : Parcelable, Serializable {

    var x: Float = 0.toFloat()
    var y: Float = 0.toFloat()

    constructor() {
        y = -1f
        x = y
    }

    override fun toString(): String {
        return "$x : $y - "
    }


    // Parcelable stuff
    private constructor(`in`: Parcel) {
        x = `in`.readFloat()
        y = `in`.readFloat()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeFloat(x)
        dest.writeFloat(y)
    }


    companion object CREATOR : Parcelable.Creator<Point> {
        override fun createFromParcel(parcel: Parcel): Point {
            return Point(parcel)
        }

        override fun newArray(size: Int): Array<Point?> {
            return arrayOfNulls(size)
        }
    }
}
