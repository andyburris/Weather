package com.andb.apps.weather.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.assent.Permission
import com.afollestad.assent.runWithPermissions
import com.andb.apps.weather.R
import com.andb.apps.weather.data.local.Prefs
import com.andb.apps.weather.data.model.DayItem
import com.andb.apps.weather.ui.daily.DailyForecastView
import com.andb.apps.weather.ui.location.LocationPickerFragment
import com.andb.apps.weather.ui.main.weatherView.MaterialWeatherView
import com.andb.apps.weather.ui.main.weatherView.WeatherView
import com.andb.apps.weather.ui.settings.SettingsFragment
import com.andb.apps.weather.ui.test.TestFragment
import com.andb.apps.weather.util.dpToPx
import com.andb.apps.weather.util.observe
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.offline_item.*
import kotlinx.android.synthetic.main.scroll_layout.*
import org.koin.android.ext.android.get


class MainActivity : AppCompatActivity() {

    val weatherView by lazy { MaterialWeatherView(this) }
    private val viewModel: WeatherViewModel by viewModel()

    private val settingsFragment: SettingsFragment = get()
    private val locationPickerFragment: LocationPickerFragment = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBackground()
        setupChips()
        setupRecycler()

        locationText.setOnClickListener { anchorView ->
            locationPickerFragment.showWithAnchor(supportFragmentManager, "location", locationIcon)
        }

