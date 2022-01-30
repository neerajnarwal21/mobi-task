package com.task.mobiadmin.fragment

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.view.InflateException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.gson.JsonObject
import com.task.mobiadmin.R
import com.task.mobiadmin.utils.Const
import com.task.mobiadmin.utils.PermissionsManager
import com.task.mobiadmin.utils.maps.LocationManager
import com.task.mobiadmin.utils.maps.MapUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fg_pin_add.*


/**
 * Created by neeraj.narwal on 16/1/17.
 */

class AddLocationPinFragment : BaseFragment(), LocationManager.LocationUpdates, GoogleMap.OnCameraIdleListener {

    private var vieww: View? = null
    private var googleMap: GoogleMap? = null
    private lateinit var currentLatLng: LatLng
    private val locationManager = LocationManager()
    private var pinAdd: PinAdd? = null
    private var goBack: Boolean = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (vieww != null) {
            val parent = vieww!!.parent as ViewGroup
            parent.removeView(vieww)
        }
        try {
            vieww = inflater!!.inflate(R.layout.fg_pin_add, container, false)
            setToolbar(true, "Add Location")
        } catch (e: InflateException) {
            e.printStackTrace()
        }
        return vieww
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pinAdd = PinAdd.instance
        currentLatLng = LatLng(store.getString(Const.LAST_LATITUDE, "0.020").toDouble(), store.getString(Const.LAST_LONGITUDE, "0.020").toDouble())
        if (PermissionsManager.checkPermissions(baseActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 120, object : PermissionsManager.PermissionCallback {
                    override fun onPermissionsGranted(resultCode: Int) {
                        initUI()
                    }

                    override fun onPermissionsDenied(resultCode: Int) {
                        goBack = true
                    }

                }))
            initUI()
    }

    override fun onResume() {
        super.onResume()
        if (goBack) baseActivity.supportFragmentManager.popBackStack()
    }

    private fun initUI() {
        locationManager.startLocationManager(baseActivity, LocationManager.Accuracy.LOW, this)
        initializeMap()
        doneBT.setOnClickListener {
            store.saveString(Const.LAST_LATITUDE, currentLatLng.latitude.toString())
            store.saveString(Const.LAST_LONGITUDE, currentLatLng.longitude.toString())
            log("Address " + currentLatLng + " " + getText(addressTV))
            pinAdd!!.sendBackLocation(getText(addressTV), currentLatLng)
            baseActivity.supportFragmentManager.popBackStack()
        }
        searchTV.setOnClickListener {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                        .build(baseActivity)
                baseActivity.startActivityForResult(intent, Const.PLACE_REQUEST)
                addressCL.visibility = View.GONE
            } catch (e: GooglePlayServicesRepairableException) {
                showToast("Play services Repair required")
            } catch (e: GooglePlayServicesNotAvailableException) {
                showToast("Play Services not available")
            }
        }
        updateText()
    }

    private fun initializeMap() {
        if (googleMap == null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync { googleMap ->
                this@AddLocationPinFragment.googleMap = googleMap
                updateCamera(false)
                googleMap.setOnCameraIdleListener(this@AddLocationPinFragment)
                googleMap.setOnCameraMoveListener {
                    addressTV.text = ""
                    doneBT.visibility = View.GONE
                }
            }
        }
    }

    override fun onCameraIdle() {
        currentLatLng = googleMap!!.cameraPosition.target
        updateText()
    }

    private var sub: Disposable? = null
    @Synchronized
    private fun updateText() {
        val o = Observable.just(MapUtils.with(baseActivity).getAddressFromLatLng(currentLatLng.latitude, currentLatLng.longitude))
        sub = o.subscribe { address ->
            log("After rx call single $address")
            if (address != "Not Found" && isAdded) {
                addressTV.text = address
                doneBT.visibility = View.VISIBLE
            } else {
                log("Starting rx call Address")
                val url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + currentLatLng.latitude + "," + currentLatLng.longitude + "&sensor=true"
                val call = apiInterface.getDecodeAddress(url)
                var addresss = "No Address to this location"
                sub = call.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete {
                            log("After rx call Address")
                            if (isAdded) {
                                addressTV.text = addresss
                                doneBT.visibility = View.VISIBLE
                            }
                        }
                        .subscribe({ jsonObject: JsonObject ->
                            if (jsonObject.getAsJsonArray("results").size() > 0)
                                addresss = jsonObject.getAsJsonArray("results").get(0).asJsonObject.get("formatted_address").asString
                        }, {
                            log("After rx call Error")
                            if (isAdded) {
                                addressTV.text = addresss
                                doneBT.visibility = View.VISIBLE
                            }
                        })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sub != null && !sub!!.isDisposed) {
            log("Disposing")
            sub?.dispose()
        }
    }

    fun onPlaceSelected(place: Place) {
        currentLatLng = place.latLng
        addressTV.text = place.address
        googleMap?.setOnCameraIdleListener(null)
        updateCamera(false)
        Handler().postDelayed({ googleMap?.setOnCameraIdleListener(this@AddLocationPinFragment) }, 200)
    }

    fun onSearchComplete() {
        addressCL.visibility = View.VISIBLE
        baseActivity.hideSoftKeyboard()
    }

    private fun updateCamera(animate: Boolean) {
        if (googleMap != null) {
            val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(currentLatLng.latitude, currentLatLng.longitude))
                    .zoom(15f)
                    .build()
            if (animate)
                googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            else
                googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    override fun onLocationFound(location: Location) {
        currentLatLng = LatLng(location.latitude, location.longitude)
        updateCamera(true)
    }

    override fun onLocationNotFound() {

    }

    override fun onLocationPermissionDenied() {

    }
}
