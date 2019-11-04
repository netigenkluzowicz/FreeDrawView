package com.rm.freedrawsample

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Created by Riccardo on 30/05/2017.
 */

internal object IntentHelper {

    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, R.string.error_opening_url, Toast.LENGTH_LONG).show()
        }

    }
}
