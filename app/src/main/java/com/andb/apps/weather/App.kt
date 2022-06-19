package com.andb.apps.weather

import android.app.Application
import android.location.Geocoder
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.andb.apps.weather.data.local.Database
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.LocalDateTimeAdapter
import com.andb.apps.weather.data.model.climacell.ClimacellOzoneAdapter
import com.andb.apps.weather.data.model.climacell.ClimacellWindDirectionAdapter
import com.andb.apps.weather.data.model.climacell.LocalDateAdapter
import com.andb.apps.weather.data.model.climacell.ZonedDateTimeAdapter
import com.andb.apps.weather.data.model.darksky.ZoneOffsetAdapter
import com.andb.apps.weather.data.repository.LocationRepo
import com.andb.apps.weather.data.repository.LocationRepoImpl
import com.andb.apps.weather.data.repository.WeatherRepo
import com.andb.apps.weather.data.repository.WeatherRepoImpl
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
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.Maxr1998.modernpreferences.PreferencesAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
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
                .add(LocalDateAdapter())
                .add(ZonedDateTimeAdapter())
                .add(ClimacellWindDirectionAdapter())
                .add(ClimacellOzoneAdapter())
                .add(KotlinJsonAdapterFactory())
                .build()
        }

        single {
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        }

        single(named("darkSkyRetrofit")) {
            Retrofit.Builder()
                .client(get())
                .baseUrl("https://api.darksky.net/")
                .addConverterFactory(MoshiConverterFactory.create(get()))
                .build()
        }
        single(named("climacellRetrofit")) {
            Retrofit.Builder()
                .client(get())
                .baseUrl("https://api.climacell.co/v3/")
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

        single<WeatherRepo> {
            WeatherRepoImpl(
                get(),
                get(named("darkSkyRetrofit")),
                get(named("climacellRetrofit"))
            )
        }
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