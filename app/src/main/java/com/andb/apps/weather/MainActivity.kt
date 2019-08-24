package com.andb.apps.weather

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.github.rongi.klaster.Klaster
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupDailyView()
    }

    lateinit var request: DarkSkyRequest //TODO: viewmodel
    val dailyAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> by lazy { dailyAdapter() }

    @SuppressLint("MissingPermission")
    fun setupDailyView() {
        runWithPermissions(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION) {
            loadingBar.visibility = View.VISIBLE
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener {
                Log.d("locationLoad", "lat: ${it.latitude}, long: ${it.longitude}")
                newIoThread {
                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    val addressList = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                    val address: String = when (addressList.size) {
                        1 -> with(addressList[0]) { "$locality, $adminArea" }
                        else -> "None" //TODO: resource
                    }
                    mainThread {
                        locationText.text = address
                    }
                }

                newIoThread {
                    request = DarkSkyRepo.getForecast(
                        it?.latitude ?: 38.963, it?.longitude ?: -77.062
                    )
                    mainThread {
                        currentTemperature.text = String.format(resources.getString(R.string.degrees_placeholder), request.currently.temperature.toInt())
                        currentFeelsLike.text = String.format(resources.getString(R.string.feels_like_placeholder), request.currently.apparentTemperature.toInt())
                        loadingBar.visibility = View.GONE
                        dailyRecycler.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = dailyAdapter
                            recycledViewPool.setMaxRecycledViews(0, 8)
                        }
                    }
                }
            }


        }
    }

    fun dailyAdapter() = Klaster.get()
        .itemCount { request.daily.data.size }
        .view { _ ->
            return@view DailyForecastView(this).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        .bind { pos ->
            val dailyConditions = request.daily.data[pos]
            val hourlyConditions = request.hourly.data.filter {
                it.time.dayOfMonth == dailyConditions.time.dayOfMonth
                        && (7..23).contains(it.time.hour)
            }
            val timeZone = request.timezone

            (itemView as DailyForecastView).setupData(dailyConditions, hourlyConditions, timeZone)
        }
        .build()
}

