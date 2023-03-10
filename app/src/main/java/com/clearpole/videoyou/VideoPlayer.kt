package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.blankj.utilcode.util.EncodeUtils.base64Encode
import com.clearpole.videoyou.code.VideoPlayerGestureListener
import com.clearpole.videoyou.databinding.ActivityVideoPlayerBinding
import com.clearpole.videoyou.model.VideoModel
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.objects.VideoPlayerObjects
import com.clearpole.videoyou.utils.SettingsItemsUntil
import com.clearpole.videoyou.utils.TimeParse.Companion.timeParse
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Thread.sleep


class VideoPlayer : AppCompatActivity() {
    private lateinit var player: ExoPlayer

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceType", "LongLogTag", "MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityVideoPlayerBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        binding.lifecycleOwner = this
        binding.videoModel = VideoModel()
        ImmersionBar.with(this).transparentBar().hideBar(BarHide.FLAG_HIDE_BAR).init()
        // ???????????????
        VideoModel.videoTitle = VideoObjects.title
        // ????????????
        binding.videoPlayerBottomBarRoot.videoPlayerVideoSlider.setLabelFormatter { value: Float ->
            return@setLabelFormatter timeParse(value.toLong()).toString()
        }
        // ???????????????????????????
        VideoPlayerObjects.isAutoFinish = false
        binding.videoPlayerBottomBarRoot.videoPlayerPauseRoot.setOnClickListener {
            if (!binding.videoView.player!!.isPlaying) {
                val draw = Drawable.createFromXml(
                    resources,
                    resources.getXml(R.drawable.baseline_pause_24)
                )
                binding.videoView.player?.play()
                binding.videoModel?.pauseImg = draw
                binding.videoPlayerAssemblyRoot.isPlayPauseRoot.visibility = View.GONE
                binding.videoPlayerAssemblyRoot.isPlayPause.setImageDrawable(draw)
            } else {
                binding.videoView.player?.pause()
                val draw = Drawable.createFromXml(
                    resources,
                    resources.getXml(R.drawable.baseline_play_arrow_24)
                )
                binding.videoPlayerAssemblyRoot.isPlayPauseRoot.visibility = View.VISIBLE
                binding.videoPlayerAssemblyRoot.isPlayPause.setImageDrawable(draw)
                binding.videoModel?.pauseImg = draw
            }
        }
        // ????????????/??????
        binding.videoPlayerBottomBarRoot.videoPlayerScreenRoot.setOnClickListener {
            if (VideoPlayerObjects.isInFullScreen) {
                this.window.clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                // ?????????????????????
                VideoPlayerObjects.isInFullScreen = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                binding.videoModel?.screenImg = Drawable.createFromXml(
                    resources,
                    resources.getXml(R.drawable.baseline_fullscreen_24)
                )
                val param = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    4.0f
                )
                binding.videoPlayerTopRoot.layoutParams = param
                binding.videoPlayerBottomRoot.layoutParams = param
            } else {
                VideoPlayerObjects.isInFullScreen = true
                this.window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                // ?????????????????????
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                binding.videoModel?.screenImg = Drawable.createFromXml(
                    resources,
                    resources.getXml(R.drawable.baseline_fullscreen_exit_24)
                )
                val param = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2.0f
                )
                binding.videoPlayerTopRoot.layoutParams = param
                binding.videoPlayerBottomRoot.layoutParams = param
            }
        }
        // ????????????/????????????
        binding.videoPlayerBottomBarRoot.videoPlayerPictureRoot.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val builder = PictureInPictureParams.Builder()
                val rational =
                    Rational(VideoPlayerObjects.videoWidth, VideoPlayerObjects.videoHeight)
                builder.setAspectRatio(rational)
                this.enterPictureInPictureMode(builder.build())
            } else {
                ToastUtils.show("????????????????????????????????????")
            }
        }
        // ???????????????
        try {
            player = ExoPlayer.Builder(this).build()
            if (intent.getStringExtra("webPath").isNullOrEmpty()) {
                binding.videoView.player = player
                when (VideoObjects.type) {
                    "LOCAL" -> {
                        player.addMediaItem(MediaItem.fromUri(VideoObjects.paths))
                        // ??????????????????????????????????????????
                    }

                    "INTERNET" -> {
                        player.addMediaItem(MediaItem.fromUri(Uri.parse(VideoObjects.paths)))
                        // ??????????????????????????????????????????
                    }
                }
            } else {
                val username = intent.getStringExtra("username")
                val password = intent.getStringExtra("password")
                val webPath = intent.getStringExtra("webPath")

                val httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                val dataSourceFactory = DataSource.Factory {
                    val dataSource = httpDataSourceFactory.createDataSource()
                    dataSource.setRequestProperty("Authorization",
                        "Basic "+ base64Encode("$username:$password").decodeToString()
                    )
                    dataSource
                }
                player = ExoPlayer.Builder(this)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                    .build().apply {
                        setMediaItem(MediaItem.fromUri(webPath.toString()))
                        prepare()
                    }
                binding.videoView.player = player
            }
        } catch (e: Exception) {
            ToastUtils.show(e.message)
            finish()
            // ??????????????????????????????
        } finally {
            player.prepare()
            var isFirst = true
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_READY -> {
                            if (VideoPlayerObjects.videoWidth == 0 || VideoPlayerObjects.videoHeight != player.videoSize.height) {
                                VideoPlayerObjects.videoHeight = player.videoSize.height
                                VideoPlayerObjects.videoWidth = player.videoSize.width
                            }
                            if (isFirst) {
                                player.playWhenReady = true
                                VideoPlayerGestureListener.gestureListener(
                                    this@VideoPlayer,
                                    binding,
                                    resources
                                )
                                // ??????????????????
                                binding.videoModel?.allProgressString =
                                    timeParse(player.duration).toString()
                                // ????????????
                                binding.videoModel?.allProgressFloat = player.duration.toFloat()
                                // ????????????
                                binding.videoModel?.pauseImg =
                                    Drawable.createFromXml(
                                        resources,
                                        resources.getXml(R.drawable.baseline_pause_24)
                                    )
                                // ??????pause icon
                                binding.videoModel?.screenImg = Drawable.createFromXml(
                                    resources,
                                    resources.getXml(R.drawable.baseline_fullscreen_24)
                                )
                                val allProgress = player.duration
                                // ??????screen control icon
                                CoroutineScope(Dispatchers.IO).launch {
                                    //????????????
                                    var nowProgress = 0L
                                    while (true) {
                                        withContext(Dispatchers.Main) {
                                            nowProgress = player.currentPosition
                                        }
                                        if (!VideoPlayerObjects.isMove && nowProgress <= allProgress) {
                                            binding.videoModel?.nowProgressString =
                                                timeParse(nowProgress).toString()
                                            binding.videoModel?.nowProgressLong = nowProgress
                                        }
                                        sleep(500)
                                    }
                                }
                                isFirst = false
                            }
                            binding.videoPlayerAssemblyRoot.isPlayLodRoot.visibility = View.GONE
                        }

                        Player.STATE_BUFFERING -> {
                            binding.videoPlayerAssemblyRoot.isPlayLodRoot.visibility = View.VISIBLE
                        }

                        Player.STATE_ENDED -> {
                            VideoPlayerObjects.isAutoFinish = true
                            player.release()
                            finish()
                        }

                        Player.STATE_IDLE -> {

                        }
                    }
                }
            })
        }
    }

    fun headers(): DataSource.Factory? {
        val headersMap: MutableMap<String, String> = HashMap()
        headersMap["Authorization"] = "Basic YWRtaW46MzQxMjI2"
        headersMap["Accept"] = "..."
        return DefaultHttpDataSource.Factory().setDefaultRequestProperties(headersMap)
            .setUserAgent("UA")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun finish() {
        if (!VideoPlayerObjects.isAutoFinish) {
            MaterialAlertDialogBuilder(
                this,
                com.google.android.material.R.style.MaterialAlertDialog_Material3_Title_Text_CenterStacked
            )
                .setTitle("????????????")
                .setMessage("????????????????????????????????????????????????")
                .setPositiveButton("??????") { _, _ -> }
                .setNegativeButton("????????????") { _, _ ->
                    Log.w("????????????", VideoPlayerObjects.videoWidth.toString())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        val builder = PictureInPictureParams.Builder()
                        val rational =
                            Rational(VideoPlayerObjects.videoWidth, VideoPlayerObjects.videoHeight)
                        builder.setAspectRatio(rational)
                        this.enterPictureInPictureMode(builder.build())
                    } else {
                        ToastUtils.show("????????????????????????????????????")
                    }
                }
                .setNeutralButton("????????????") { _, _ ->
                    player.release()
                    VideoPlayerObjects.isFirstLod = true
                    VideoPlayerObjects.isAutoFinish = true
                    super.finish()
                }
                .show()
        } else {
            VideoPlayerObjects.isAutoFinish = true
            super.finish()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        if (SettingsItemsUntil.readSettingData("isAutoPicture").toBoolean()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val builder = PictureInPictureParams.Builder()
                val rational =
                    Rational(VideoPlayerObjects.videoWidth, VideoPlayerObjects.videoHeight)
                builder.setAspectRatio(rational)
                this.enterPictureInPictureMode(builder.build())
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            findViewById<RelativeLayout>(R.id.video_player_control_root).visibility = View.GONE
        } else {
            findViewById<RelativeLayout>(R.id.video_player_control_root).visibility = View.VISIBLE
        }
    }

}