package com.bazaar.location.sample.service

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import com.bazaar.location.sample.R
import com.bazaar.location.sample.SamplePresenter
import com.bazaar.location.sample.SamplePresenter.SampleView

class SampleServiceActivity : AppCompatActivity(), SampleView {
    private var intentFilter: IntentFilter? = null
    private var samplePresenter: SamplePresenter? = null
    private var progressDialog: ProgressDialog? = null
    private var locationText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_display_layout)
        locationText = findViewById<View>(R.id.locationText) as TextView
        samplePresenter = SamplePresenter(this)
        displayProgress()
        startService(Intent(this, SampleService::class.java))
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(broadcastReceiver, getIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(broadcastReceiver)
    }

    override var text: String?
        get() = locationText!!.text.toString()
        set(text) {
            locationText!!.text = text
        }

    override fun updateProgress(text: String?) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.setMessage(text)
        }
    }

    override fun dismissProgress() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    }

    private fun displayProgress() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog!!.window!!.addFlags(Window.FEATURE_NO_TITLE)
            progressDialog!!.setMessage("Getting location...")
        }
        if (!progressDialog!!.isShowing) {
            progressDialog!!.show()
        }
    }

    private fun getIntentFilter(): IntentFilter {
        if (intentFilter == null) {
            intentFilter = IntentFilter()
            intentFilter!!.addAction(SampleService.Companion.ACTION_LOCATION_CHANGED)
            intentFilter!!.addAction(SampleService.Companion.ACTION_LOCATION_FAILED)
            intentFilter!!.addAction(SampleService.Companion.ACTION_PROCESS_CHANGED)
        }
        return intentFilter!!
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == SampleService.Companion.ACTION_LOCATION_CHANGED) {
                samplePresenter!!.onLocationChanged(intent.getParcelableExtra<Parcelable>(SampleService.Companion.EXTRA_LOCATION) as Location)
            } else if (action == SampleService.Companion.ACTION_LOCATION_FAILED) {
                samplePresenter!!.onLocationFailed(intent.getIntExtra(SampleService.Companion.EXTRA_FAIL_TYPE, FailType.UNKNOWN))
            } else if (action == SampleService.Companion.ACTION_PROCESS_CHANGED) {
                samplePresenter!!.onProcessTypeChanged(intent.getIntExtra(SampleService.Companion.EXTRA_PROCESS_TYPE,
                        ProcessType.GETTING_LOCATION_FROM_CUSTOM_PROVIDER))
            }
        }
    }
}