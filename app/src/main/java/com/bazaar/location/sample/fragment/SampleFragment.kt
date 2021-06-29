package com.bazaar.location.sample.fragment

import android.app.ProgressDialog
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import bazaar.tech.library.location.base.LocationBaseFragment
import bazaar.tech.library.location.configuration.Configurations.defaultConfiguration
import bazaar.tech.library.location.configuration.LocationConfiguration
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType
import com.bazaar.location.sample.R
import com.bazaar.location.sample.SamplePresenter
import com.bazaar.location.sample.SamplePresenter.SampleView

class SampleFragment : LocationBaseFragment(), SampleView {
    private var progressDialog: ProgressDialog? = null
    private var locationText: TextView? = null
    private var samplePresenter: SamplePresenter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.location_display_layout, container, false)
        locationText = view.findViewById<View>(R.id.locationText) as TextView
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (samplePresenter != null) samplePresenter!!.destroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        samplePresenter = SamplePresenter(this)
        location
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
            progressDialog = ProgressDialog(context)
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