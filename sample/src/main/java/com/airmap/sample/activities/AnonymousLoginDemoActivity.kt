package com.airmap.sample.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.widget.TextView

import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.sample.R

import java.util.UUID

import timber.log.Timber

class AnonymousLoginDemoActivity : AppCompatActivity() {

    companion object {
        fun Context.AnonymousLoginIntent() = Intent(this, AnonymousLoginDemoActivity::class.java)
    }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val statusTextView by lazy { findViewById<TextView>(R.id.status_text_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anon_login)

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // check if user is already logged in
        if (AirMap.getUserId().isNullOrEmpty()) {
            statusTextView.text = "Already logged in as:\n\n" + AirMap.getUserId() + "\n\nwith ability to create flights, receive traffic and send telemetry."
        } else {
            // Any unique identifier from the developer for their user (UUID, username, email)
            val userId = UUID.randomUUID().toString()

            AirMap.performAnonymousLogin(userId, object : AirMapCallback<Void>() {
                public override fun onSuccess(response: Void) {
                    Timber.v("Token is: %s", AirMap.getAuthToken())
                    statusTextView.text = "Logged in as:\n\n" + AirMap.getUserId() + "\n\nNow able to create flights, receive traffic and send telemetry."
                }

                public override fun onError(e: AirMapException) {
                    Timber.e(e, e.detailedMessage)
                    statusTextView.text = "Anonymous Login failed"
                }
            })
        }
    }
}
