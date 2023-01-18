package com.clearpole.videoyou.model

import android.graphics.Bitmap
import com.drake.brv.BindingAdapter
import com.drake.brv.item.ItemBind

data class MainVideoItemModel(val title: String, val size: String, val img: Bitmap?, val path: String) :
    ItemBind {
    override fun onBind(holder: BindingAdapter.BindingViewHolder) {
    }
}