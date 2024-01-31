package com.bhanit.stageexoplayer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bhanit.stageexoplayer.databinding.StageExoPlayerBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.ExoTrackSelection
import com.google.android.exoplayer2.util.MimeTypes

class StageExoPlayer : AppCompatActivity() {
    private val TAG = "StageExoPlayer"
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        StageExoPlayerBinding.inflate(layoutInflater)
    }
    private val HSL_URL =
        "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"

    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpFullScreen()
        setContentView(viewBinding.root)
        setUpPlayer()
        addMediaItem(HSL_URL)
        hideSystemUI()
    }

    override fun onResume() {
        super.onResume()
        viewBinding.player.player?.play()
    }

    override fun onPause() {
        super.onPause()
        viewBinding.player.player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding.player.player?.stop()
        viewBinding.player.player?.release()
    }

    private fun setUpPlayer() {
        Log.d(TAG, "setUpPlayer: ")
        val trackSelector = getDefaultTrackController()
        val loadControl = getLoadController()
        initExoPlayer(trackSelector, loadControl)
        setUpAudioAttributes()
        viewBinding.player.player = player
        preSetUI()
    }

    //initializing exoplayer
    private fun initExoPlayer(
        trackSelector: DefaultTrackSelector,
        loadControl: DefaultLoadControl,
    ) {
        Log.d(TAG, "initExoPlayer: ")
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector)
            .setLoadControl(loadControl).build()
    }

    //set up audio attributes
    private fun setUpAudioAttributes() {
        Log.d(TAG, "setUpAudioAttributes: ")
        val audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build()
        player.setAudioAttributes(audioAttributes, false)
    }

    // pre handle player ui
    private fun preSetUI() {
        Log.d(TAG, "preSetUI: ")
        //hiding all the ui StyledPlayerView comes with
        viewBinding.player.setShowNextButton(false)
        viewBinding.player.setShowPreviousButton(false)
        //setting the scaling mode to scale to fit
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
    }

    private fun getLoadController(): DefaultLoadControl {
        return DefaultLoadControl.Builder().setBufferDurationsMs(
            DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
            DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
            DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
        ).build()
    }

    private fun getDefaultTrackController(): DefaultTrackSelector {
        val trackSelectionFactory: ExoTrackSelection.Factory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, trackSelectionFactory)
        trackSelector.parameters = trackSelector.buildUponParameters()
            .setPreferredAudioLanguage("hi")  // Prefer Hindi audio
            .build()
        return trackSelector
    }

    // will add hsl url to play
    private fun addMediaItem(mediaUrl: String) {
        //Creating a media item of HLS Type
        val mediaItem = MediaItem.Builder().setUri(mediaUrl)
            .setMimeType(MimeTypes.APPLICATION_M3U8) //m3u8 is the extension used with HLS sources
            .build()
        player.setMediaItem(mediaItem)
        player.prepare()
        getVideoQualityTrack()
        player.repeatMode = Player.REPEAT_MODE_ONE //repeating the video from start after it's over
        player.play()
    }

    // this method will gets the available resolution of the the video, currently not working, need to find the correct way with updated version of com.google.android.exoplayer:exoplayer:2.19.1
    private fun getVideoQualityTrack() {
        val tracks = player.currentTracks
        if (tracks.groups.isEmpty()) {
            Log.d(TAG, "getVideoQualityTrack:tracks group is empty ")
            Toast.makeText(this, "tracks group is empty", Toast.LENGTH_SHORT).show()
            return
        }
        for (trackGroup in tracks.groups) {
            // Group level information.
            val trackType = trackGroup.type
            val trackInGroupIsSelected = trackGroup.isSelected
            val trackInGroupIsSupported = trackGroup.isSupported
            Log.d(
                "BHANIT",
                "getVideoQualityTrack:trackType $trackType trackInGroupIsSelected $trackInGroupIsSelected  trackInGroupIsSupported $trackInGroupIsSupported"
            )
            for (i in 0 until trackGroup.length) {
                // Individual track information.
                val isSupported = trackGroup.isTrackSupported(i)
                val isSelected = trackGroup.isTrackSelected(i)
                val trackFormat = trackGroup.getTrackFormat(i)

                Log.d(
                    "BHANIT",
                    "getVideoQualityTrack: isSupported $isSupported isSelected $isSelected trackFormat $trackFormat"
                )
            }
        }
    }

    /*Method will setUp screen in full view*/
    private fun setUpFullScreen() {
        Log.d(TAG, "setUpFullScreen: ")
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
    }

}



