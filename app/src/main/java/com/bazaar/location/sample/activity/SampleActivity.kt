package com.bazaar.location.sample.activity

import android.app.ProgressDialog
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import bazaar.tech.library.location.base.LocationBaseActivity
import bazaar.tech.library.location.configuration.Configurations.defaultConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import com.bazaar.location.sample.R
import com.bazaar.location.sample.SamplePresenter
import com.bazaar.location.sample.SamplePresenter.SampleView

class SampleActivity : LocationBaseActivity(), SampleView {
    private var progressDialog: ProgressDialog? = null
    private var locationText: TextView? = null
    private var samplePresenter: SamplePresenter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_display_layout)
        locationText = findViewById<View>(R.id.locationText) as TextView
        samplePresenter = SamplePresenter(this)
        location
    }

    override fun onDestroy() {
        super.onDestroy()
        samplePresenter!!.destroy()
    }

    override val locationConfiguration: LocationConfiguration
        get() = defaultConfiguration("Gimme the permission!", "Would you mind to turn GPS on?")

    override fun onLocationChanged(location: Location?) {
        samplePresenter!!.onLocationChanged(location)
    }

    override fun onLocationFailed(@FailType failType: Int) {
        samplePresenter!!.onLocationFailed(failType)
    }

    override fun onProcessTypeChanged(@ProcessType processType: Int) {
        samplePresenter!!.onProcessTypeChanged(processType)
    }

    override fun onResume() {
        super.onResume()
        if (locationManager.isWaitingForLocation
                && !locationManager.isAnyDialogShowing) {
            displayProgress()
        }
    }

    override fun onPause() {
        super.onPause()
        dismissProgress()
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
}