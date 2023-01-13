package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout.LayoutParams
import androidx.annotation.RequiresApi
import com.clearpole.videoyou.code.VideoPlayerGestureListener.Companion.gestureListener
import com.clearpole.videoyou.databinding.ActivityVideoPlayerBinding
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.untils.TimeParse
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import java.lang.Thread.sleep


class VideoPlayer : BaseActivity<ActivityVideoPlayerBinding>() {
    private var isFirstLod = true
    private val upDataValue = 1
    private var stateOfPlay = true
    private var stateOfSlider = false

    companion object {
        const val CURRENT_POSITION = "now"
    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).transparentBar().hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).init()

        mV.videoPlayerTitle.text = VideoObjects.title
        mV.videoPlayerVideoSlider.setLabelFormatter { value: Float ->
            return@setLabelFormatter TimeParse.timeParse(value.toLong()).toString()
        }

        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        mV.videoPlayerPicture.setOnClickListener {
            this.enterPictureInPictureMode()
        }
        mV.videoPlayerScreenControlAll.setOnClickListener {
            setFullScreen(true)
            setBarWeight(3f)
        }
        mV.videoPlayerScreenControlAllEdit.setOnClickListener {
            setFullScreen(false)
            setBarWeight(5f)
        }

        getVideoInfoToSetSuitScreen()
        setHandControl()
    }

    @Suppress("DEPRECATION")
    override fun onUserLeaveHint() {
        this.enterPictureInPictureMode()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode){
            mV.videoPlayerControl.visibility = View.GONE
        }else{
            mV.videoPlayerControl.visibility = View.VISIBLE
        }
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isFirstLod) {
            mV.videoView.setVideoPath(VideoObjects.paths)
            mV.videoView.setOnPreparedListener {
                mV.videoView.start()
                object : Thread() {
                    override fun run() {
                        super.run()
                        upDateUIWhenByProgressChanged()
                    }
                }.start()
            }
            isFirstLod = false
        }
    }

    private fun upDateUIWhenByProgressChanged() = try {
        while (stateOfPlay) {
            val videoView = mV.videoView
            val message = Message.obtain()
            val mBundle = Bundle()
            if (videoView.isPlaying) {
                mBundle.putLong(CURRENT_POSITION, videoView.currentPosition)
                message.what = 1
                message.data = mBundle
                handler.sendMessage(message)
            }
            sleep(500)

        }
    } catch (_: java.lang.Exception) {

    }

    @Suppress("DEPRECATION")
    private val handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val nowDuration = mV.videoPlayerVideoSliderNowText
            val seek = mV.videoPlayerVideoSlider
            if (msg.what == upDataValue) {
                val current = msg.data.getLong(CURRENT_POSITION).toFloat()
                if (current <= mV.videoView.duration && !stateOfSlider) {
                    seek.value = current
                }
                nowDuration.text = TimeParse.timeParse(msg.data.getLong(CURRENT_POSITION))
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setHandControl() {
        gestureListener(context = this, activityBinding = mV, stateOfSliderVal = stateOfSlider)
    }

    private fun getVideoInfoToSetSuitScreen() {
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(VideoObjects.paths)
            val width =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
            val height =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
            val duration =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toFloat()
            mV.videoPlayerVideoSlider.valueTo = duration
            mV.videoPlayerVideoSliderAllText.text = TimeParse.timeParse(duration.toLong())

            if (width!! > height!!) {
                setFullScreen(true)
                setBarWeight(3f)
            } else {
                setFullScreen(false)
                setBarWeight(5f)
            }
        } catch (_: Exception) {

        } finally {
            mmr.release()
        }
    }

    private fun setFullScreen(i: Boolean) {
        if (i) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            mV.videoPlayerScreenControlAllEdit.visibility = View.VISIBLE
            mV.videoPlayerScreenControlAll.visibility = View.GONE
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            mV.videoPlayerScreenControlAllEdit.visibility = View.GONE
            mV.videoPlayerScreenControlAll.visibility = View.VISIBLE
        }
    }

    private fun setBarWeight(f: Float) {
        mV.videoPlayerTopBar.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT, f
        )
        mV.videoPlayerBottomBar.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT, f
        )
    }


}