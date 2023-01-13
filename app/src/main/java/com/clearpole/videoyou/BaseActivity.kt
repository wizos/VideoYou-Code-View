package com.clearpole.videoyou

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.dylanc.viewbinding.base.ViewBindingUtil
import com.google.android.material.color.DynamicColors

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    lateinit var mV: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)
        mV = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        setContentView(mV.root)
    }
}