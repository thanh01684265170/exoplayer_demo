package hvcnbcvt_uddd.myapplication

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*

class Activity : AppCompatActivity() {
    private var mExoPlayerView: SimpleExoPlayerView? = null
    private var mVideoSource: MediaSource? = null
    private var mExoPlayerFullscreen = false
    private lateinit var mFullScreenDialog: Dialog

    private var mResumeWindow: Int = 0
    private var mResumePosition: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun initFullscreenDialog() {
        mFullScreenDialog = object : Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog()
                super.onBackPressed()
            }
        }
    }


    private fun openFullscreenDialog() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        (exo_player.parent as ViewGroup).removeView(exo_player)
        mFullScreenDialog.addContentView(
            exo_player,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        mExoPlayerFullscreen = true
        mFullScreenDialog.show()
    }


    private fun closeFullscreenDialog() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        (mExoPlayerView?.parent as ViewGroup).removeView(mExoPlayerView)
        main_media_frame.addView(mExoPlayerView)
        mExoPlayerFullscreen = false
        mFullScreenDialog.dismiss()
        exo_fullscreen_icon.setImageDrawable(
            ContextCompat.getDrawable(
                this@Activity,
                R.drawable.ic_fullscreen_expand
            )
        )
    }


    private fun initFullscreenButton() {
        exo_fullscreen_button.setOnClickListener {
            if (!mExoPlayerFullscreen)
                openFullscreenDialog()
            else
                closeFullscreenDialog()
        }
    }


    private fun initExoPlayer() {
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val loadControl = DefaultLoadControl()
        val player = ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(this), trackSelector, loadControl)
        exo_player.player = player
        val haveResumePosition = mResumeWindow != C.INDEX_UNSET
        if (haveResumePosition) {
            exo_player.player.seekTo(mResumeWindow, mResumePosition)
        }
        exo_player.player.prepare(mVideoSource)
        exo_player.player.playWhenReady = false
    }


    override fun onResume() {
        super.onResume()
        if (mExoPlayerView == null) {

            mExoPlayerView = findViewById(R.id.exo_player) as SimpleExoPlayerView

            initFullscreenDialog()
            initFullscreenButton()

            val streamUrl = "http://qthttp.apple.com.edgesuite.net/1010qwoeiuryfg/sl.m3u8"
            val userAgent = Util.getUserAgent(this@Activity, applicationContext.applicationInfo.packageName)
            val httpDataSourceFactory = DefaultHttpDataSourceFactory(
                userAgent,
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
            )
            val dataSourceFactory = DefaultDataSourceFactory(this@Activity, null, httpDataSourceFactory)
            val daUri = Uri.parse(streamUrl)

            mVideoSource = HlsMediaSource(daUri, dataSourceFactory, 1, null, null)
        }

        initExoPlayer()

        if (mExoPlayerFullscreen) {
            (exo_player.parent as ViewGroup).removeView(exo_player)
            mFullScreenDialog.addContentView(
                exo_player,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            mFullScreenDialog.show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (exo_player != null && exo_player.player != null) {
            mResumeWindow = exo_player.player.currentWindowIndex
            mResumePosition = Math.max(0, exo_player.player.contentPosition)

            exo_player.player.release()
        }
            mFullScreenDialog.dismiss()
    }

}