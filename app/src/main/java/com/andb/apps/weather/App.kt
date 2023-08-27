package com.andb.apps.weather

import android.app.Application
import android.location.Geocoder
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.andb.apps.weather.data.local.DEBUG_OFFLINE
import com.andb.apps.weather.data.local.Database
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.local.WeatherSettings
import com.andb.apps.weather.data.local.dataStore
import com.andb.apps.weather.data.model.LocalDateTimeAdapter
import com.andb.apps.weather.data.model.climacell.ClimacellOzoneAdapter
import com.andb.apps.weather.data.model.climacell.ClimacellWindDirectionAdapter
import com.andb.apps.weather.data.model.climacell.LocalDateAdapter
import com.andb.apps.weather.data.model.climacell.ZonedDateTimeAdapter
import com.andb.apps.weather.data.remote.WeatherKitService
import com.andb.apps.weather.data.repository.location.LocationRepo
import com.andb.apps.weather.data.repository.location.LocationRepoImpl
import com.andb.apps.weather.data.repository.weather.MockProviderRepo
import com.andb.apps.weather.data.repository.weather.ProviderRepo
import com.andb.apps.weather.data.repository.weather.WeatherKitConfig
import com.andb.apps.weather.data.repository.weather.WeatherKitRepo
import com.andb.apps.weather.data.repository.weather.WeatherRepo
import com.andb.apps.weather.data.repository.weather.WeatherRepoImpl
import com.chibatching.kotpref.Kotpref
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.russhwolf.settings.datastore.DataStoreSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Locale

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

        single(named("weatherKitRetrofit")) {
            Retrofit.Builder()
                .client(get())
                .baseUrl("https://weatherkit.apple.com/")
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

        single<WeatherSettings> {
            WeatherSettings(DataStoreSettings(datastore = dataStore))
        }
        single<ProviderRepo> {
            if (DEBUG_OFFLINE) {
                MockProviderRepo(get())
            } else {
                val weatherKitRetrofit: Retrofit = get(named("weatherKitRetrofit"))
                val settings: WeatherSettings = get()
                WeatherKitRepo(
                    weatherKitService = weatherKitRetrofit.create(WeatherKitService::class.java),
                    config = WeatherKitConfig(
                        keyID = BuildConfig.WEATHERKIT_KEY_ID,
                        teamID = BuildConfig.WEATHERKIT_TEAM_ID,
                        serviceID = BuildConfig.WEATHERKIT_SERVICE_ID,
                        privateKey = BuildConfig.WEATHERKIT_PRIVATE_KEY,
                    )
                )
            }
        }
        single<WeatherRepo> {
            WeatherRepoImpl(get())
        }
        single<LocationRepo> { LocationRepoImpl(get(), get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        Places.initialize(this, BuildConfig.MAPS_API_KEY)
        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(koinModule)
        }
        Kotpref.init(this)
        AppCompatDelegate.setDefaultNightMode(Prefs.nightMode)
    }
}