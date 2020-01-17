package com.andb.apps.weather

import android.app.Application
import android.location.Geocoder
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.andb.apps.weather.data.local.Database
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.LocalDateTimeAdapter
import com.andb.apps.weather.data.model.ZoneOffsetAdapter
import com.andb.apps.weather.data.repository.DarkSkyRepo
import com.andb.apps.weather.data.repository.DarkSkyRepoImpl
import com.andb.apps.weather.data.repository.LocationRepo
import com.andb.apps.weather.data.repository.LocationRepoImpl
import com.andb.apps.weather.ui.location.LocationPickerFragment
import com.andb.apps.weather.ui.location.LocationPickerViewModel
import com.andb.apps.weather.ui.main.WeatherViewModel
import com.andb.apps.weather.ui.settings.SettingsFragment
import com.andb.apps.weather.ui.settings.SettingsLayout
import com.andb.apps.weather.ui.settings.SettingsViewModel
import com.chibatching.kotpref.Kotpref
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.moshi.Moshi
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

@ExperimentalCoroutinesApi
class App : Application() {

    private val koinModule = module {
        single {
            Room.databaseBuilder(androidContext(), Database::class.java, "WeatherDatabase")
                .fallbackToDestructiveMigration()
                .build()
        }
        single { val database: Database = get(); database.locationDao() }

        single {
            Moshi.Builder()
                .add(ZoneOffsetAdapter())
                .add(LocalDateTimeAdapter())
                .build()
        }
        single {
            Retrofit.Builder()
                .baseUrl("https://api.darksky.net/")
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }
        single<FusedLocationProviderClient> {
            LocationServices.getFusedLocationProviderClient(
                androidContext()
            )
        }
        single { Geocoder(androidContext(), Locale.getDefault()) }
        single<PlacesClient> { Places.createClient(androidContext()) }

        single<DarkSkyRepo> { DarkSkyRepoImpl(get(), get()) }
        single<LocationRepo> { LocationRepoImpl(get(), get(), get(), get()) }

        viewModel { WeatherViewModel(get(), get()) }
        viewModel { LocationPickerViewModel(get()) }
        viewModel { SettingsViewModel(PreferencesAdapter(SettingsLayout.create(androidContext()))) }

        single { SettingsFragment() }
        single { LocationPickerFragment() }
    }

    override fun onCreate() {
        super.onCreate()
        Places.initialize(this, BuildConfig.MAPS_API_KEY)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(koinModule)
        }
        AndroidThreeTen.init(this)
        Kotpref.init(this)
        AppCompatDelegate.setDefaultNightMode(Prefs.nightMode)
    }
}