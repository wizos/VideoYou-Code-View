package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import cn.jzvd.JzvdStd
import com.clearpole.videoyou.code.VideoPlayerGestureListener
import com.clearpole.videoyou.code.VideoPlayerIjk
import com.clearpole.videoyou.databinding.ActivityVideoPlayerBinding
import com.clearpole.videoyou.model.VideoModel
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.objects.VideoObjects.Companion.paths
import com.clearpole.videoyou.objects.VideoObjects.Companion.type
import com.clearpole.videoyou.objects.VideoPlayerObjects
import com.clearpole.videoyou.utils.SettingsItemsUntil
import com.clearpole.videoyou.utils.TimeParse.Companion.timeParse
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import com.hjq.toast.ToastUtils
import kotlinx.coroutines.CoroutineScope 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Thread.sleep


class VideoPlayer : AppCompatActivity() {
    @SuppressLint("ResourceType")
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityVideoPlayerBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_video_player)
        binding.lifecycleOwner = this
        binding.videoModel = VideoModel()
        ImmersionBar.with(this).transparentBar().hideBar(BarHide.FLAG_HIDE_NAVIGATION_BAR).init()
        // 沉浸状态栏
        VideoModel.videoTitle = VideoObjects.title
        // 设置标题
        binding.videoPlayerBottomBarRoot.videoPlayerVideoSlider.setLabelFormatter { value: Float ->
            return@setLabelFormatter timeParse(value.toLong()).toString()
        }
        // 设置拖动条标签文本
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // 设置播放器全屏
        binding.videoPlayerBottomBarRoot.videoPlayerPauseRoot.setOnClickListener {
            if (!binding.videoView.isPlaying){
                binding.videoView.start()
                binding.videoModel?.pauseImg = Drawable.createFromXml(resources,resources.getXml(R.drawable.baseline_pause_24))
            }else{
                binding.videoView.pause()
                binding.videoModel?.pauseImg = Drawable.createFromXml(resources,resources.getXml(R.drawable.baseline_play_arrow_24))
            }
        }
        // 设置播放/暂停
        binding.videoPlayerBottomBarRoot.videoPlayerScreenRoot.setOnClickListener {
            if (VideoPlayerObjects.isInFullScreen){
                VideoPlayerObjects.isInFullScreen = false
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                binding.videoModel?.screenImg = Drawable.createFromXml(resources,resources.getXml(R.drawable.baseline_fullscreen_24))
            }else{
                VideoPlayerObjects.isInFullScreen = true
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                binding.videoModel?.screenImg = Drawable.createFromXml(resources,resources.getXml(R.drawable.baseline_fullscreen_exit_24))
            }
        }
        binding.jz.setUp(paths,"1",JzvdStd.SCREEN_FULLSCREEN,VideoPlayerIjk::class.java)
        // 设置全屏/取消全屏
        binding.videoPlayerBottomBarRoot.videoPlayerPictureRoot.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.enterPictureInPictureMode()
            }else{
                ToastUtils.show("您的系统版本不支持画中画")
            }
        }
        // 设置画中画
        try {
            when (type) {
                "LOCAL" -> {
                    binding.videoView.setVideoPath(paths)
                    // 如果是本地就载入本地视频路径
                }

                "INTERNET" -> {
                    binding.videoView.setVideoURI(Uri.parse(paths))
                    // 如果是网络视频就载入网络
                }
            }
        } catch (e: Exception) {
            ToastUtils.show(e.message)
            // 捕获错误
        } finally {
            //binding.videoView.start()
            // 开始播放视频
            binding.videoView.setOnPreparedListener {
                // 视频准备完毕之后
                VideoPlayerGestureListener.gestureListener(this, binding,resources)
                // 开启手势监听
                binding.videoModel?.allProgressString =
                    timeParse(binding.videoView.duration).toString()
                // 全部时长
                binding.videoModel?.allProgressFloat = binding.videoView.duration.toFloat()
                // 全部时长
                binding.videoModel?.pauseImg = Drawable.createFromXml(resources,resources.getXml(R.drawable.baseline_pause_24))
                // 设置pause icon
                binding.videoModel?.screenImg = Drawable.createFromXml(resources,resources.getXml(R.drawable.baseline_fullscreen_24))
                // 设置screen control icon
                CoroutineScope(Dispatchers.IO).launch {
                    // 开启协程
                    /*while (true) {
                        val nowProgress = binding.videoView.currentPosition
                        if (binding.videoView.isPlaying) {
                            if (!VideoPlayerObjects.isMove) {
                                binding.videoModel?.nowProgressString =
                                    timeParse(nowProgress).toString()
                                binding.videoModel?.nowProgressLong = nowProgress
                            }
                            sleep(500)
                        }else if (binding.videoView.duration - nowProgress < 50){
                            finish()
                        }
                    }*/
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onUserLeaveHint() {
        if (SettingsItemsUntil.readSettingData("isAutoPicture").toBoolean()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.enterPictureInPictureMode()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode){
            findViewById<RelativeLayout>(R.id.video_player_control_root).visibility = View.GONE
        }else{
            findViewById<RelativeLayout>(R.id.video_player_control_root).visibility = View.VISIBLE
        }
    }

}