package com.example.demo_map


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.example.demo_map.R
import com.example.demo_map.LocationPermissionHelper
import java.lang.ref.WeakReference

/**
 * Tracks the user location on screen, simulates a navigation session.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)
        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {

        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
////import com.mapbox.maps.Style
////import com.mapbox.maps.plugin.animation.MapAnimationOptions
////import com.mapbox.maps.plugin.animation.camera
////import com.mapbox.maps.plugin.locationcomponent.location
////import com.mapbox.navigation.base.options.NavigationOptions
////import com.mapbox.navigation.core.MapboxNavigation
////import com.mapbox.navigation.core.MapboxNavigationProvider
////import com.mapbox.navigation.core.trip.session.LocationMatcherResult
////import com.mapbox.navigation.core.trip.session.LocationObserver
////import com.mapbox.navigation.examples.R
////import com.mapbox.navigation.examples.databinding.MapboxActivityUserCurrentLocationBinding
////import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
////
//class MainActivity : AppCompatActivity() {
//
//    private val navigationLocationProvider = NavigationLocationProvider()
//
//    /**
//     * Gets notified with location updates.
//     *
//     * Exposes raw updates coming directly from the location services
//     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
//     */
//    private val locationObserver = object : LocationObserver {
//        /**
//         * Invoked as soon as the [Location] is available.
//         */
//        override fun onNewRawLocation(rawLocation: Location) {
//            // Not implemented in this example. However, if you want you can also
//            // use this callback to get location updates, but as the name suggests
//            // these are raw location updates which are usually noisy.
//        }
//
//        /**
//         * Provides the best possible location update, snapped to the route or
//         * map-matched to the road if possible.
//         */
//        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
//            val enhancedLocation = locationMatcherResult.enhancedLocation
//            navigationLocationProvider.changePosition(
//                enhancedLocation,
//                locationMatcherResult.keyPoints,
//            )
//            // Invoke this method to move the camera to your current location.
//            updateCamera(enhancedLocation)
//        }
//    }
//
//    /**
//     * Mapbox Maps entry point obtained from the [MapView].
//     * You need to get a new reference to this object whenever the [MapView] is recreated.
//     */
//    private lateinit var mapboxMap: MapboxMap
//
//    /**
//     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
//     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
//     */
//    private lateinit var mapboxNavigation: MapboxNavigation
//
//    /**
//     * Bindings to the example layout.
//     */
//    private lateinit var binding: MapboxActivityUserCurrentLocationBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = MapboxActivityUserCurrentLocationBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        mapboxMap = binding.mapView.getMapboxMap()
//        // Instantiate the location component which is the key component to fetch location updates.
//        binding.mapView.location.apply {
//            setLocationProvider(navigationLocationProvider)
//
//            // Uncomment this block of code if you want to see a circular puck with arrow.
//            /*
//            locationPuck = LocationPuck2D(
//                bearingImage = ContextCompat.getDrawable(
//                    this@ShowCurrentLocationActivity,
//                    R.drawable.mapbox_navigation_puck_icon
//                )
//            )
//            */
//
//            // When true, the blue circular puck is shown on the map. If set to false, user
//            // location in the form of puck will not be shown on the map.
//            enabled = true
//        }
//
//        init()
//    }
//
//    private fun init() {
//        initStyle()
//        initNavigation()
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun initNavigation() {
//        mapboxNavigation = MapboxNavigation(
//            NavigationOptions.Builder(this)
//                .accessToken(getString(R.string.mapbox_access_token))
//                .build()
//        ).apply {
//            // This is important to call as the [LocationProvider] will only start sending
//            // location updates when the trip session has started.
//            startTripSession()
//            // Register the location observer to listen to location updates received from the
//            // location provider
//            registerLocationObserver(locationObserver)
//        }
//    }
//
//    private fun initStyle() {
//        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
//    }
//
//    private fun updateCamera(location: Location) {
//        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
//        binding.mapView.camera.easeTo(
//            CameraOptions.Builder()
//                // Centers the camera to the lng/lat specified.
//                .center(Point.fromLngLat(location.longitude, location.latitude))
//                // specifies the zoom value. Increase or decrease to zoom in or zoom out
//                .zoom(12.0)
//                // specify frame of reference from the center.
//                .padding(EdgeInsets(500.0, 0.0, 0.0, 0.0))
//                .build(),
//            mapAnimationOptions
//        )
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
//        mapboxNavigation.stopTripSession()
//        // make sure to unregister the observer you have registered.
//        mapboxNavigation.unregisterLocationObserver(locationObserver)
//    }
//}
////
////
////    private lateinit var mapView: MapView
////    private lateinit var map: MapboxMap
////    private lateinit var permissionManager: PermissionsManager
////    private lateinit var originLocation: Location
////
////    private var locationEngine: LocationEngine? = null
////    private var locationLayerPlugin: LocationLayerPlugin? = null
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_main)
////        Mapbox.getInstance(applicationContext, getString(R.string.mapbox_access_token))
////        mapView = findViewById(R.id.mapView)
////        mapView.onCreate(savedInstanceState)
////        mapView.getMapAsync {mapboxMap ->
////            map = mapboxMap
////            enableLocation()
////        }
////    }
////
////    private fun enableLocation() {
////        if(PermissionsManager.areLocationPermissionsGranted(this)) {
////            initializeLocationEngine()
////            initializeLocationLayer()
////        }
////        else {
////            permissionManager = PermissionsManager(this)
////            permissionManager.requestLocationPermissions(this)
////        }
////    }
////
////    @SuppressWarnings("MissingPermission")
////    private fun initializeLocationEngine() {
////        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
////        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
////        locationEngine?.activate()
////
////        val lastLocation = locationEngine?.lastLocation
////        if(lastLocation != null) {
////            originLocation = lastLocation
////            setCameraPosition(lastLocation)
////        } else {
////            locationEngine?.addLocationEngineListener(this)
////        }
////    }
////    @SuppressWarnings("MissingPermission")
////    private fun initializeLocationLayer() {
////        locationLayerPlugin = LocationLayerPlugin(mapView, map, locationEngine)
////        locationLayerPlugin?.setLocationLayerEnabled(true)
////        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
////        locationLayerPlugin?.renderMode = RenderMode.NORMAL
////    }
////
////    private fun setCameraPosition(location: Location) {
////        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 13.0))
////    }
////
////    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>) {
////        // explain why u nedd to give permis
////    }
////
////    override fun onPermissionResult(granted: Boolean) {
////        if(granted) {
////            enableLocation()
////        }
////    }
////
////    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
////        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
////    }
////
////    override fun onLocationChanged(location: Location?) {
////        location?.let {
////            originLocation = location
////            setCameraPosition(location)
////        }
////    }
////    @SuppressWarnings("MissingPermission")
////    override fun onConnected() {
////        locationEngine?.requestLocationUpdates()
////    }
////    @SuppressWarnings("MissingPermission")
////    override fun onStart() {
////        super.onStart()
////        if (PermissionsManager.areLocationPermissionsGranted(this)) {
////            locationEngine?.requestLocationUpdates()
////            locationLayerPlugin?.onStart()
////        }
////        mapView.onStart()
////    }
////
////    override fun onResume() {
////        super.onResume()
////        mapView.onResume()
////    }
////
////    override fun onPause() {
////        super.onPause()
////        mapView.onPause()
////    }
////
////    override fun onStop() {
////        super.onStop()
////        mapView.onStop()
////    }
////    override fun onDestroy() {
////        super.onDestroy()
////        mapView.onDestroy()
////        locationEngine?.deactivate()
////    }
////
////    override fun onSaveInstanceState(outState: Bundle) {
////        super.onSaveInstanceState(outState)
////        if(outState != null) {
////            mapView.onSaveInstanceState(outState)
////        }
////    }
////
////    override fun onLowMemory() {
////        super.onLowMemory()
////        mapView.onLowMemory()
////    }
////}