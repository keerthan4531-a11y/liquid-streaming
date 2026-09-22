package com.cybersec.liquidstream.ui.screens.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import com.cybersec.liquidstream.PipManager
import com.cybersec.liquidstream.core.network.NetworkClient
import com.cybersec.liquidstream.data.model.StreamSource
import com.cybersec.liquidstream.data.repository.WatchHistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AspectRatioMode(val label: String, val resizeMode: Int) {
    FIT("Fit (16:9)", AspectRatioFrameLayout.RESIZE_MODE_FIT),
    ZOOM("Zoom / Crop", AspectRatioFrameLayout.RESIZE_MODE_ZOOM),
    FILL("Stretch (Fill)", AspectRatioFrameLayout.RESIZE_MODE_FILL)
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val areControlsVisible: Boolean = true,
    val streamSource: StreamSource? = null,
    val isBuffering: Boolean = true,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isScreenLocked: Boolean = false,
    val quickToastText: String? = null
)

class PlayerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var isInitialized = false
    private var watchHistoryRepository: WatchHistoryRepository? = null
    private var toastJob: Job? = null

    fun initializePlayer(
        context: Context,
        stream: StreamSource,
        initialPositionMs: Long = 0L
    ) {
        if (isInitialized) return
        isInitialized = true

        watchHistoryRepository = WatchHistoryRepository.getInstance(context)
        val resumePos = if (initialPositionMs > 0) {
            initialPositionMs
        } else {
            watchHistoryRepository?.getSavedPosition(stream.title) ?: 0L
        }

        _uiState.value = _uiState.value.copy(
            streamSource = stream,
            isBuffering = true,
            hasError = false
        )

        try {
            val streamUrl = if (stream.directStreamUrl.isNotBlank()) {
                stream.directStreamUrl
            } else {
                "https://www.w3schools.com/html/mov_bbb.mp4"
            }

            // Build OkHttp data source with the EXACT headers from reverse engineering
            val headers = stream.streamHeaders.toMutableMap()
            val userAgent = headers.remove("User-Agent") ?: "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

            // Only send upstream Referer when streaming from reverse-engineered upstream domains (uptodub.ch, onestream, downloadpage, moviespage, etc.)
            val isUpstreamCdn = streamUrl.contains("uptodub.ch") || streamUrl.contains("onestream.today") ||
                    streamUrl.contains("downloadpage.xyz") || streamUrl.contains("moviespage.xyz") ||
                    streamUrl.contains("dubmv.xyz") || streamUrl.contains("dubpage.xyz") ||
                    streamUrl.contains("isaidub.dad") || streamUrl.contains("dubshare.one")
            if (!isUpstreamCdn) {
                headers.remove("Referer")
            }

            val okHttpFactory = OkHttpDataSource.Factory(NetworkClient.mediaOkHttpClient)
                .setUserAgent(userAgent)
                .setDefaultRequestProperties(headers)

            val player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(DefaultMediaSourceFactory(okHttpFactory))
                .build()

            val mediaItem = MediaItem.fromUri(streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()

            if (resumePos > 3000L) {
                player.seekTo(resumePos)
                showQuickToast("Resumed from ${fmtTime(resumePos)}")
            }

            player.playWhenReady = true
            PipManager.isVideoActive = true

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    val duration = player.duration.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        durationMs = duration
                    )
                    if (playbackState == Player.STATE_READY) {
                        saveCurrentProgress()
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    PipManager.isVideoActive = isPlaying
                    if (!isPlaying) {
                        saveCurrentProgress()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        hasError = true,
                        errorMessage = "Playback error: ${error.localizedMessage ?: "Unknown error"}\nStream: ${stream.directStreamUrl.take(80)}..."
                    )
                }
            })

            exoPlayer = player
            startPositionTracker()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isBuffering = false,
                hasError = true,
                errorMessage = "Init error: ${e.localizedMessage}"
            )
        }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    private fun startPositionTracker() {
        viewModelScope.launch {
            var tick = 0
            while (true) {
                exoPlayer?.let { p ->
                    val pos = p.currentPosition.coerceAtLeast(0L)
                    val dur = p.duration.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        bufferedPositionMs = p.bufferedPosition.coerceAtLeast(0L)
                    )

                    // Auto-save every 4 seconds (8 ticks)
                    tick++
                    if (tick % 8 == 0 && dur > 0 && pos > 1000) {
                        saveCurrentProgress()
                    }
                }
                delay(500)
            }
        }
    }

    fun saveCurrentProgress() {
        val p = exoPlayer ?: return
        val stream = _uiState.value.streamSource ?: return
        val pos = p.currentPosition.coerceAtLeast(0L)
        val dur = p.duration.coerceAtLeast(0L)
        if (dur > 0 && pos > 1000) {
            watchHistoryRepository?.saveProgress(
                movieId = stream.title,
                title = stream.title,
                posterUrl = "", // handled by repository if empty
                detailUrl = stream.watchOnlinePageUrl,
                positionMs = pos,
                durationMs = dur
            )
        }
    }

    fun togglePlayPause() {
        exoPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        saveCurrentProgress()
    }

    fun seekRelative(deltaMs: Long) {
        exoPlayer?.let {
            val target = (it.currentPosition + deltaMs).coerceIn(0L, it.duration.coerceAtLeast(0L))
            it.seekTo(target)
            saveCurrentProgress()
        }
    }

    fun skipForward() {
        seekRelative(10_000L)
    }

    fun skipBackward() {
        seekRelative(-10_000L)
    }

    fun cycleAspectRatio() {
        val current = _uiState.value.aspectRatioMode
        val next = when (current) {
            AspectRatioMode.FIT -> AspectRatioMode.ZOOM
            AspectRatioMode.ZOOM -> AspectRatioMode.FILL
            AspectRatioMode.FILL -> AspectRatioMode.FIT
        }
        _uiState.value = _uiState.value.copy(aspectRatioMode = next)
        showQuickToast("Aspect Ratio: ${next.label}")
    }

    fun setScreenLocked(locked: Boolean) {
        _uiState.value = _uiState.value.copy(
            isScreenLocked = locked,
            areControlsVisible = !locked
        )
        if (locked) {
            showQuickToast("Screen Locked")
        } else {
            showQuickToast("Screen Unlocked")
        }
    }

    fun cycleSpeed() {
        val speeds = listOf(1.0f, 1.25f, 1.5f, 2.0f, 0.75f)
        val cur = _uiState.value.playbackSpeed
        val idx = speeds.indexOf(cur)
        val next = speeds[(if (idx == -1) 0 else idx + 1) % speeds.size]
        exoPlayer?.setPlaybackSpeed(next)
        _uiState.value = _uiState.value.copy(playbackSpeed = next)
        showQuickToast("Speed: ${next}x")
    }

    fun toggleControls() {
        if (_uiState.value.isScreenLocked) return
        _uiState.value = _uiState.value.copy(areControlsVisible = !_uiState.value.areControlsVisible)
    }

    fun setControlsVisible(visible: Boolean) {
        if (_uiState.value.isScreenLocked && visible) return
        _uiState.value = _uiState.value.copy(areControlsVisible = visible)
    }

    fun showQuickToast(message: String) {
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(quickToastText = message)
            delay(1500)
            if (_uiState.value.quickToastText == message) {
                _uiState.value = _uiState.value.copy(quickToastText = null)
            }
        }
    }

    fun releasePlayer() {
        saveCurrentProgress()
        PipManager.isVideoActive = false
        exoPlayer?.release()
        exoPlayer = null
        isInitialized = false
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    private fun fmtTime(ms: Long): String {
        val s = ms / 1000
        val h = s / 3600; val m = (s % 3600) / 60; val sec = s % 60
        return if (h > 0) "%02d:%02d:%02d".format(h, m, sec) else "%02d:%02d".format(m, sec)
    }
}
