package bazaar.tech.library.location.providers.locationprovider

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.os.Looper
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import bazaar.tech.library.location.constants.RequestCode

class GooglePlayServicesLocationSource(private val context: Context, private val locationRequest: LocationRequest, private val sourceListener: SourceListener) : LocationCallback() {
    private val fusedLocationProviderClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    interface SourceListener : OnSuccessListener<LocationSettingsResponse>, OnFailureListener {
        override fun onSuccess(locationSettingsResponse: LocationSettingsResponse)
        override fun onFailure(exception: Exception)
        fun onLocationResult(locationResult: LocationResult)
        fun onLastKnowLocationTaskReceived(task: Task<Location>)
    }

    fun checkLocationSettings() {
        LocationServices.getSettingsClient(fusedLocationProviderClient.applicationContext)
                .checkLocationSettings(
                        LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                .build()
                )
                .addOnSuccessListener { locationSettingsResponse -> sourceListener.onSuccess(locationSettingsResponse) }
                .addOnFailureListener { exception -> sourceListener.onFailure(exception) }
    }

    @Throws(SendIntentException::class)
    fun startSettingsApiResolutionForResult(resolvable: ResolvableApiException, activity: Activity) {
        resolvable.startResolutionForResult(activity, RequestCode.SETTINGS_API)
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdate() {
        // This method is suited for the foreground use cases
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.myLooper())
    }

    fun removeLocationUpdates(): Task<Void> {
        return fusedLocationProviderClient.removeLocationUpdates(this)
    }

    @SuppressLint("MissingPermission")
    fun requestLastLocation() {
        fusedLocationProviderClient.lastLocation
                .addOnCompleteListener { task -> sourceListener.onLastKnowLocationTaskReceived(task) }
    }

    override fun onLocationResult(locationResult: LocationResult) {
        sourceListener.onLocationResult(locationResult)
    }

}