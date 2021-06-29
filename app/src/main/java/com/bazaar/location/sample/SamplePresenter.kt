package com.bazaar.location.sample

import android.location.Location
import android.text.TextUtils
import bazaar.tech.library.location.constants.FailType
import bazaar.tech.library.location.constants.ProcessType

class SamplePresenter(private var sampleView: SampleView?) {
    fun destroy() {
        sampleView = null
    }

    fun onLocationChanged(location: Location?) {
        sampleView!!.dismissProgress()
        setText(location)
    }

    fun onLocationFailed(@FailType failType: Int) {
        sampleView!!.dismissProgress()
        when (failType) {
            FailType.TIMEOUT -> {
                sampleView!!.text = "Couldn't get location, and timeout!"
            }
            FailType.PERMISSION_DENIED -> {
                sampleView!!.text = "Couldn't get location, because user didn't give permission!"
            }
            FailType.NETWORK_NOT_AVAILABLE -> {
                sampleView!!.text = "Couldn't get location, because network is not accessible!"
            }
            FailType.GOOGLE_PLAY_SERVICES_NOT_AVAILABLE -> {
                sampleView!!.text = "Couldn't get location, because Google Play Services not available!"
            }
            FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DIALOG -> {
                sampleView!!.text = "Couldn't display settingsApi dialog!"
            }
            FailType.GOOGLE_PLAY_SERVICES_SETTINGS_DENIED -> {
                sampleView!!.text = "Couldn't get location, because user didn't activate providers via settingsApi!"
            }
            FailType.DEFAULT_CONFIGURATION_NOT_FOUND -> {
                sampleView!!.text = "Couldn't get location, because user default default configuration not found"
            }
            FailType.GOOGLE_PLAY_CONFIGURATION_NOT_FOUND -> {
                sampleView!!.text = "Couldn't get location, because user default google configuration not found"
            }
            FailType.VIEW_DETACHED -> {
                sampleView!!.text = "Couldn't get location, because in the process view was detached!"
            }
            FailType.VIEW_NOT_REQUIRED_TYPE -> {
                sampleView!!.text = ("Couldn't get location, "
                        + "because view wasn't sufficient enough to fulfill given configuration!")
            }
            FailType.UNKNOWN -> {
                sampleView!!.text = "Ops! Something went wrong!"
            }
        }
    }

    fun onProcessTypeChanged(@ProcessType newProcess: Int) {
        when (newProcess) {
            ProcessType.GETTING_LOCATION_FROM_GOOGLE_PLAY_SERVICES -> {
                sampleView!!.updateProgress("Getting Location from Google Play Services...")
            }
            ProcessType.GETTING_LOCATION_FROM_GPS_PROVIDER -> {
                sampleView!!.updateProgress("Getting Location from GPS...")
            }
            ProcessType.GETTING_LOCATION_FROM_NETWORK_PROVIDER -> {
                sampleView!!.updateProgress("Getting Location from Network...")
            }
            ProcessType.ASKING_PERMISSIONS, ProcessType.GETTING_LOCATION_FROM_CUSTOM_PROVIDER -> {
            }
        }
    }

    private fun setText(location: Location?) {
        val appendValue = """
            ${location!!.latitude}, ${location.longitude}
            
            """.trimIndent()
        val newValue: String
        val current: CharSequence = sampleView!!.text!!
        newValue = if (!TextUtils.isEmpty(current)) {
            current.toString() + appendValue
        } else {
            appendValue
        }
        sampleView!!.text = newValue
    }

    interface SampleView {
        var text: String?
        fun updateProgress(text: String?)
        fun dismissProgress()
    }
}