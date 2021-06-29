# AndroidLocationTracker

To get location on Android Devices, there are 'some' steps you need to go through!
What are those? Let's see...

<ul>
<li>Check whether the application has required permission or not</li>
<li>If not, ask runtime permissions from user</li>
<li>Check whether user granted the permissions or not</li>
</ul>

Let's assume we got the permission, now what?
We have this cool Google Play Services optimised location provider [FusedLocationProviderClient][1] which provides a common location pool that any application use this api can retrieve location in which interval they require. It reduces battery usage, and decreases waiting time (most of the time) for location.
YES, we want that! Right?

<ul>
<li>Check whether Google Play Services is available on device</li>
<li>If not, see if user can handle this problem?</li>
<li>If so, ask user to do it</li>
<li>Then, check again to see if user actually did it or not</li>
<li>If user did actually handle it, then start location update request</li>
<li>Of course, handle GoogleApiClient connection issues first</li>
<li>And have a fallback plan, if you're not able to get location in certain time period</li>
</ul>

Ouu and yes, this new even cooler SettingsApi, that user doesn't need to go to settings to activate GPS or Wifi. Isn't that cool, let's implement that too!

<ul>
<li>Call SettingsApi to adapt device settings up to your location requirement</li>
<li>Wait for the result and check your options</li>
<li>Is current settings are enough to get required location?</li>
<li>Or can you ask user to adapt them?</li>
<li>Maybe it is not even possible to use it.</li>
<li>Let's assume we asked user to adapt, but he/she didn't want to do it.</li>
</ul>

Well whatever we tried to optimise, right? But till now, all depends on GooglePlayServices what happens if user's device doesn't have GooglePlayServices, or user didn't want to handle GooglePlayServices issue or user did everything and waited long enough but somehow GooglePlayServices weren't able to return any location. What now?
Surely we still have good old times GPS and Network Providers, right? Let's switch to them and see what we need to do!

<ul>
<li>Check whether GPS Provider is enabled or not</li>
<li>If it is not, ask user to enable it</li>
<li>Check again whether user actually did enable it or not</li>
<li>If it is enabled, start location update request</li>
<li>But switch to network if GPS is not retrieving location after waiting long enough</li>
<li>Check whether Network Provider is enabled or not</li>
<li>If it is, start location update request</li>
<li>If none of these work, then fail</li>
</ul>

All of these steps, just to retrieve user's current location. And in every application, you need to reconsider what you did and what you need to add for this time.

<b>With this library you just need to provide a Configuration object with your requirements, and you will receive a location or a fail reason with all the stuff are described above handled.</b>
 
This library requires quite a lot of lifecycle information to handle all the steps between onCreate - onResume - onPause - onDestroy - onActivityResult - onRequestPermissionsResult.
You can simply use one of [LocationBaseActivity][2], [LocationBaseFragment][3], [LocationBaseService][4] or you can manually call those methods as required.

[See the sample application][5] for detailed usage!

## Configuration

All those settings below are optional. Use only those you really want to customize. Please do not copy-paste this configuration directly. If you want to use pre-defined configurations, see [Configurations][6].

```java 
LocationConfiguration awesomeConfiguration = new LocationConfiguration.Builder()
    .keepTracking(false)
    .askForPermission(new PermissionConfiguration.Builder()
        .permissionProvider(new YourCustomPermissionProvider())
        .rationaleMessage("Gimme the permission!")
        .rationaleDialogProvider(new YourCustomDialogProvider())
        .requiredPermissions(new String[] { permission.ACCESS_FINE_LOCATION })
        .build())
    .useGooglePlayServices(new GooglePlayServicesConfiguration.Builder()
        .locationRequest(YOUR_CUSTOM_LOCATION_REQUEST_OBJECT)
        .fallbackToDefault(true)
        .askForGooglePlayServices(false)
        .askForSettingsApi(true)
        .failOnConnectionSuspended(true)
        .failOnSettingsApiSuspended(false)
        .ignoreLastKnowLocation(false)
        .setWaitPeriod(20 * 1000)
        .build())
    .useDefaultProviders(new DefaultProviderConfiguration.Builder()
        .requiredTimeInterval(5 * 60 * 1000)
        .requiredDistanceInterval(0)
        .acceptableAccuracy(5.0f)
        .acceptableTimePeriod(5 * 60 * 1000)
        .gpsMessage("Turn on GPS?")
        .gpsDialogProvider(new YourCustomDialogProvider())
        .setWaitPeriod(ProviderType.GPS, 20 * 1000)
        .setWaitPeriod(ProviderType.NETWORK, 20 * 1000)
        .build())
    .build();
```

Library is modular enough to let you create your own way for Permission request, Dialog display, or even a whole LocationProvider process. (Custom LocationProvider implementation is described below in LocationManager section)

You can create your own [PermissionProvider][7] implementation and simply set it to [PermissionConfiguration][8], and then library will use your implementation. Your custom PermissionProvider implementation will receive your configuration requirements from PermissionConfiguration object once it's built. If you don't specify any PermissionProvider to PermissionConfiguration [DefaultPermissionProvider][9] will be used. If you don't specify PermissionConfiguration to LocationConfiguration [StubPermissionProvider][10] will be used instead.

