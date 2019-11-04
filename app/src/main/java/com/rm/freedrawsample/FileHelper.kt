package com.rm.freedrawsample

import android.content.Context
import android.os.Handler
import android.os.Looper

import com.rm.freedrawview.FreeDrawSerializableState

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Created by Riccardo on 23/05/2017.
 */

object FileHelper {

    private val FILE_NAME = "draw_state.ser"

    fun saveStateIntoFile(
        context: Context?, state: FreeDrawSerializableState?,
        listener: StateSaveInterface?
    ) {

        if (context != null && state != null) {

            Thread(StateSaveRunnable(context, listener, state)).start()
        } else {

            listener?.onStateSaveError()
        }
    }

    fun getSavedStoreFromFile(
        context: Context?, listener: StateExtractorInterface?
    ) {

        if (context != null) {

            Thread(StateExtractorRunnable(context, listener)).start()
        } else {

            listener?.onStateExtractionError()
        }
    }

    fun deleteSavedStateFile(context: Context?) {

        if (context != null) {

            var fos: FileOutputStream? = null
            try {
                fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
                val os = ObjectOutputStream(fos)
                os.close()
                fos!!.close()
            } catch (e: Exception) {
                e.printStackTrace()

                if (fos != null) {

                    try {
                        fos.close()
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                }
            }

        }
    }


    // Runnable that extracts the FreeDrawSerializableState from a file
    private class StateExtractorRunnable(private val mContext: Context, private val mListener: StateExtractorInterface?) : Runnable {

        override fun run() {
            var fis: FileInputStream? = null
            try {

                fis = mContext.openFileInput(FILE_NAME)
                val `is` = ObjectInputStream(fis)

                val state = `is`.readObject() as FreeDrawSerializableState

                fis!!.close()
                `is`.close()

                if (mListener != null) {
                    runOnUiThread(Runnable { mListener.onStateExtracted(state) })
                }
            } catch (e: Exception) {
                e.printStackTrace()

                if (fis != null) {

                    try {
                        fis.close()
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                }

                if (mListener != null) {

                    runOnUiThread(Runnable { mListener.onStateExtractionError() })
                }
            }

        }
    }


    // Runnable that save a FreeDrawSerializableState inside a file
    private class StateSaveRunnable(
        private val mContext: Context, private val mListener: StateSaveInterface?, private val mState: FreeDrawSerializableState
    ) : Runnable {

        override fun run() {
            var fos: FileOutputStream? = null
            try {
                fos = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
                val os = ObjectOutputStream(fos)
                os.writeObject(mState)
                os.flush()
                fos!!.flush()
                os.close()
                fos.close()

                if (mListener != null) {
                    runOnUiThread(Runnable { mListener.onStateSaved() })
                }
            } catch (e: Exception) {
                e.printStackTrace()

                if (fos != null) {

                    try {
                        fos.close()
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                }

                runOnUiThread(Runnable { mListener!!.onStateSaveError() })
            }

        }
    }


    // Listener for file creation
    interface StateSaveInterface {
        fun onStateSaved()

        fun onStateSaveError()
    }

    // Listener for file data extraction
    interface StateExtractorInterface {
        fun onStateExtracted(state: FreeDrawSerializableState)

        fun onStateExtractionError()
    }


    // Shortcut method to run on uiThread a runnable
    private fun runOnUiThread(runnable: Runnable) {

        Handler(Looper.getMainLooper()).post(runnable)
    }
}
