package com.clearpole.videoyou

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.dylanc.viewbinding.base.ViewBindingUtil
import com.google.android.material.color.DynamicColors
import com.hjq.toast.ToastUtils
import com.tencent.mmkv.MMKV

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    lateinit var mV: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtils.init(this.application)
        DynamicColors.applyToActivityIfAvailable(this)
        mV = ViewBindingUtil.inflateWithGeneric(this, layoutInflater)
        MMKV.initialize(this)
        setContentView(mV.root)
    }
}