        settingsButton.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            ft.apply {
                add(R.id.fragmentHolder, settingsFragment)
                addToBackStack("settings")
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                commit()
            }
        }

        testButton.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            ft.apply {
                add(R.id.fragmentHolder, TestFragment())
                addToBackStack("test")
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                commit()
            }
        }

        refreshLayout.apply {
            setOnRefreshListener {
                viewModel.refresh()
            }
        }

        offlineRefreshButton.setOnClickListener {
            viewModel.refresh()
        }

        if (viewModel.isInitial()) {
            runWithPermissions(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION) {
                refreshLayout.isRefreshing = true
            }
        }

        viewModel.loading.observe(this) {
            refreshLayout.isRefreshing = it; Log.d(
            "loadingObserve",
            "refreshLayout.isRefreshing = ${refreshLayout.isRefreshing}"
        )
        }
        viewModel.offline.observe(this) { offlineItem.isVisible = it }
        viewModel.locationName.observe(this) { locationText.text = it }
        viewModel.currentTemp.observe(this) {
            currentTemperature.text = String.format(
                resources.getString(
                    R.string.degrees_placeholder
                ), it
            )
        }
        viewModel.currentFeelsLike.observe(this) {
            currentFeelsLike.text =
                String.format(resources.getString(R.string.feels_like_placeholder), it)
        }
        viewModel.currentBackground.observe(this) { weatherView.setWeather(it.first, it.second) }
        viewModel.minutely.observe(this) { minutelyView.setup(it) }
        viewModel.dailyForecasts.observe(this) {
            dayList = it
            dailyAdapter.notifyDataSetChanged()
            dailyRecycler.doOnNextLayout {
                val itemHeight = dailyRecycler.layoutManager?.findViewByPosition(0)?.height ?: 0
                Log.d("cardOffset", "itemHeight: $itemHeight")
                currentTemperature.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = (Resources.getSystem().displayMetrics.heightPixels
                            - itemHeight
                            - dpToPx(16 + 8 + 8)
                            - currentFeelsLike.height
                            - currentTemperature.height
                            - bottomCard.height
                            - minutelyView.height)
                }
            }
            providerBadge.text = resources.getString(R.string.provider_placeholder)
                .format(Prefs.providers.first().name)
        }

        minutelyView.onToggle = { heightChange, animate ->
            Log.d("onToggle", "heightChange: $heightChange")
            if (animate) {
                TransitionManager.beginDelayedTransition(nestedScrollView)
            }
            currentTemperature.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin += heightChange
            }
        }
        minutelyView.onTouch = { action ->
            Log.d("minutelyViewOnTouch", "action == $action")
            nestedScrollView.setScrollingEnabled(action == MotionEvent.ACTION_UP)
        }

    }

    private val dailyAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> by lazy { dailyAdapter() }

    private var selectedChip = 0

    private fun setupRecycler() {
        dailyRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dailyAdapter
            recycledViewPool.setMaxRecycledViews(0, 8)
        }
    }

    private fun setupBackground() {
        weatherViewHolder.addView(
            weatherView,
            0,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        weatherView.setDrawable(true)
        weatherView.setWeather(WeatherView.WEATHER_KIND_CLEAR, true)
        nestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            weatherView.onScroll(scrollY)
        }
    }

    private fun setupChips() {
        bottomCard.onChipSelectedListener = { chip, longClick ->
            selectedChip = chip + if (longClick) 4 else 0
            dailyAdapter.notifyItemRangeChanged(0, dayList.size, PAYLOAD_CHIP_CHANGE)
        }
    }

    var dayList = listOf<DayItem>()
    private fun dailyAdapter() = Klaster.get()
        .itemCount { dayList.size }
        .view { _ ->
            return@view DailyForecastView(this).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }
        .bind { _: Int ->
            this.setIsRecyclable(false)
        }
        .bind { position, payloads ->
            when {
                payloads.isEmpty() -> {
                    val item = dayList[position]
                    (itemView as DailyForecastView).apply {
                        setupData(selectedChip, item.day, item.hourly, getGraphConfig())
                        syncScrollListener = { _, newPos ->
                            val layoutManager = (dailyRecycler.layoutManager as LinearLayoutManager)
                            for (i in layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()) {
                                (dailyRecycler[i] as DailyForecastView).syncScroll(
                                    newPos,
                                    anchorHour()
                                )
                            }
                        }
                    }
                }
                payloads[0] == PAYLOAD_CHIP_CHANGE -> (itemView as DailyForecastView).changeDisplay(
                    selectedChip
                )
                payloads[0] == PAYLOAD_COLOR_CHANGE -> (itemView as DailyForecastView).refreshColors(
                    selectedChip
                )
            }
        }
        .build()

    private fun getGraphConfig(): GraphConfig {
        val allHourly = dayList.flatMap { it.hourly }
        val rainMax = allHourly.maxByOrNull { it.precipIntensity }?.precipIntensity ?: 0.0
        val tempMin = allHourly.minByOrNull { it.temperature }?.temperature ?: 0.0
        val tempMax = allHourly.maxByOrNull { it.temperature }?.temperature ?: 100.0
        val windMax = allHourly.maxByOrNull { it.windSpeed }?.windSpeed ?: 0.0
        return GraphConfig(rainMax, tempMin, tempMax, windMax)
    }

    override fun onBackPressed() {
        when {
            settingsFragment.backPossible() -> settingsFragment.goBack()
            supportFragmentManager.backStackEntryCount > 0 -> {
                supportFragmentManager.popBackStack()
                refreshColors()
                refreshTimeRange()
            }
            else -> super.onBackPressed()
        }
    }

    private fun refreshColors() {
        setupChips()
        dailyAdapter.notifyItemRangeChanged(0, dayList.size, PAYLOAD_COLOR_CHANGE)
    }

    private fun refreshTimeRange() {
        val currentHourly = viewModel.dailyForecasts.value?.get(1)?.hourly ?: return
        val needToRefreshStart = currentHourly.firstOrNull()?.time?.hour != Prefs.dayStart
        val needToRefreshEnd = currentHourly.lastOrNull()?.time?.hour != Prefs.dayEnd
        if (needToRefreshStart || needToRefreshEnd) {
            viewModel.refresh()
        }
    }

    companion object {
        private const val PAYLOAD_COLOR_CHANGE = 34325
        private const val PAYLOAD_SCROLL_SYNC = 34325
        private const val PAYLOAD_CHIP_CHANGE = 98712
    }
}

class GraphConfig(
    val rainMax: Double,
    val tempMin: Double,
    val tempMax: Double,
    val windMax: Double
)
