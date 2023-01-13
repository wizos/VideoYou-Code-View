package com.clearpole.videoyou.code

import android.annotation.SuppressLint
import android.content.Context
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.clearpole.videoyou.R
import com.clearpole.videoyou.databinding.ActivityVideoPlayerBinding
import com.clearpole.videoyou.untils.BaseClickListener
import com.clearpole.videoyou.untils.TimeParse.Companion.timeParse
import com.google.android.material.slider.Slider

class VideoPlayerGestureListener {
    companion object {
        private var stateOfPlayerMove = false

        @Suppress("DEPRECATION", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        fun gestureListener(
            context: Context,
            activityBinding: ActivityVideoPlayerBinding,
            stateOfSliderVal: Boolean,
        ) {

            var stateOfSlider = stateOfSliderVal

            val slateAnimBsSlideIn = AnimationUtils.loadAnimation(
                context,
                com.google.android.material.R.anim.design_bottom_sheet_slide_in
            )
            val slateAnimBsSlideOut = AnimationUtils.loadAnimation(
                context,
                com.google.android.material.R.anim.design_bottom_sheet_slide_out
            )
            val slateAnimaTopSlideIn = AnimationUtils.loadAnimation(
                context,
                com.google.android.material.R.anim.abc_slide_in_top
            )
            slateAnimaTopSlideIn.duration = 150L
            val slateAnimaTopSlideOut = AnimationUtils.loadAnimation(
                context,
                com.google.android.material.R.anim.abc_slide_out_top
            )
            slateAnimaTopSlideOut.duration = 150L
            val slateAnimaBottomSlideIn = AnimationUtils.loadAnimation(
                context,
                com.google.android.material.R.anim.abc_slide_in_bottom
            )
            slateAnimaBottomSlideIn.duration = 150L
            val slateAnimaBottomSlideOut = AnimationUtils.loadAnimation(
                context,
                com.google.android.material.R.anim.abc_slide_out_bottom
            )
            slateAnimaBottomSlideOut.duration = 150L

            var isLongClickMode = false
            // 是否处于长按模式
            var isSpeedMode = false
            // 是否处于倍速模式
            var isOpenToolBar = false
            // 上下工具栏是否被打开
            var isPlayMode = true
            // 是否处于播放状态
            var isMoveMode = false
            // 是否处于手指移动状态
            var firstX = 0f
            // 手指刚接触屏幕的x轴位置
            var isImplement = false
            // 手指刚接触屏幕的事件执行情况
            var stateOfScroll = "暂未设置(LEFT|RIGHT)"
            // 滑动的状态：左或右
            var newProgressLong = 0L
            // 想要调整到的视频进度

            //设置进度条拖动事件
            activityBinding.videoPlayerVideoSlider.addOnSliderTouchListener(object :
                Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    stateOfSlider = true
                    //开始拖动的时候设置变量，暂停进度条跟随线程变化进度
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    activityBinding.videoView.seekTo(slider.value.toLong())
                    //结束拖动，将视频进度设置为进度条进度
                    stateOfSlider = false
                    //还原变量，进度条继续跟随线程变化
                }

            })

            val vibrator = context.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
            // 申请振动器

            // 本方法涵盖：双击屏幕事件，单击屏幕事件
            activityBinding.videoPlayerControl.setOnClickListener(object : BaseClickListener() {
                override fun onSingleClick(v: View?) {
                    // 单击屏幕，判断是否打开工具栏，执行操作
                    if (!isOpenToolBar) {
                        // 如果没打开工具栏，执行打开
                        activityBinding.videoPlayerTopBar.visibility = View.VISIBLE
                        activityBinding.videoPlayerBottomBar.visibility = View.VISIBLE
                        activityBinding.videoPlayerTopBar.startAnimation(slateAnimaTopSlideIn)
                        activityBinding.videoPlayerBottomBar.startAnimation(slateAnimaBottomSlideIn)
                        isOpenToolBar = true
                    } else {
                        // 如果打开了工具栏，执行关闭
                        activityBinding.videoPlayerTopBar.startAnimation(slateAnimaTopSlideOut)
                        activityBinding.videoPlayerBottomBar.startAnimation(slateAnimaBottomSlideOut)
                        activityBinding.videoPlayerTopBar.visibility = View.GONE
                        activityBinding.videoPlayerBottomBar.visibility = View.GONE
                        isOpenToolBar = false
                    }
                }

                override fun onDoubleClick(v: View?) {
                    // 双击屏幕，判断是否处于播放状态
                    if (isPlayMode) {
                        // 如果正在播放，就暂停视频播放
                        Glide.with(context).load(R.drawable.baseline_play_arrow_24)
                            .into(activityBinding.isPlayPause)
                        activityBinding.isPlayPauseRoot.visibility = View.VISIBLE
                        activityBinding.isPlayPauseRoot.startAnimation(slateAnimBsSlideIn)
                        activityBinding.videoView.pause()
                        isPlayMode = false
                    } else {
                        // 如果本来就是暂停播放，就开始播放
                        Glide.with(context).load(R.drawable.baseline_pause_24)
                            .into(activityBinding.isPlayPause)
                        activityBinding.isPlayPauseRoot.startAnimation(slateAnimBsSlideOut)
                        activityBinding.isPlayPauseRoot.visibility = View.GONE
                        activityBinding.videoView.start()
                        isPlayMode = true
                    }
                }

            })

            // 本方法涵盖：长按屏幕事件
            activityBinding.videoPlayerControl.setOnLongClickListener {
                // 长按屏幕，判断是否处于倍速模式
                if (!isSpeedMode && !stateOfPlayerMove) {
                    // 如果目前不是倍速模式，就启动倍速，震动手机一次并隐藏上下工具栏
                    isSpeedMode = true
                    vibrator.vibrate(30)
                    activityBinding.videoPlayerTopBar.startAnimation(slateAnimaTopSlideOut)
                    activityBinding.videoPlayerBottomBar.startAnimation(slateAnimaBottomSlideOut)
                    activityBinding.videoPlayerTopBar.visibility = View.GONE
                    activityBinding.videoPlayerBottomBar.visibility = View.GONE
                    activityBinding.videoPlayer2x.visibility = View.VISIBLE
                    activityBinding.videoPlayer2x.startAnimation(slateAnimBsSlideIn)
                    activityBinding.videoView.playbackSpeed = 2.0f
                    isLongClickMode = true
                    // 声明：现在处于长按状态
                    isOpenToolBar = false
                    // 声明：工具栏已被隐藏
                }
                true
            }

            //此方法涵盖：左右滑动调整进度
            activityBinding.videoPlayerControl.setOnTouchListener { _, event ->
                // 当手指刚接触的屏幕
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (!isImplement) {
                        isImplement = true
                        firstX = event.x
                        // 存储刚刚接触屏幕的X轴位置
                    }
                }
                // 当手指在屏幕上移动
                if (event.action == MotionEvent.ACTION_MOVE) {
                    val nowPosition = activityBinding.videoView.currentPosition
                    // 目前视频播放进度（Long形式）
                    val nowTime = timeParse(nowPosition)
                    // 目前视频播放进度（时间形式）
                    val newPosition = nowPosition + (event.x.toLong() - firstX.toLong()) * 20
                    // 目前想要调整的视频播放进度：目前视频的播放进度+(现在手指在屏幕上的位置-第一次手指在屏幕上的位置)×20
                    val newTime = timeParse(newPosition)
                    // 目前想要调整的视频播放进度（时间形式）
                    if (stateOfScroll == "LEFT") {
                        // 如果是向左滑动
                        // 向左滑动，左边文本显示想要调整到的时间，右边文本显示现在播放的时间
                        activityBinding.playProgressLeft.text = newTime
                        activityBinding.playProgressRight.text = nowTime
                        if (newTime!!.contains("-")) {
                            // 如果想要调整到的时间超过逻辑（负数），就返回00:00
                            activityBinding.playProgressLeft.text = "00:00"
                            newProgressLong = 0L
                            // 调整到视频0进度
                        } else if (newPosition > activityBinding.videoView.duration) {
                            // 如果想要调整到的地方是大于视频总长度的，不符合逻辑，返回视频最后处
                            activityBinding.playProgressLeft.text =
                                timeParse(activityBinding.videoView.duration)
                            newProgressLong = activityBinding.videoView.duration
                            // 调整到视频最大进度
                        } else {
                            // 不符合以上两种特殊情况，则为正常情况
                            activityBinding.playProgressLeft.text = newTime
                            newProgressLong = newPosition
                            // 调整到想要调整的进度
                        }
                    } else {
                        // 如果是向右滑动，和上面原理一样，不做解释
                        activityBinding.playProgressLeft.text = nowTime
                        activityBinding.playProgressRight.text = newTime
                        if (newTime!!.contains("-")) {
                            activityBinding.playProgressRight.text = "00:00"
                            newProgressLong = 0
                        } else if (newPosition > activityBinding.videoView.duration) {
                            activityBinding.playProgressRight.text =
                                timeParse(activityBinding.videoView.duration)
                            newProgressLong = activityBinding.videoView.duration
                        } else {
                            activityBinding.playProgressRight.text = newTime
                            newProgressLong = newPosition
                        }
                    }

                    if (newPosition > nowPosition) {
                        // 如果想要调整的进度大于目前正在播放的进度，则手指正在向右滑动
                        activityBinding.playProgressTo.text = "->"
                        stateOfScroll = "RIGHT"
                    } else {
                        // 如果想要调整的进度小于目前正在播放的进度，则手指正在向左滑动
                        activityBinding.playProgressTo.text = "<-"
                        stateOfScroll = "LEFT"
                    }

                    if (!isMoveMode && !isSpeedMode) {
                        // 如果手指不处于移动状态且不在进行倍速播放，就显示调整进度的信息框架
                        isMoveMode = true
                        activityBinding.playProgressRoot.visibility = View.VISIBLE
                        activityBinding.playProgressTo.text = "->"
                        stateOfPlayerMove = true
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    // 手指离开屏幕执行的事件
                    if (isMoveMode) {
                        activityBinding.playProgressRoot.visibility = View.GONE
                        isMoveMode = false
                        stateOfPlayerMove = false
                        isImplement = false
                        activityBinding.videoView.seekTo(newProgressLong)
                        // 离开屏幕后将视频调整到欲调整的位置
                    }
                    if (isLongClickMode) {
                        // 如果正在倍速，离开屏幕就停止倍速
                        activityBinding.videoPlayer2x.startAnimation(slateAnimBsSlideOut)
                        activityBinding.videoPlayer2x.visibility = View.GONE
                        activityBinding.videoView.playbackSpeed = 1.0f
                        isSpeedMode = false
                        isLongClickMode = false
                    }
                }
                false
            }
        }
    }
}