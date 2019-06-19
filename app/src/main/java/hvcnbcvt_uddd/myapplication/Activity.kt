package hvcnbcvt_uddd.myapplication

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.widget.FrameLayout
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
import kotlinx.android.synthetic.main.exo_playback_control_view.*

class Activity : AppCompatActivity() {
    private val STATE_RESUME_WINDOW = "resumeWindow"
    private val STATE_RESUME_POSITION = "resumePosition"
    private val STATE_PLAYER_FULLSCREEN = "playerFullscreen"

    private var mExoPlayerView: SimpleExoPlayerView? = null
    private var mVideoSource: MediaSource? = null
    private var mExoPlayerFullscreen = false
    private lateinit var mFullScreenDialog: Dialog

    private var mResumeWindow: Int = 0
    private var mResumePosition: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
        }
    }


    public override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow)
        outState.putLong(STATE_RESUME_POSITION, mResumePosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen)

        super.onSaveInstanceState(outState)
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
        (mExoPlayerView!!.parent as ViewGroup).removeView(mExoPlayerView)
        mFullScreenDialog.addContentView(
            mExoPlayerView!!,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
//        exo_fullscreen_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen_skrink))
        mExoPlayerFullscreen = true
        mFullScreenDialog.show()
    }


    private fun closeFullscreenDialog() {

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        (mExoPlayerView!!.parent as ViewGroup).removeView(mExoPlayerView)
        (findViewById(R.id.main_media_frame) as FrameLayout).addView(mExoPlayerView)
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

//        val controlView = mExoPlayerView!!.findViewById(R.id.exo_controller)
//        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon)
//        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button)
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
        mExoPlayerView!!.player = player

        val haveResumePosition = mResumeWindow != C.INDEX_UNSET

        if (haveResumePosition) {
            mExoPlayerView!!.player.seekTo(mResumeWindow, mResumePosition)
        }

        mExoPlayerView!!.player.prepare(mVideoSource)
        mExoPlayerView!!.player.playWhenReady = true
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
            (mExoPlayerView!!.parent as ViewGroup).removeView(mExoPlayerView)
            mFullScreenDialog.addContentView(
                mExoPlayerView!!,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
//            exo_fullscreen_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen_skrink))
            mFullScreenDialog.show()
        }
    }


    override fun onPause() {

        super.onPause()

        if (mExoPlayerView != null && mExoPlayerView!!.player != null) {
            mResumeWindow = mExoPlayerView!!.player.currentWindowIndex
            mResumePosition = Math.max(0, mExoPlayerView!!.player.contentPosition)

            mExoPlayerView!!.player.release()
        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss()
    }

}