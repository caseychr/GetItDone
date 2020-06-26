package com.ccasey.getitdone.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ccasey.getitdone.R
import com.ccasey.getitdone.ResourceView
import com.ccasey.getitdone.ResourceViewObserver
import com.ccasey.getitdone.model.Run
import com.ccasey.getitdone.utils.FirebaseUtil
import com.ccasey.getitdone.viewextensions.*
import com.ccasey.getitdone.viewmodel.RunningViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.core.constants.Constants
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMapClickListener
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.maps.Style.OnStyleLoaded
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_running.*
import kotlinx.android.synthetic.main.layout_play.view.*
import kotlinx.android.synthetic.main.layout_quick_start.view.*
import kotlinx.android.synthetic.main.layout_run_metrics.view.*
import kotlinx.android.synthetic.main.layout_run_spinner.view.*
import kotlinx.android.synthetic.main.plan_run_bottom_sheet.*
import kotlinx.android.synthetic.main.plan_run_bottom_sheet.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

const val SIXTY = 60
const val INTERVAL = 1000L

class RunningFragment : Fragment(), PermissionsListener {

    lateinit var viewModel: RunningViewModel

    lateinit var viewLayout: View
    lateinit var sheetBehavior: BottomSheetBehavior<View>
    lateinit var bottomSheet: LinearLayout
    lateinit var countdownTimer: CountDownTimer

    var mDistance: String? = null
    var mDuration: String? = null

    lateinit var mapbox: MapboxMap
    lateinit var mStyle: Style
    lateinit var client: MapboxDirections
    lateinit var currentRoute: DirectionsRoute

