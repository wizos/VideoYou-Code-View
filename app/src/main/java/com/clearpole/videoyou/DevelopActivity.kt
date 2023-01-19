package com.clearpole.videoyou

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import com.clearpole.videoyou.databinding.ActivityDevelopBinding
import com.clearpole.videoyou.utils.IsNightMode
import com.gyf.immersionbar.ImmersionBar
import com.hjq.toast.ToastUtils
import com.tencent.mmkv.MMKV

class DevelopActivity : BaseActivity<ActivityDevelopBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).transparentBar().statusBarDarkFont(!IsNightMode.isNightMode(resources)).init()
        val canClick = false
        val kv = MMKV.mmkvWithID("dev")
        if (canClick){
            binding.devMmkvWrite.setOnClickListener {
                val isWrite = kv.encode(binding.devMmkvEditKey.text.toString(), binding.devMmkvEditValue.text.toString())
                if (isWrite){
                    ToastUtils.showShort("写入成功")
                }else{
                    ToastUtils.showShort("写入失败")
                }
            }
            binding.devMmkvRead.setOnClickListener {
                val value = kv.decodeString(binding.devMmkvEditKey.text.toString())
                ToastUtils.show(value)
            }
            binding.devMmkvRemove.setOnClickListener {
                kv.removeValueForKey(binding.devMmkvEditKey.text.toString())
                ToastUtils.showShort("已尝试删除")
            }
            binding.devMmkvRemoveAll.setOnClickListener {
                val isRemove = getSharedPreferences("dev", MODE_PRIVATE).edit().clear().commit()
                if (isRemove){
                    ToastUtils.showShort("删除成功")
                }else{
                    ToastUtils.showShort("删除失败")
                }
            }
        }
        binding.devBlurOpen.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding.devBlur.setRenderEffect(RenderEffect.createBlurEffect(8F, 8F, Shader.TileMode.CLAMP))
            }else{
                ToastUtils.showShort("您的设备版本小于安卓12/不是安卓设备\n开启失败")
            }
        }
        binding.devBlurClose.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                binding.devBlur.setRenderEffect(null)
            }
        }
    }
}