You can create your own [DialogProvider][11] implementation to display `rationale message` or `gps request message` to user, and simply set them to required configuration objects. If you don't specify any [SimpleMessageDialogProvider][12] will be used as default.

## LocationManager

Ok, we have our configuration object up to requirements, now we need a manager configured with it.

```java
// LocationManager MUST be initialized with Application context in order to prevent MemoryLeaks
LocationManager awesomeLocationManager = new LocationManager.Builder(getApplicationContext())
    .activity(activityInstance) // Only required to ask permission and/or GoogleApi - SettingsApi
    .fragment(fragmentInstance) // Only required to ask permission and/or GoogleApi - SettingsApi
    .configuration(awesomeConfiguration)
    .locationProvider(new YourCustomLocationProvider())
    .notify(new LocationListener() { ... })
    .build();
```

LocationManager doesn't keep strong reference of your activity **OR** fragment in order not to cause any memory leak. They are required to ask for permission and/or GoogleApi - SettingsApi in case they need to be resolved.

You can create your own [LocationProvider][13] implementation and ask library to use it. If you don't set any, library will use [DispatcherLocationProvider][14], which will do all the stuff is described above, as default.

Enough, gimme the location now!

```java
awesomeLocationManager.get();
```

Done! Enjoy :)

## Logging

Library has a lot of log implemented, in order to make tracking the process easy, you can simply enable or disable it.
It is highly recommended to disable in release mode.

```java 
LocationManager.enableLog(false);
```

For a more fine tuned logging, you can provide a custom Logger implementation to filter and delegate logs as you need it.

```java
Logger myCustomLoggerImplementation = new MyCustomLoggerImplementation();
LocationManager.setLogger(myCustomLoggerImplementation);
```

## Restrictions
If you are using LocationManager in a
- Fragment, you need to redirect your `onActivityResult` to fragment manually, because GooglePlayServices Api and SettingsApi calls `startActivityForResult` from activity. For the sample implementation please see [SampleFragmentActivity][15].
- Service, you need to have the permission already otherwise library will fail immediately with PermissionDenied error type. Because runtime permissions can be asked only from a fragment or an activity, not from a context. For the sample implementation please see [SampleService][16].

## AndroidManifest

Library requires 3 permission;
 - 2 of them `ACCESS_NETWORK_STATE` and `INTERNET` are not in `Dangerous Permissions` and they are required in order to use Network Provider. So if your configuration doesn't require them, you don't need to define them, otherwise they need to be defined.
 - The other one is `ACCESS_FINE_LOCATION` and it is marked as `Dangerous Permissions`, so you need to define it in Manifest and library will ask runtime permission for that if the application is running on Android M or higher OS  version. If you don't specify in Manifest, library will fail immediately with PermissionDenied when location is required.

```html
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

You might also need to consider information below from [the location guide page.][17]

<blockquote>
<b>Caution:</b> If your app targets Android 5.0 (API level 21) or higher, you must declare that your app uses the android.hardware.location.network or android.hardware.location.gps hardware feature in the manifest file, depending on whether your app receives location updates from NETWORK_PROVIDER or from GPS_PROVIDER. If your app receives location information from either of these location provider sources, you need to declare that the app uses these hardware features in your app manifest. On devices running versions prior to Android 5.0 (API 21), requesting the ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission includes an implied request for location hardware features. However, requesting those permissions does not automatically request location hardware features on Android 5.0 (API level 21) and higher.
</blockquote>

## Download
Add library dependency to your `build.gradle` file:

```groovy
dependencies {    
     implementation 'com.github.hamzaahmedkhan:AndroidLocationTracker:1.0.0'
}
```

[1]: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient
[2]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/base/LocationBaseActivity.kt
[3]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/base/LocationBaseFragment.kt
[4]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/base/LocationBaseService.kt
[5]: https://github.com/hamzaahmedkhan/LocationManager/tree/master/app
[6]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/configuration/Configurations.kt
[7]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/permissionprovider/PermissionProvider.kt
[8]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/configuration/PermissionConfiguration.kt
[9]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/permissionprovider/DefaultPermissionProvider.kt
[10]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/permissionprovider/StubPermissionProvider.kt
[11]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/dialogprovider/DialogProvider.kt
[12]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/dialogprovider/SimpleMessageDialogProvider.kt
[13]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/locationprovider/LocationProvider.kt
[14]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/library/src/main/java/bazaar/tech/library/location/providers/locationprovider/DispatcherLocationProvider.kt
[15]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/app/src/main/java/bazaar/tech/library/location/sample/fragment/SampleFragmentActivity.kt
[16]: https://github.com/hamzaahmedkhan/LocationManager/blob/master/app/src/main/java/bazaar/tech/library/location/sample/service/SampleService.kt
[17]:https://developer.android.com/guide/topics/location/strategies.html