    lateinit var mPermissionsManager: PermissionsManager
    lateinit var mLocationComponent: LocationComponent
    lateinit var origin: Point
    lateinit var destination: Point

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Mapbox.getInstance(activity!!.applicationContext, getString(R.string.mapbox_api_key))
        viewLayout = inflater.inflate(R.layout.fragment_running, container, false)
        bottomSheet = viewLayout.findViewById(R.id.run_bottom_sheet)
        return viewLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.title_workout)
        viewModel = ViewModelProvider(this@RunningFragment)[RunningViewModel::class.java].apply {
            runMetricsLiveData.observe(viewLifecycleOwner, ResourceViewObserver(getCurrentRunMetrics))
            durationLeftLiveData.observe(viewLifecycleOwner, ResourceViewObserver(getDurationLeft))
        }
        initMap()
        initBottomSheet()
    }

    private fun initBottomSheet() {
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
        initNumPicker(getString(R.string.tag_distance))
        initTabs()
        startBtn.setOnClickListener {
            startOnClick()
        }
    }

    ////////////////////////////////////// INIT BOTTOM SHEET //////////////////////////////////////

    private fun startOnClick() {
        // Once Start Button is Clicked
        bottomSheet.apply {
            layout_play.apply {
                visibility = View.VISIBLE
                playImg.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
                stopImg.visibility = View.GONE
                stopImgCircle.visibility = View.GONE
                metricDistanceNum.text = getString(R.string.reset_distance)
                metricPaceNum.text = getString(R.string.reset_pace)
                metricTimeNum.text = getString(R.string.reset_time)
            }
            // Execute if going for distance
            if(bottomSheet.tabs.selectedTabPosition == 0) {
                layout_play.tracker.setLongToTextTimer(((layout_run_spinner.primaryNumberPicker.value.toLong() * SIXTY)
                        + layout_run_spinner.secondNumberPicker.value) * INTERVAL)
                layout_play.trackerUnit.text = context.getString(R.string.distance_to_go)
            }
            // Execute if going for time
            if(bottomSheet.tabs.selectedTabPosition == 1) {
                layout_play.tracker.setLongToTextTimer(((layout_run_spinner.primaryNumberPicker.value.toLong() * SIXTY)
                        + layout_run_spinner.secondNumberPicker.value) * INTERVAL)
                initPreWorkoutCountdownTimer((layout_run_spinner.primaryNumberPicker.value.toLong() * SIXTY * SIXTY * INTERVAL)
                        + (layout_run_spinner.secondNumberPicker.value * SIXTY * INTERVAL))
                layout_play.trackerUnit.text = context.getString(R.string.time_to_go)
            }
            // Hide stuff not needed
            bottomSheetTitle.visibility = View.GONE
            tabs.visibility = View.GONE
            layout_run_spinner.visibility = View.GONE
            // Toggle UI and logic for play/pause
            layout_play.playImgCircle.setOnClickListener {
                if(playImg.drawable.constantState!!.equals(resources.getDrawable(R.drawable.ic_pause).constantState)) {
                    //TODO pause 2 timers, save metrics and remain duration to shared prefs
                    val currentRun = Run((layout_play.metricDistanceNum.text).toString().toFloat(), layout_play.metricTimeNum.setTextToLongTimer(),
                        0.0f, System.currentTimeMillis(), emptyList(), emptyList())
                    countdownTimer.endTimer()

                    viewModel.setDurationLeft(layout_play.tracker.setTextToLongTimer())
                    viewModel.setCurrentRunMetrics(currentRun)

                    playImg.setImageDrawable(resources.getDrawable(R.drawable.ic_play_arrow))
                    stopImgCircle.visibility = View.VISIBLE
                    stopImg.visibility = View.VISIBLE
                } else {
                    viewModel.getCurrentRunMetrics()
                    viewModel.getDurationLeft()

                    //TODO move to resourceView
                    playImg.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
                    stopImgCircle.visibility = View.GONE
                    stopImg.visibility = View.GONE
                }
            }
            // Hide Start Button
            startBtn.visibility = View.GONE
        }
        // Stop long click listener logic
        bottomSheet.layout_play.stopImgCircle.setOnLongClickListener {
            resetWorkout()
            //TODO Save run NEED ROOM
            true
        }
    }

    private fun initTabs() {
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_go_for_distance)).setTag(getString(R.string.tag_distance)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_go_for_time)).setTag(getString(R.string.tag_time)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_create_route)).setTag(getString(R.string.tag_route)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_challenge_yourself)).setTag(getString(R.string.tag_challenge)))
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.tag) {
                    getString(R.string.tag_distance) -> {
                        initNumPicker(getString(R.string.tag_distance))
                    }
                    getString(R.string.tag_time) -> {
                        initNumPicker(getString(R.string.tag_time))
                    }
                    getString(R.string.tag_route) -> {
                        initNumPicker(getString(R.string.tag_route))
                    }
                    getString(R.string.tag_challenge) -> {
                        initNumPicker(getString(R.string.tag_challenge))
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun initNumPicker(tagName: String) {
        layout_run_spinner.primaryNumberPicker.minValue = 0
        layout_run_spinner.secondNumberPicker.minValue = 0
        when(tagName) {
            getString(R.string.tag_time) -> {
                layout_run_spinner.apply {
                    primaryNumberPicker.visibility = View.VISIBLE
                    secondNumberPicker.visibility = View.VISIBLE
                    unit.visibility = View.VISIBLE
                    primaryNumberPicker.maxValue = 23
                    decimal.text = context.getString(R.string.hours)
                    secondNumberPicker.maxValue = 59
                    unit.text = context.getString(R.string.minutes)
                }
            }
            getString(R.string.tag_distance) -> {
                layout_run_spinner.apply {
                    primaryNumberPicker.visibility = View.VISIBLE
                    secondNumberPicker.visibility = View.VISIBLE
                    unit.visibility = View.VISIBLE
                    primaryNumberPicker.maxValue = 100
                    decimal.text = "."
                    secondNumberPicker.maxValue = 9
                    unit.text = context.getString(R.string.miles)
                }
            }
            getString(R.string.tag_route) -> {
                layout_run_spinner.apply {
                    primaryNumberPicker.visibility = View.INVISIBLE
                    secondNumberPicker.visibility = View.GONE
                    unit.visibility = View.INVISIBLE
                    decimal.text = context.getString(R.string.coming_soon)
                }
            }
            getString(R.string.tag_challenge) -> {
                layout_run_spinner.apply {
                    primaryNumberPicker.visibility = View.INVISIBLE
                    secondNumberPicker.visibility = View.GONE
                    unit.visibility = View.INVISIBLE
                    decimal.text = context.getString(R.string.coming_soon)
                }
            }
        }
    }

    private fun initCountdownTimer(timer: Long) : CountDownTimer {
        countdownTimer = object : CountDownTimer(timer, INTERVAL) {
            override fun onFinish() {
            }

            override fun onTick(millisUntilFinished: Long) {
                layout_play.metricTimeNum.setLongToTextTimer((timer - millisUntilFinished)+ INTERVAL)
                layout_play.tracker.setLongToTextTimer(millisUntilFinished)
            }

        }
        return countdownTimer
    }

    private fun initPreWorkoutCountdownTimer(timer : Long) {
        layoutQuickStartBtn.quickStartTV.text = ""
        layoutQuickStartBtn.quickStartTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
        val timer = object : CountDownTimer(5000, INTERVAL) {
            override fun onFinish() {
                initCountdownTimer(timer).start()
                layoutQuickStartBtn.visibility = View.GONE
            }

            override fun onTick(millisUntilFinished: Long) {
                if((millisUntilFinished/1000).toInt() <= 0) {
                    layoutQuickStartBtn.quickStartTV.text = getString(R.string.button_go)
                } else {
                    layoutQuickStartBtn.quickStartTV.setLongToTextTimer(millisUntilFinished, true)
                }
            }

        }.start()
    }

    private fun resetWorkout() {
        layoutQuickStartBtn.apply {
            quickStartTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            quickStartTV.text = getString(R.string.btn_quick_start)
            visibility = View.VISIBLE
        }
        layout_play.visibility = View.GONE
        layout_run_spinner.visibility = View.VISIBLE
        bottomSheetTitle.visibility = View.VISIBLE
        startBtn.visibility = View.VISIBLE
        bottomSheet.tabs.visibility = View.VISIBLE
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    ///////////////////////////////////////// INIT MAP ////////////////////////////////////////////

    private fun initMap() {
        view_map.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(mapboxMap: MapboxMap) {
                mapbox = mapboxMap
                mapbox.setStyle(
                    Style.MAPBOX_STREETS,
                    OnStyleLoaded { style ->
                        mStyle = style
                        getCurrentLocation()
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        //origin = Point.fromLngLat(-84.276938, 33.898869);
                        destination = Point.fromLngLat(-84.276938, 33.898869)
                        mStyle.initSource(origin, destination)
                        mStyle.initLayers(resources)
                        mapbox.addOnMapClickListener(OnMapClickListener { point ->
                            val s = String.format(
                                Locale.US,
                                "User clicked at: %s",
                                point.toString()
                            )
                            destination = Point.fromLngLat(
                                point.longitude,
                                point.latitude
                            )
                            getRoute(mStyle, origin, destination)
                            Log.i("AREA CLICKED", s)
                            Toast.makeText(context, s, Toast.LENGTH_LONG).show()
                            true
                        })
                    })
            }
        })
    }

    private fun getRoute(
        @NonNull style: Style,
        origin: Point,
        destination: Point
    ) {
        val dest = LatLng()
        dest.longitude = destination.longitude()
        dest.latitude = destination.latitude()
        client = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_WALKING)
            .accessToken(getString(R.string.mapbox_api_key))
            .build()
        client.enqueueCall(object : Callback<DirectionsResponse?> {
            override fun onResponse(
                call: Call<DirectionsResponse?>,
                response: Response<DirectionsResponse?>
            ) {
                println(call.request().url().toString())

// You can get the generic HTTP info about the response
                Log.d("Response code: ", response.body().toString())
                if (response.body() == null) {
                    Log.e("No routes fou you set", " the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Log.e("No routes found", "")
                    return
                }

// Get the directions route
                currentRoute = response.body()!!.routes()[0]
                mDistance.convertDistance(currentRoute)
                mDuration.convertDuration(currentRoute)

// Make a toast which displays the route's distance
                Log.i("CurrentRoute", currentRoute.toString())
                Log.i("D&D", "DIST $mDistance, DUR $mDuration")
                Toast.makeText(
                    activity,
                    "DIST $mDistance, DUR $mDuration",
                    Toast.LENGTH_SHORT
                ).show()
                if (style.isFullyLoaded) {
// Retrieve and update the source designated for showing the directions route
                    val source =
                        style.getSourceAs<GeoJsonSource>(MapViewExtensions.ROUTE_SOURCE_ID)

// Create a LineString with the directions route's geometry and
// reset the GeoJSON source for the route LineLayer source
                    if (source != null) {
                        Log.d("onResponse: ", "source != null")
                        source.setGeoJson(
                            FeatureCollection.fromFeature(
                                Feature.fromGeometry(
                                    LineString.fromPolyline(
                                        currentRoute.geometry()!!,
                                        Constants.PRECISION_6
                                    )
                                )
                            )
                        )
                        mapbox.shiftMapView(mLocationComponent, dest)
                    }
                }
            }

            override fun onFailure(
                call: Call<DirectionsResponse?>,
                throwable: Throwable
            ) {
                Log.e("Error: ", throwable.message)
                Toast.makeText(
                    context, "Error: " + throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun getCurrentLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            val locationComponentActivationOptions =
                LocationComponentActivationOptions.builder(context!!, mStyle)
                    .useDefaultLocationEngine(true).build()
            mLocationComponent = mapbox.getLocationComponent()
            if (ActivityCompat.checkSelfPermission(
                    activity!!.applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    activity!!.applicationContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mLocationComponent.activateLocationComponent(locationComponentActivationOptions)
            mLocationComponent.setLocationComponentEnabled(true)
            mLocationComponent.setRenderMode(RenderMode.GPS)
            mLocationComponent.setCameraMode(CameraMode.TRACKING)
            origin = Point.fromLngLat(
                mLocationComponent.getLastKnownLocation()!!.getLongitude(),
                mLocationComponent.getLastKnownLocation()!!.getLatitude()
            )
        } else {
            mPermissionsManager = PermissionsManager(this)
            mPermissionsManager.requestLocationPermissions(activity)
        }
    }

    /////////////////////////////////////// Resource Views /////////////////////////////////////////

    private val getCurrentRunMetrics = object : ResourceView<Run> {
        override fun showData(data: Run) {
            println("RUN GOT: $data")
        }

        override fun showLoading(isLoading: Boolean) {

        }

        override fun showError(error: Throwable) {

        }

    }

    private val getDurationLeft = object : ResourceView<Long> {
        override fun showData(data: Long) {
            println("DURATION GOT: $data")
            countdownTimer.createCountDownTimer(layout_play.tracker, data)
            /*layout_play.playImgCircle.playImg.setImageDrawable(resources.getDrawable(R.drawable.ic_pause))
            layout_play.playImgCircle.stopImgCircle.visibility = View.GONE
            layout_play.playImgCircle.stopImg.visibility = View.GONE*/
        }

        override fun showLoading(isLoading: Boolean) {

        }

        override fun showError(error: Throwable) {

        }

    }

    ///////////////////////////////////////// NOT USED /////////////////////////////////////////////
    ///////////////////////////////////////// NOT USED /////////////////////////////////////////////
    ///////////////////////////////////////// NOT USED /////////////////////////////////////////////

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        //Not used
    }

    override fun onPermissionResult(granted: Boolean) {
        //Not used
    }
}
