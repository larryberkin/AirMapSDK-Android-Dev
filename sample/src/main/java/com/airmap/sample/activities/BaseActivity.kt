package com.airmap.sample.activities

import android.app.Activity
import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

abstract class BaseActivity : AppCompatActivity() {

    fun Activity.isActive() = !isFinishing && !isDestroyed

    fun Activity.showErrorDialog(message: String) {
        val builder = AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)

        if (isActive()) {
            builder.show()
        }
    }

    fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    fun Context.toast(@StringRes resId: Int) = toast(getString(resId))
}
