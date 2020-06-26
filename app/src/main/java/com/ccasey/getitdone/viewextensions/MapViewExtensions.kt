package com.ccasey.getitdone.viewextensions

import android.content.res.Resources
import android.graphics.Color
import com.ccasey.getitdone.R
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils

class MapViewExtensions {
    companion object {
        const val ROUTE_LAYER_ID = "route-layer-id"
        const val ROUTE_SOURCE_ID = "route-source-id"
        const val ICON_LAYER_ID = "icon-layer-id"
        const val ICON_SOURCE_ID = "icon-source-id"
        const val RED_PIN_ICON_ID = "red-pin-icon-id"
    }
}

fun MapboxMap.shiftMapView(locationComponent: LocationComponent, dest: LatLng) {
    val origin = LatLng()
    origin.latitude = locationComponent.lastKnownLocation!!.latitude
    origin.longitude = locationComponent.lastKnownLocation!!.longitude
    val latLngBounds =
        LatLngBounds.Builder().include(origin).include(dest).build()
    animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200), 1000)
}

fun String?.convertDistance(route: DirectionsRoute): String? {
    val km = route.distance()!! / 1000
    val miles = km * 0.621371
    return miles.toString()
}

fun String?.convertDuration(route: DirectionsRoute): String? {
    val minutes = route.duration()!! / 60
    return minutes.toString()
}

fun Style.initSource(origin: Point, destination: Point) {
    addSource(
        GeoJsonSource(
            MapViewExtensions.ROUTE_SOURCE_ID,
            FeatureCollection.fromFeatures(arrayOf())
        )
    )
    val iconGeoJsonSource = GeoJsonSource(
        MapViewExtensions.ICON_SOURCE_ID,
        FeatureCollection.fromFeatures(
            arrayOf(
                Feature.fromGeometry(
                    Point.fromLngLat(
                        origin!!.longitude(),
                        origin!!.latitude()
                    )
                ),
                Feature.fromGeometry(
                    Point.fromLngLat(
                        destination!!.longitude(),
                        destination!!.latitude()
                    )
                )
            )
        )
    )
    addSource(iconGeoJsonSource)
}

fun Style.initLayers(resources: Resources) {
    val routeLayer =
        LineLayer(MapViewExtensions.ROUTE_LAYER_ID, MapViewExtensions.ROUTE_SOURCE_ID)

// Add the LineLayer to the map. This layer will display the directions route.
    routeLayer.setProperties(
        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        PropertyFactory.lineWidth(5f),
        PropertyFactory.lineColor(
            Color.parseColor(
                "#009688"
            )
        )
    )
    addLayer(routeLayer)

// Add the red marker icon image to the map
    addImage(
        MapViewExtensions.RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
            resources.getDrawable(R.drawable.ic_play_arrow)
        )!!
    )

// Add the red marker icon SymbolLayer to the map
    addLayer(
        SymbolLayer(MapViewExtensions.ICON_LAYER_ID, MapViewExtensions.ICON_SOURCE_ID).withProperties(
            PropertyFactory.iconImage(MapViewExtensions.RED_PIN_ICON_ID),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.iconOffset(
                arrayOf(
                    0f,
                    -4f
                )
            )
        )
    )
}