package com.clearpole.videoyou.untils

import android.app.Activity
import android.widget.LinearLayout
import com.gyf.immersionbar.ImmersionBar

class SetBarTransparent {
    companion object {
        fun setBarTransparent(statusBarView: LinearLayout, activity: Activity) {
            ImmersionBar.with(activity).transparentBar().statusBarDarkFont(true)
                .init()
            val statusBarHeight = ImmersionBar.getStatusBarHeight(activity)
            statusBarView.layoutParams.height = statusBarHeight
        }
    }
}