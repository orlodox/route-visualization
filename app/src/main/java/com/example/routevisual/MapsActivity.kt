package com.example.routevisual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import java.util.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        val RECOGNIZE_SPEECH_REQUEST_CODE = 10
        var parsingResults: Pair<MutableList<Maneuver>, MutableList<String>> = Pair(
            mutableListOf(),
            mutableListOf()
        )
        val initPosition = LatLng(59.937261, 30.258291)
        const val initAzimuth = 60.toDouble()
        var stagedModeEnabled = false

    }

    private lateinit var mMap: GoogleMap
    private lateinit var geoApiContext: GeoApiContext

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        geoApiContext = GeoApiContext.Builder()
            .apiKey(this.getString(R.string.google_maps_key))
            .build()
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(initPosition).title("0"))
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(initPosition).bearing(initAzimuth.toFloat())
                    .zoom(13.5F).build()
            ), 5000, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                }

                override fun onCancel() {
                }
            }
        )

    }

    fun getSpeechInput(view: View) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, RECOGNIZE_SPEECH_REQUEST_CODE)
        } else {
            showToast("Устройство не поддерживает распознавание речи")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RECOGNIZE_SPEECH_REQUEST_CODE -> data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.let {
                    onSpeechRecognized(it[0])
                }
            else -> showToast("Не удалось распознать речь")

        }
    }

    private fun onSpeechRecognized(speech: String) {
        parsingResults = TextToManeuversConverter.convertText(speech)
        if (parsingResults.first.isNotEmpty() && parsingResults.second.isNotEmpty()) {
            resultTextView.text =
                parsingResults.second.stream().reduce { oneStr, str -> "$oneStr\n$str" }.get()
            val moving = Moving(initPosition, initAzimuth, geoApiContext, this)
            moving.doManeuvers(parsingResults.first, mMap)
        } else showToast("Маршрут не распознан")
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun onStagedModeClicked(view: View) {
        stagedModeEnabled = (view as CheckBox).isChecked
    }

    fun onClearClicked(view: View) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(initPosition).title("0"))
        mMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder().target(initPosition).bearing(initAzimuth.toFloat())
                    .zoom(13.5F).build()
            )
        )
        if (parsingResults != null) {
            parsingResults.first.clear()
            parsingResults.second.clear()
            resultTextView.text = ""
        }
    }

    fun onTestClick(view: View) {
        onSpeechRecognized("1000 м прямо налево 500 м направо 1000 м налево 200 м")
    }

}