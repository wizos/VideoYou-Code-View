package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.clearpole.videoyou.databinding.ActivitySearchBinding
import com.clearpole.videoyou.databinding.HistoryItemBinding
import com.clearpole.videoyou.model.MainVideoItemModel
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.untils.DatabaseStorage
import com.clearpole.videoyou.untils.GetVideoThumbnail
import com.clearpole.videoyou.untils.IsNightMode
import com.clearpole.videoyou.untils.SettingsItemsUntil
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class SearchActivity : BaseActivity<ActivitySearchBinding>(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       ImmersionBar.with(this).transparentBar().statusBarDarkFont(!IsNightMode.isNightMode(resources)).init()
        binding.catSearchView.editText.setOnEditorActionListener { v, _, _ ->
            binding.catSearchBar.text = v.text
            setSearchList(v.text.toString())
            binding.catSearchView.hide()
            false
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun setSearchList(key:String) {
        CoroutineScope(Dispatchers.IO).launch {
            val models = getDataForSearch(key,DatabaseStorage.readDataByData())
            val isRipple = SettingsItemsUntil.readSettingData("isRipple").toBoolean()
            launch(Dispatchers.Main) {
                binding.searchRv.linear().setup {
                    addType<MainVideoItemModel> { R.layout.history_item }
                    onBind {
                        val binding = HistoryItemBinding.bind(itemView)
                        binding.mainHistoryItemName.text =
                            getModel<MainVideoItemModel>(layoutPosition).title
                        binding.mainHistoryItemSize.text =
                            getModel<MainVideoItemModel>(layoutPosition).size
                        binding.historyItemCover.setImageBitmap(
                            getModel<MainVideoItemModel>(
                                layoutPosition
                            ).img
                        )
                        binding.historyItemRoot.setOnClickListener {
                            VideoObjects.paths = getModel<MainVideoItemModel>(layoutPosition).path
                            VideoObjects.title = getModel<MainVideoItemModel>(layoutPosition).title
                            VideoObjects.type = "LOCAL"
                            val intent = Intent(this@SearchActivity, VideoPlayer::class.java)
                            startActivity(intent)
                        }
                        if (!isRipple) {
                            binding.historyItemRoot.background = null
                        }
                    }
                }.models = models
            }
        }
    }
    private fun getDataForSearch(key: String,kv: JSONArray): MutableList<Any> {
        return mutableListOf<Any>().apply {
            for (index in 0 until kv.length()) {
                val jsonObject = JSONObject(kv.getString(index))
                if (jsonObject.getString("title").contains(key)) {
                    add(
                        MainVideoItemModel(
                            title = jsonObject.getString("title"),
                            size = jsonObject.getString("size"),
                            img = GetVideoThumbnail.getVideoThumbnail(
                                contentResolver,
                                Uri.parse(jsonObject.getString("uri"))
                            ),
                            path = jsonObject.getString("path")
                        )
                    )
                }
            }
        }
    }

}