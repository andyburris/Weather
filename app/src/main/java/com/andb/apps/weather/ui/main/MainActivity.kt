package com.andb.apps.weather.ui.main

import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.transition.TransitionManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentTransaction
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
import com.andb.apps.weather.util.*
import com.github.rongi.klaster.Klaster
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.offline_item.*
import kotlinx.android.synthetic.main.scroll_layout.*
import org.koin.android.ext.android.get
import org.koin.android.viewmodel.ext.android.viewModel


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

        locationText.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            ft.apply {
                add(R.id.fragmentHolder, locationPickerFragment)
                addToBackStack("location")
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                commit()
            }
        }

        settingsButton.setOnClickListener {
            Log.d("settingsButton", "clicked")
            val ft = supportFragmentManager.beginTransaction()
            ft.apply {
                add(R.id.fragmentHolder, settingsFragment)
                addToBackStack("settings")
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                commit()
            }
        }

        testButton.setOnClickListener {
            Log.d("testButton", "clicked")
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
        viewModel.offline.observe(this) {
            offlineItem.visibility = if (it) View.VISIBLE else View.GONE
        }
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
        viewModel.minutely.observe(this) { minutelyView.setup(it.first, it.second) }
        viewModel.dailyForecasts.observe(this) {
            dayList = it
            dailyAdapter.notifyDataSetChanged()
            dailyRecycler.doOnNextLayout {
                val itemHeight = (dailyRecycler.layoutManager?.findViewByPosition(0)?.height
                    ?: 0)
                Log.d("cardOffset", "itemHeight: $itemHeight")
                currentTemperature.layoutParams =
                    (currentTemperature.layoutParams as ConstraintLayout.LayoutParams).also {
                        it.topMargin =
                            Resources.getSystem().displayMetrics.heightPixels - itemHeight - dpToPx(
                                16 + 8 + 8
                            ) - currentFeelsLike.height - currentTemperature.height - bottomCard.height - minutelyView.height
                    }
            }
        }

        minutelyView.onToggle = { collapsing, oldHeight, newHeight, animate ->
            //val heightOffset = if(collapsed) -357 else 357
            val heightOffset = oldHeight - newHeight
            Log.d(
                "onToggle",
                "oldHeight: $oldHeight, newHeight: $newHeight, heightOffset: $heightOffset"
            )
            if (animate) {
                TransitionManager.beginDelayedTransition(nestedScrollView)
            }
            currentTemperature.layoutParams =
                (currentTemperature.layoutParams as ConstraintLayout.LayoutParams).also {
                    it.topMargin = it.topMargin + heightOffset
                }
        }
        minutelyView.onTouch = { action ->
            if (action == MotionEvent.ACTION_UP) {
                //nestedScrollView.startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL)
                Log.d("scrollingOnTouch", "true")
                nestedScrollView.setScrollingEnabled(true)
            } else {
                //nestedScrollView.stopNestedScroll()
                Log.d("scrollingOnTouch", "false")
                nestedScrollView.setScrollingEnabled(false)
            }
        }

    }

    private val dailyAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> by lazy { dailyAdapter() }

    var selectedChip = 0

    fun setupRecycler() {
        dailyRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dailyAdapter
            recycledViewPool.setMaxRecycledViews(0, 8)
        }
    }

    fun setupBackground() {
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

    fun setupChips() {

        colors = listOf(
            ContextCompat.getColor(this, R.color.chipDefaultDay),
            Prefs.colorTemperature,
            Prefs.colorRain,
            Prefs.colorUVIndex,
            Prefs.colorWind
        )
            .map { Pair(it, chipTextFrom(it)) }

        chipList.forEachIndexed { index, chip ->
            chip.chipStrokeColor = ColorStateList.valueOf(colors[index].first)
            chip.chipBackgroundColor =
                createChipStateList(colors[index].first)
        }

        dailyChipSummary.setOnClickListener { selectChip(0) }
        dailyChipTemperature.setOnClickListener { selectChip(1) }
        dailyChipTemperature.setOnLongClickListener { selectChip(1, alternativeData = true); true }
        dailyChipRain.setOnClickListener { selectChip(2) }
        dailyChipRain.setOnLongClickListener { selectChip(2, alternativeData = true); true }
        dailyChipUV.setOnClickListener { selectChip(3) }
        dailyChipWind.setOnClickListener { selectChip(4) }

        selectChip(selectedChip, updateGraphs = false)
    }

    private val chipList by lazy {
        listOf(dailyChipSummary, dailyChipTemperature, dailyChipRain, dailyChipUV, dailyChipWind)
    }
    private lateinit var colors: List<Pair<Int, Int>>
    private fun selectChip(
        index: Int,
        alternativeData: Boolean = false,
        updateGraphs: Boolean = true
    ) {
        selectedChip = index + if (alternativeData) 4 else 0
        val chip = chipList[index]
        chip.apply {
            isSelected = true
            setTextColor(colors[index].second)
            chipIconTint = ColorStateList.valueOf(colors[index].second)
        }
        chipList.minus(chip).forEach {
            it.isSelected = false
            val textIconColor = colorByNightModeRes(
                R.color.chipDefaultDay,
                R.color.chipDefaultNight
            )
            it.setTextColor(textIconColor)
            it.chipIconTint = ColorStateList.valueOf(textIconColor)
        }

        if (updateGraphs) {
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
                    (itemView as DailyForecastView).setupData(
                        selectedChip,
                        item.day,
                        item.hourly,
                        item.timeZone
                    )
                }
                payloads[0] == PAYLOAD_COLOR_CHANGE -> (itemView as DailyForecastView).refreshColors(
                    selectedChip
                )
                payloads[0] == PAYLOAD_SCROLL_SYNC -> (itemView as DailyForecastView).syncScroll()
                else -> (itemView as DailyForecastView).changeDisplay(selectedChip)
            }
        }
        .build()

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

