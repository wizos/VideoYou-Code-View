package com.clearpole.videoyou.utils

import android.content.res.Configuration
import android.content.res.Resources

class IsNightMode {
    companion object{
        fun isNightMode(resources: Resources): Boolean {
            return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
        }
    }
}