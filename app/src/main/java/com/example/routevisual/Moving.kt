package com.example.routevisual

import android.graphics.Color
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.errors.ApiException
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import java.io.IOException
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class Moving(
    var position: LatLng,
    var azimuth: Double,
    val geoApiContext: GeoApiContext,
    val activity: MapsActivity
) {
    companion object {
        const val METERS_IN_ONE_LAT_DEGREE = 111134.861111
        const val METERS_IN_ONE_LNG_EQUATOR_DEGREE = 111321.377778

    }

    fun doManeuver(maneuver: Maneuver, numberOfManeuver: Int, mMap: GoogleMap) {
        when (maneuver.type) {
            ManeuverType.STRAIGHT -> {
                position = getPositionAfterMovingForward(maneuver.numberOfMeters)
                val marker = MarkerOptions()
                    .position(position)
                    .title(numberOfManeuver.toString())

                mMap.addMarker(MarkerOptions().position(position))
            }
            ManeuverType.RIGHT_TURN -> {
                azimuth += 90.0
            }
            ManeuverType.LEFT_TURN -> {
                azimuth -= 90.0
            }
            ManeuverType.BACK_TURN -> {
                azimuth += 180.0
            }

        }
    }

    private fun getPositionAfterMovingForward(amountMeters: Int): LatLng {
        val deltaLat = cos(azimuth.toRadians()) * (amountMeters / METERS_IN_ONE_LAT_DEGREE)
        val METERS_IN_ONE_LNG_DEGREE =
            cos(position.latitude.toRadians()) * METERS_IN_ONE_LNG_EQUATOR_DEGREE
        val deltaLng = sin(azimuth.toRadians()) * (amountMeters / METERS_IN_ONE_LNG_DEGREE)

        return LatLng(position.latitude + deltaLat, position.longitude + deltaLng)
    }

    private fun Double.toRadians(): Double {
        return this * (PI / 180)
    }

    fun doManeuvers(maneuvers: List<Maneuver>, mMap: GoogleMap) {
        val places: MutableList<LatLng> = mutableListOf()
        places.add(position)
        maneuvers.forEach {
            doManeuver(it, maneuvers.indexOf(it), mMap)
            places.add(position)
            updateCamera(mMap)
        }
        val result = getPath(places.map { it.toBad() })
        if (result != null) drawLineByPath(result, mMap)
    }

    private fun updateCamera(mMap: GoogleMap) {
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(position).bearing(azimuth.toFloat()).zoom(13.5F)
                    .build()
            )
        )

    }

    private fun getPath(places: List<com.google.maps.model.LatLng>): DirectionsResult? {
        var result: DirectionsResult? = null
        try {
            result = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(places.first())
                .destination(places.last())
                .waypoints(*(places.filter { places.indexOf(it) > 0 && places.indexOf(it) < places.lastIndex }
                    .toTypedArray()))
                .await()
        } catch (e: ApiException) {
            e.printStackTrace();
        } catch (e: InterruptedException) {
            e.printStackTrace();
        } catch (e: IOException) {
            e.printStackTrace();
        }
        return result
    }

    private fun clearLines(mMap: GoogleMap) {
        mMap.clear()
    }
// 195.70.215.232
    private fun drawLineByPath(result: DirectionsResult, mMap: GoogleMap) {

        val mPoints = PolyUtil.decode(result.routes[0].overviewPolyline.encodedPath)
        val line = PolylineOptions()
            .addAll(mPoints)
            .width(9f)
            .color(Color.parseColor("#DC0000"))

        (activity.supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment).getMapAsync(OnMapReadyCallback { mapboxMap ->
            mapboxMap.addPolyline(line)
            activity
        })

    }

    private fun com.google.maps.model.LatLng.toGood(): LatLng = LatLng(this.lat, this.lng)
    private fun LatLng.toBad(): com.google.maps.model.LatLng =
        com.google.maps.model.LatLng(this.latitude, this.longitude)

}