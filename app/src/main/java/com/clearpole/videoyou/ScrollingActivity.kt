package com.clearpole.videoyou

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import com.clearpole.videoyou.databinding.ActivityScrollingBinding
import com.clearpole.videoyou.untils.IsNightMode
import com.gyf.immersionbar.ImmersionBar
import com.hjq.toast.ToastUtils
import com.tencent.mmkv.MMKV

class ScrollingActivity : BaseActivity<ActivityScrollingBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).transparentBar().statusBarDarkFont(!IsNightMode.isNightMode(resources)).init()
        val canClick = false
        val kv = MMKV.mmkvWithID("dev")
        if (canClick){
            mV.devMmkvWrite.setOnClickListener {
                val isWrite = kv.encode(mV.devMmkvEditKey.text.toString(), mV.devMmkvEditValue.text.toString())
                if (isWrite){
                    ToastUtils.showShort("写入成功")
                }else{
                    ToastUtils.showShort("写入失败")
                }
            }
            mV.devMmkvRead.setOnClickListener {
                val value = kv.decodeString(mV.devMmkvEditKey.text.toString())
                ToastUtils.show(value)
            }
            mV.devMmkvRemove.setOnClickListener {
                kv.removeValueForKey(mV.devMmkvEditKey.text.toString())
                ToastUtils.showShort("已尝试删除")
            }
            mV.devMmkvRemoveAll.setOnClickListener {
                val isRemove = getSharedPreferences("dev", MODE_PRIVATE).edit().clear().commit()
                if (isRemove){
                    ToastUtils.showShort("删除成功")
                }else{
                    ToastUtils.showShort("删除失败")
                }
            }
        }
        mV.devBlurOpen.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mV.devBlur.setRenderEffect(RenderEffect.createBlurEffect(8F, 8F, Shader.TileMode.CLAMP))
            }else{
                ToastUtils.showShort("您的设备版本小于安卓12/不是安卓设备\n开启失败")
            }
        }
        mV.devBlurClose.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mV.devBlur.setRenderEffect(null)
            }
        }
    }
}