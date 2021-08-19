package com.vyvu.musikapp.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.os.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.vyvu.musikapp.AppVals
import com.vyvu.musikapp.R
import com.vyvu.musikapp.model.Mp3
import com.vyvu.musikapp.model.Video
import com.vyvu.musikapp.service.utils.MusikUtils
import kotlinx.coroutines.*

class MusikService : Service() {
    private var playlist = mutableListOf<Mp3>()
    private var cursor = ZERO
    private var isShuffle = false
    private var isLoopAll = false
    private var streamingSong: Video? = null
    private var playingSong: Mp3? = null
    private var handler = Handler(Looper.getMainLooper())
    private val mediaPlayer by lazy {
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { it.start() }
        }
    }
    private val broadcast by lazy { LocalBroadcastManager.getInstance(applicationContext) }
    private val sendPosTask by lazy {
        object : Runnable {
            override fun run() {
                broadcast.sendBroadcast(
                    Intent(AppVals.Action.DECLARE_CURRENT_POS).apply {
                        putExtra(
                            AppVals.Action.DECLARE_CURRENT_POS,
                            mediaPlayer.currentPosition.toLong()
                        )
                    }
                )
                handler.postDelayed(this, (AppVals.Other.MILI_TO_SEC * FACTOR).toLong())
            }
        }
    }
    private val mediaSession by lazy {
        MediaSession(
            applicationContext,
            AppVals.String.MUSIK_SERVICE_NAME
        )
    }
    private val streamButtons by lazy {
        arrayOf(
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_stop_circle_24,
                AppVals.Action.STOP,
                AppVals.Action.STOP_CODE
            ).build(),
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_refresh_24,
                AppVals.Action.LOOP_SINGLE,
                AppVals.Action.LOOP_SINGLE_CODE
            ).build(),
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_delete_forever_24,
                AppVals.Action.STOP_SERVICE,
                AppVals.Action.STOP_SERVICE_CODE
            ).build()
        )
    }

    private val playListButtons by lazy {
        arrayOf(
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_shuffle_24,
                AppVals.Action.SHUFFLE_PLAYLIST,
                AppVals.Action.SHUFFLE_PLAYLIST_CODE
            ).build(),
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_skip_previous_24,
                AppVals.Action.PREVIOUS_PLAYLIST,
                AppVals.Action.PREVIOUS_PLAYLIST_CODE
            ).build(),
            null,
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_skip_next_24,
                AppVals.Action.NEXT_PLAYLIST,
                AppVals.Action.NEXT_PLAYLIST_CODE
            ).build(),
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_refresh_24,
                AppVals.Action.LOOP_ALL_PLAYLIST,
                AppVals.Action.LOOP_ALL_PLAYLIST_CODE
            ).build(),
            MusikUtils.buildActionButton(
                this,
                R.drawable.ic_baseline_delete_forever_24,
                AppVals.Action.STOP_SERVICE,
                AppVals.Action.STOP_SERVICE_CODE
            ).build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {
            val action = intent.getStringExtra(AppVals.Action.INTENT_KEY_ACTIONS_CONTROL)
            broadcast.sendBroadcast(
                when (action) {
                    AppVals.Action.INIT_PLAYLIST -> initPlaylist(getParcelableArrayListExtra(action)!!)
                    AppVals.Action.PLAY_STREAM -> playStream(getParcelableExtra(action)!!, action)
                    AppVals.Action.PLAY_PLAYLIST -> playPlaylist(getParcelableExtra(action)!!, action)
                    AppVals.Action.STOP -> stop()
                    AppVals.Action.LOOP_SINGLE -> loopSingle(action)
                    AppVals.Action.OFF_LOOP_SINGLE -> offLoopSingle(action)
                    AppVals.Action.PAUSE_PLAYLIST -> pausePlaylist().also { notifyPlaylist(action) }
                    AppVals.Action.RESUME_PLAYLIST -> resumePlaylist().also { notifyPlaylist(action) }
                    AppVals.Action.NEXT_PLAYLIST -> nextPlaylist(action)
                    AppVals.Action.PREVIOUS_PLAYLIST -> previousPlaylist(action)
                    AppVals.Action.SHUFFLE_PLAYLIST -> shufflePlaylist().also { notifyPlaylist(action) }
                    AppVals.Action.LOOP_ALL_PLAYLIST -> loopAllPlaylist().also { notifyPlaylist(action) }
                    AppVals.Action.OFF_SHUFFLE_PLAYLIST -> offShufflePlaylist().also { notifyPlaylist(action) }
                    AppVals.Action.OFF_LOOP_ALL_PLAYLIST -> offLoopAllPlaylist().also { notifyPlaylist(action) }
                    AppVals.Action.SEEK_PLAYLIST -> seekPlaylist(getFloatExtra(action, ZERO.toFloat()).toLong())
                    AppVals.Action.STOP_SERVICE -> stopService(startId)
                    else -> Intent()
                }.apply { if (this.action == null) this.action = action }
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initPlaylist(data: MutableList<Mp3>): Intent {
        playlist = data
        return Intent()
    }

    private fun playStream(ytVideo: Video, action: String): Intent {
        streamingSong = ytVideo
        playingSong = null
        mediaPlayer.run {
            if (isPlaying) reset()
            setDataSource(ytVideo.mp3Src)
            prepareAsync()
            if (!isLooping) setOnCompletionListener {
                this@MusikService.stop()
                broadcast.sendBroadcast(Intent(AppVals.Action.STOP))
            }
        }
        notifyStream(action)
        handler.removeCallbacksAndMessages(null)
        handler.post(sendPosTask)
        return Intent().apply { putExtra(action, ytVideo) }
    }

    private fun playPlaylist(mp3: Mp3, action: String): Intent {
        playingSong = mp3
        streamingSong = null
        cursor = playlist.indexOfFirst { it.id == mp3.id }
        mediaPlayer.run {
            when {
                !isLooping && !isShuffle && !isLoopAll -> setOnCompletionListener {
                    normalOnCompletionInPlaylist()
                }
                isShuffle -> shufflePlaylist()
                isLooping -> loopAllPlaylist()
                else -> {
                }
            }
            reset()
            setDataSource(applicationContext, mp3.uri)
            prepareAsync()
        }
        notifyPlaylist(action)
        handler.removeCallbacksAndMessages(null)
        handler.post(sendPosTask)
        return Intent().apply { putExtra(action, mp3) }
    }

    private fun stop(): Intent {
        mediaPlayer.run {
            stop()
            reset()
        }
        handler.removeCallbacksAndMessages(null)
        stopForeground(true)
        return Intent().apply {
            putExtra(action, arrayOf(playingSong, streamingSong))
            playingSong = null
            streamingSong = null
        }
    }

    private fun loopSingle(action: String): Intent {
        isLoopAll = false
        isShuffle = false
        mediaPlayer.run {
            isLooping = true
            setOnCompletionListener {}
        }
        if (streamingSong != null) notifyStream(action)
        return Intent()
    }

    private fun offLoopSingle(action: String): Intent {
        isLoopAll = false
        mediaPlayer.run {
            isLooping = false
            if (streamingSong == null && playingSong != null && !isShuffle && !isLoopAll) setOnCompletionListener {
                normalOnCompletionInPlaylist()
            }
        }
        if (streamingSong != null) notifyStream(action)
        return Intent()
    }

    private fun pausePlaylist(): Intent {
        mediaPlayer.pause()
        handler.removeCallbacksAndMessages(null)
        return Intent()
    }

    private fun resumePlaylist(): Intent {
        mediaPlayer.start()
        handler.post(sendPosTask)
        return Intent()
    }

    private fun nextPlaylist(action: String): Intent {
        cursor =
            if (isShuffle) (ZERO until playlist.size.dec()).random() else if (cursor >= playlist.size.dec()) ZERO else cursor.inc()
        playPlaylist(playlist[cursor], action)
        return Intent(action).apply { putExtra(action, playlist[cursor]) }
    }

    private fun previousPlaylist(action: String): Intent {
        cursor = if (cursor == ZERO) playlist.size.dec() else cursor.dec()
        playPlaylist(playlist[cursor], action)
        return Intent().apply { putExtra(action, playlist[cursor]) }
    }

    private fun shufflePlaylist(): Intent {
        mediaPlayer.isLooping = false
        isShuffle = true
        isLoopAll = false
        mediaPlayer.setOnCompletionListener {
            broadcast.sendBroadcast(nextPlaylist(AppVals.Action.NEXT_PLAYLIST))
        }
        return Intent()
    }

    private fun loopAllPlaylist(): Intent {
        mediaPlayer.isLooping = false
        isShuffle = false
        isLoopAll = true
        mediaPlayer.setOnCompletionListener {
            broadcast.sendBroadcast(nextPlaylist(AppVals.Action.NEXT_PLAYLIST))
        }
        return Intent()
    }

    private fun offShufflePlaylist(): Intent {
        isShuffle = false
        mediaPlayer.run {
            if (!isLooping && !isLoopAll) setOnCompletionListener {
                normalOnCompletionInPlaylist()
            }
        }
        return Intent()
    }

    private fun offLoopAllPlaylist(): Intent {
        isLoopAll = false
        mediaPlayer.run {
            isLooping = false
            if (!isLooping && !isLoopAll) setOnCompletionListener {
                normalOnCompletionInPlaylist()
            }
        }
        return Intent()
    }

    private fun seekPlaylist(pos: Long): Intent {
        mediaPlayer.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                seekTo(pos, MediaPlayer.SEEK_CLOSEST)
            else seekTo(pos.toInt())
            start()
        }
        return Intent()
    }

    private fun stopService(startId: Int): Intent {
        stop()
        stopForeground(true)
        stopSelf(startId)
        return Intent()
    }

    private fun normalOnCompletionInPlaylist() {
        if (cursor >= playlist.size.dec()) this@MusikService.stop()
        else broadcast.sendBroadcast(nextPlaylist(AppVals.Action.NEXT_PLAYLIST))
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun notifyStream(action: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val buttons: Array<Notification.Action> = when (action) {
                AppVals.Action.PLAY_STREAM -> afterRequestPlayStream()
                AppVals.Action.LOOP_SINGLE -> afterRequestLoopSingle()
                AppVals.Action.OFF_LOOP_SINGLE -> afterRequestOffLoopSingle()
                else -> afterRequestStop().filterNotNull().toTypedArray()
            }
            val notification = withContext(Dispatchers.IO) {
                MusikUtils.getBaseNotificationStream(
                    this@MusikService,
                    streamingSong,
                    Notification.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView()
                )!!
            }.apply {
                buttons.forEach { addAction(it) }
            }.build()
            startForeground(FOREGROUND_ID, notification)
        }
    }

    private fun afterRequestPlayStream() =
        streamButtons

    private fun afterRequestStop() =
        arrayOf<Notification.Action?>()

    private fun afterRequestLoopSingle() =
        streamButtons.apply {
            this[ONE] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_filter_1_24,
                AppVals.Action.OFF_LOOP_SINGLE,
                AppVals.Action.OFF_LOOP_SINGLE_CODE
            ).build()
        }

    private fun afterRequestOffLoopSingle() =
        streamButtons.apply {
            this[ONE] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_refresh_24,
                AppVals.Action.LOOP_SINGLE,
                AppVals.Action.OFF_LOOP_SINGLE_CODE
            ).build()
        }

    private fun notifyPlaylist(action: String) {
        val buttons: Array<Notification.Action?> = when (action) {
            AppVals.Action.PLAY_PLAYLIST -> afterRequestPlayPlaylist()
            AppVals.Action.PAUSE_PLAYLIST -> afterRequestPausePlaylist()
            AppVals.Action.RESUME_PLAYLIST -> afterRequestResumePlaylist()
            AppVals.Action.NEXT_PLAYLIST -> afterRequestNextPlaylist()
            AppVals.Action.PREVIOUS_PLAYLIST -> afterRequestPreviousPlaylist()
            AppVals.Action.LOOP_ALL_PLAYLIST -> afterRequestLoopAllPlaylist()
            AppVals.Action.SHUFFLE_PLAYLIST -> afterRequestShufflePlaylist()
            AppVals.Action.OFF_LOOP_ALL_PLAYLIST -> afterRequestOffLoopAllPlaylist()
            AppVals.Action.OFF_SHUFFLE_PLAYLIST -> afterRequestOffShufflePlaylist()
            else -> afterRequestStop()
        }
        val notification = MusikUtils.getBaseNotificationPlaylist(
            this,
            playingSong,
            Notification.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(ONE, TWO, THREE, FIVE),
            contentResolver
        )!!.apply {
            buttons.forEach { if (it != null) addAction(it) }
        }.build()
        startForeground(FOREGROUND_ID, notification)
    }

    private fun afterRequestPlayPlaylist() =
        playListButtons.apply {
            this[TWO] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_pause_24,
                AppVals.Action.PAUSE_PLAYLIST,
                AppVals.Action.PAUSE_PLAYLIST_CODE
            ).build()
        }

    private fun afterRequestPausePlaylist() =
        playListButtons.apply {
            this[TWO] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_play_arrow_24,
                AppVals.Action.RESUME_PLAYLIST,
                AppVals.Action.RESUME_PLAYLIST_CODE
            ).build()
        }

    private fun afterRequestResumePlaylist() = afterRequestPlayPlaylist()

    private fun afterRequestNextPlaylist() = playListButtons

    private fun afterRequestPreviousPlaylist() = playListButtons

    private fun afterRequestLoopAllPlaylist() =
        playListButtons.apply {
            this[FOUR] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_all_inclusive_24,
                AppVals.Action.OFF_LOOP_ALL_PLAYLIST,
                AppVals.Action.OFF_LOOP_ALL_PLAYLIST_CODE
            ).build()
            this[ZERO] = null
        }

    private fun afterRequestShufflePlaylist() =
        playListButtons.apply {
            this[ZERO] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_trending_flat_24,
                AppVals.Action.OFF_SHUFFLE_PLAYLIST,
                AppVals.Action.OFF_SHUFFLE_PLAYLIST_CODE
            ).build()
            this[FOUR] = null
        }

    private fun afterRequestOffLoopAllPlaylist() =
        playListButtons.apply {
            this[FOUR] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_refresh_24,
                AppVals.Action.LOOP_ALL_PLAYLIST,
                AppVals.Action.LOOP_ALL_PLAYLIST_CODE
            ).build()
            this[ZERO] = MusikUtils.buildActionButton(
                this@MusikService,
                R.drawable.ic_baseline_shuffle_24,
                AppVals.Action.SHUFFLE_PLAYLIST,
                AppVals.Action.SHUFFLE_PLAYLIST_CODE
            ).build()
        }

    private fun afterRequestOffShufflePlaylist() = afterRequestOffLoopAllPlaylist()

    private companion object {
        const val FOREGROUND_ID = 1
        const val ZERO = 0
        const val ONE = 1
        const val TWO = 2
        const val THREE = 3
        const val FOUR = 4
        const val FIVE = 5
        const val FACTOR = 1.5
    }
}
