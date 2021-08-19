package com.vyvu.musikapp.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.R
import com.vyvu.musikapp.contract.uicontract.MainActivityContract
import com.vyvu.musikapp.databinding.ActivityMainBinding
import com.vyvu.musikapp.model.Mp3
import com.vyvu.musikapp.model.Result
import com.vyvu.musikapp.model.Video
import com.vyvu.musikapp.presenter.MainActivityPresenter
import com.vyvu.musikapp.service.MusikService
import com.vyvu.musikapp.utils.toSecond
import com.vyvu.musikapp.utils.toTime
import com.vyvu.musikapp.view.adapter.Mp3Adapter
import com.vyvu.musikapp.view.adapter.YTVideoAdapter

class MainActivity : AppCompatActivity(), MainActivityContract.View {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val presenter by lazy { MainActivityPresenter(this) }
    private var adapters = arrayOf<Any?>(null, null)
    private val broadcastReceiver by lazy {
        presenter.requestRegisterBroadcast(
            this,
            initIntentFilter()
        )
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                setSupportActionBar(binding.toolBar)
                initRecyclerYTVideoView()
                initLayoutRefresh()
                initTab()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        broadcastReceiver.goAsync()
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, PERM_REQUEST_FAILURE, Toast.LENGTH_LONG).show()
        }
    }

    private fun initIntentFilter(): IntentFilter =
        IntentFilter().apply {
            addAction(AppVals.Action.LOOP_SINGLE)
            addAction(AppVals.Action.OFF_LOOP_SINGLE)
            addAction(AppVals.Action.STOP)
            addAction(AppVals.Action.PLAY_STREAM)
            addAction(AppVals.Action.PLAY_PLAYLIST)
            addAction(AppVals.Action.PAUSE_PLAYLIST)
            addAction(AppVals.Action.RESUME_PLAYLIST)
            addAction(AppVals.Action.NEXT_PLAYLIST)
            addAction(AppVals.Action.PREVIOUS_PLAYLIST)
            addAction(AppVals.Action.LOOP_ALL_PLAYLIST)
            addAction(AppVals.Action.SHUFFLE_PLAYLIST)
            addAction(AppVals.Action.OFF_LOOP_ALL_PLAYLIST)
            addAction(AppVals.Action.OFF_SHUFFLE_PLAYLIST)
            addAction(AppVals.Action.STOP_SERVICE)
            addAction(AppVals.Action.DECLARE_CURRENT_POS)
        }

    private fun initRecyclerYTVideoView() {
        binding.recycler.run {
            if (adapters[TAB_CLOUD_POS] == null) {
                adapter = YTVideoAdapter(this@MainActivity)
                adapters[TAB_CLOUD_POS] = adapter
                layoutManager = LinearLayoutManager(this@MainActivity)
                presenter.requestYTVideo(DEFAULT_QUERY, DEFAULT_SETTING_PAGE_NUMBER)
            } else adapter = adapters[TAB_CLOUD_POS] as YTVideoAdapter
        }
    }

    private fun initRecyclerMp3View() {
        binding.recycler.run {
            if (adapters[TAB_STORAGE_POS] == null) {
                adapter = Mp3Adapter(this@MainActivity)
                adapters[TAB_STORAGE_POS] = adapter
                layoutManager = LinearLayoutManager(this@MainActivity)
                presenter.requestAllMp3(contentResolver)
            } else adapter = adapters[TAB_STORAGE_POS] as Mp3Adapter
        }
    }

    private fun initLayoutRefresh() {
        binding.run {
            layoutRefresh.setOnRefreshListener {
                layoutRefresh.isRefreshing = true
                if (recycler.adapter is YTVideoAdapter) presenter.requestYTVideo(
                    DEFAULT_QUERY,
                    DEFAULT_SETTING_PAGE_NUMBER
                )
                else presenter.requestAllMp3(contentResolver)
                layoutRefresh.isRefreshing = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_appbar_main_activity, menu)
        initSearchView()
        return true
    }

    private fun initSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (binding.toolBar.menu.findItem(R.id.itemSearch).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean =
                    (
                            if (binding.recycler.adapter is YTVideoAdapter)
                                presenter.requestYTVideo(
                                    query!!,
                                    DEFAULT_SETTING_PAGE_NUMBER
                                )
                            else
                                presenter.requestMp3(
                                    SELECTION_SEARCH,
                                    arrayOf("$PERCENT$query$PERCENT"),
                                    contentResolver
                                )
                            ).let {
                            clearFocus()
                            true
                        }

                override fun onQueryTextChange(newText: String?): Boolean = true
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.itemSettings -> {
                true
            }
            R.id.itemExit -> finish().let { true }
            else -> super.onOptionsItemSelected(item)
        }

    @SuppressLint("NotifyDataSetChanged")
    override fun onDataReceived(data: MutableList<*>) {
        binding.recycler.adapter?.run {
            if (this is YTVideoAdapter) {
                results = data.filterIsInstance<Result>().toMutableList()
            } else if (this is Mp3Adapter) {
                mp3s = data.filterIsInstance<Mp3>().toMutableList()
                this@MainActivity.startService(
                    Intent(this@MainActivity, MusikService::class.java).apply {
                        putExtra(
                            AppVals.Action.INTENT_KEY_ACTIONS_CONTROL,
                            AppVals.Action.INIT_PLAYLIST
                        )
                        putParcelableArrayListExtra(
                            AppVals.Action.INIT_PLAYLIST,
                            arrayListOf<Mp3>().apply { addAll(mp3s) })
                    }
                )
            }
            notifyDataSetChanged()
        }
    }

    private fun initTab() {
        binding.layoutTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.run {
                    when (tab?.position) {
                        TAB_CLOUD_POS -> {
                            initRecyclerYTVideoView()
                        }
                        TAB_STORAGE_POS -> {
                            initRecyclerMp3View()
                        }
                        else -> {
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })
    }

    override fun onDataFailed(exception: Exception) {
        throw exception
    }

    override fun onReceivedBroadcast(dataReceived: Intent) {
        when (dataReceived.action) {
            AppVals.Action.STOP -> afterRequestStop()
            AppVals.Action.LOOP_SINGLE -> afterRequestLoopSingle()
            AppVals.Action.OFF_LOOP_SINGLE -> afterRequestOffLoopSingle()
            AppVals.Action.PLAY_STREAM -> afterRequestPlayStream(dataReceived)
            AppVals.Action.PLAY_PLAYLIST -> afterRequestPlayPlaylist(dataReceived)
            AppVals.Action.PAUSE_PLAYLIST -> afterRequestPausePlaylist()
            AppVals.Action.RESUME_PLAYLIST -> afterRequestResumePlaylist()
            AppVals.Action.NEXT_PLAYLIST -> afterRequestNextPlaylist(dataReceived)
            AppVals.Action.PREVIOUS_PLAYLIST -> afterRequestPreviousPlaylist(dataReceived)
            AppVals.Action.LOOP_ALL_PLAYLIST -> afterRequestLoopAllPlaylist()
            AppVals.Action.SHUFFLE_PLAYLIST -> afterRequestShufflePlaylist()
            AppVals.Action.OFF_LOOP_ALL_PLAYLIST -> afterRequestOffLoopAllPlaylist()
            AppVals.Action.OFF_SHUFFLE_PLAYLIST -> afterRequestOffShufflePlaylist()
            AppVals.Action.STOP_SERVICE -> afterRequestStopService()
            AppVals.Action.DECLARE_CURRENT_POS -> afterDeclareCurrentPos(dataReceived)
            else -> {
            }
        }
    }

    private fun afterRequestStop() {
        binding.run {
            layoutControl.run {
                textTitle.text = AppVals.String.EMPTY_STRING
                textEndTime.text = AppVals.String.EMPTY_STRING
                buttonNext.isEnabled = true
                buttonPrevious.isEnabled = true
                buttonShuffle.isEnabled = true
                buttonPlayOrPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                slider.value=slider.valueTo
            }
            recycler.adapter.run {
                if (this is YTVideoAdapter) {
                    YTVideoAdapter.currentPlayingButton?.setIconResource(R.drawable.ic_baseline_play_arrow_24)
                    YTVideoAdapter.currentPlayingButton = null
                    YTVideoAdapter.oldPos = AppVals.Code.NONE_CODE
                    YTVideoAdapter.oldButton = null
                }
            }
        }
    }

    private fun afterRequestLoopSingle() {
        binding.layoutControl.buttonLoopOneOrAll.run {
            setImageResource(R.drawable.ic_baseline_filter_1_24)
            setOnClickListener { shortRequestControl(AppVals.Action.OFF_LOOP_SINGLE) }
        }
        binding.layoutControl.buttonShuffle.isEnabled = false
    }

    private fun afterRequestOffLoopSingle() {
        binding.run {
            layoutControl.run {
                buttonLoopOneOrAll.run {
                    setImageResource(R.drawable.ic_baseline_refresh_24)
                    setOnClickListener { shortRequestControl(AppVals.Action.LOOP_SINGLE) }
                }
                if (recycler.adapter is Mp3Adapter) buttonShuffle.isEnabled = true
            }
        }
    }

    private fun afterRequestPlayStream(dataReceived: Intent) {
        val data = dataReceived.getParcelableExtra<Video>(dataReceived.action)!!
        binding.layoutControl.run {
            textTitle.text = data.title
            textEndTime.text = data.duration
            buttonNext.isEnabled = false
            buttonPrevious.isEnabled = false
            buttonShuffle.isEnabled = false
            slider.valueTo=data.duration.toSecond().toFloat()
            buttonPlayOrPause.run {
                setImageResource(R.drawable.ic_baseline_stop_circle_24)
                setOnClickListener { shortRequestControl(AppVals.Action.STOP) }
            }
            buttonLoopOneOrAll.setOnClickListener { shortRequestControl(AppVals.Action.LOOP_SINGLE) }
            slider.clearOnSliderTouchListeners()
        }
    }

    private fun afterRequestPlayPlaylist(dataReceived: Intent) {
        val data = dataReceived.getParcelableExtra<Mp3>(dataReceived.action)!!
        binding.layoutControl.run {
            textTitle.text = data.title
            textEndTime.text = data.duration.toTime()
            slider.valueTo = (data.duration / AppVals.Other.MILI_TO_SEC).toFloat().inc()
            buttonNext.setOnClickListener { shortRequestControl(AppVals.Action.NEXT_PLAYLIST) }
            buttonPrevious.setOnClickListener { shortRequestControl(AppVals.Action.PREVIOUS_PLAYLIST) }
            buttonShuffle.setOnClickListener { shortRequestControl(AppVals.Action.SHUFFLE_PLAYLIST) }
            buttonPlayOrPause.run {
                setImageResource(R.drawable.ic_baseline_pause_24)
                setOnClickListener { shortRequestControl(AppVals.Action.PAUSE_PLAYLIST) }
            }
            buttonLoopOneOrAll.run {
                setOnClickListener { shortRequestControl(AppVals.Action.LOOP_SINGLE) }
                setOnLongClickListener {
                    shortRequestControl(AppVals.Action.LOOP_ALL_PLAYLIST)
                    true
                }
            }
            slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    this@MainActivity.startService(
                        Intent(this@MainActivity, MusikService::class.java).apply {
                            putExtra(
                                AppVals.Action.INTENT_KEY_ACTIONS_CONTROL,
                                AppVals.Action.SEEK_PLAYLIST
                            )
                            putExtra(
                                AppVals.Action.SEEK_PLAYLIST,
                                slider.value.dec() * AppVals.Other.MILI_TO_SEC
                            )
                        }
                    )
                }

            })
        }
    }

    private fun afterRequestPausePlaylist() {
        binding.layoutControl.buttonPlayOrPause.run {
            setImageResource(R.drawable.ic_baseline_play_arrow_24)
            setOnClickListener { shortRequestControl(AppVals.Action.RESUME_PLAYLIST) }
        }
    }

    private fun afterRequestResumePlaylist() {
        binding.layoutControl.buttonPlayOrPause.run {
            setImageResource(R.drawable.ic_baseline_pause_24)
            setOnClickListener { shortRequestControl(AppVals.Action.PAUSE_PLAYLIST) }
        }
    }

    private fun afterRequestNextPlaylist(dataReceived: Intent) {
        val data = dataReceived.getParcelableExtra<Mp3>(dataReceived.action)!!
        binding.layoutControl.run {
            textTitle.text = data.title
            textEndTime.text = data.duration.toTime()
            slider.valueTo = (data.duration / AppVals.Other.MILI_TO_SEC).toFloat().inc()
        }
    }

    private fun afterRequestPreviousPlaylist(dataReceived: Intent) {
        val data = dataReceived.getParcelableExtra<Mp3>(dataReceived.action)!!
        binding.layoutControl.run {
            textTitle.text = data.title
            textEndTime.text = data.duration.toTime()
            slider.valueTo = (data.duration / AppVals.Other.MILI_TO_SEC).toFloat().inc()
        }
    }

    private fun afterRequestLoopAllPlaylist() {
        binding.layoutControl.buttonLoopOneOrAll.run {
            setImageResource(R.drawable.ic_baseline_all_inclusive_24)
            setOnLongClickListener {
                shortRequestControl(AppVals.Action.OFF_LOOP_ALL_PLAYLIST)
                true
            }
        }
        binding.layoutControl.buttonShuffle.isEnabled = false
    }

    private fun afterRequestShufflePlaylist() {
        binding.layoutControl.buttonShuffle.run {
            setImageResource(R.drawable.ic_baseline_trending_flat_24)
            setOnClickListener { shortRequestControl(AppVals.Action.OFF_SHUFFLE_PLAYLIST) }
        }
        binding.layoutControl.buttonLoopOneOrAll.isEnabled = false
    }

    private fun afterRequestOffLoopAllPlaylist() {
        binding.layoutControl.buttonLoopOneOrAll.run {
            setImageResource(R.drawable.ic_baseline_refresh_24)
            setOnLongClickListener {
                shortRequestControl(AppVals.Action.LOOP_ALL_PLAYLIST)
                true
            }
        }
        binding.layoutControl.buttonShuffle.isEnabled = true
    }

    private fun afterRequestOffShufflePlaylist() {
        binding.layoutControl.buttonShuffle.run {
            setImageResource(R.drawable.ic_baseline_shuffle_24)
            setOnClickListener { shortRequestControl(AppVals.Action.SHUFFLE_PLAYLIST) }
        }
        binding.layoutControl.buttonLoopOneOrAll.isEnabled = true
    }

    private fun afterDeclareCurrentPos(dataReceived: Intent) {
        binding.layoutControl.slider.run {
            value = (dataReceived.getLongExtra(
                dataReceived.action,
                valueFrom.toLong()
            ) / AppVals.Other.MILI_TO_SEC).toFloat()
        }
    }

    private fun shortRequestControl(action: String) {
        this@MainActivity.startService(
            Intent(this@MainActivity, MusikService::class.java).apply {
                putExtra(AppVals.Action.INTENT_KEY_ACTIONS_CONTROL, action)
            }
        )
    }

    private fun afterRequestStopService() {
        afterRequestStop()
    }

    override fun showProgress() {
        binding.indicatorProgress.show()
    }

    override fun dismissProgress() {
        binding.indicatorProgress.hide()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private companion object {
        const val TAB_CLOUD_POS = 0
        const val TAB_STORAGE_POS = 1
        const val SELECTION_SEARCH = "${MediaStore.Audio.Media.TITLE} LIKE ?"
        const val PERCENT = "%"
        const val PERM_REQUEST_FAILURE = "You can't use download option"
        const val DEFAULT_QUERY = "%26"
        const val DEFAULT_SETTING_PAGE_NUMBER = 1
    }
}
