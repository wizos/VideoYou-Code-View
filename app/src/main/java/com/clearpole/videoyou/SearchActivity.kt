package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.clearpole.videoyou.databinding.ActivitySearchBinding
import com.clearpole.videoyou.databinding.HistoryItemBinding
import com.clearpole.videoyou.model.HistoryModel
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.untils.ByteToString
import com.clearpole.videoyou.untils.GetVideoThumbnail
import com.clearpole.videoyou.untils.IsNightMode
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.gyf.immersionbar.ImmersionBar


class SearchActivity : BaseActivity<ActivitySearchBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       ImmersionBar.with(this).transparentBar().statusBarDarkFont(!IsNightMode.isNightMode(resources)).init()
        mV.catSearchView.editText.setOnEditorActionListener { v, _, _ ->
            mV.catSearchBar.text = v.text
            setSearchList(v.text.toString())
            mV.catSearchView.hide()
            false
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun setSearchList(key:String) {
        Thread {
            val cursor: Cursor? = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )
            val indexVideoId = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val indexVideoSize = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val indexVideoTitle = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val indexVideoPath = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            runOnUiThread {
                mV.searchRv.linear().setup {
                    addType<HistoryModel> { R.layout.history_item }
                    onBind {
                        val binding = HistoryItemBinding.bind(itemView)
                        binding.mainHistoryItemName.text =
                            getModel<HistoryModel>(layoutPosition).title
                        binding.mainHistoryItemSize.text =
                            getModel<HistoryModel>(layoutPosition).size
                        binding.historyItemCover.setImageBitmap(
                            getModel<HistoryModel>(
                                layoutPosition
                            ).img
                        )
                        binding.historyItemRoot.setOnClickListener {
                            VideoObjects.paths = getModel<HistoryModel>(layoutPosition).path
                            VideoObjects.title = getModel<HistoryModel>(layoutPosition).title
                            val intent = Intent(this@SearchActivity, VideoPlayer::class.java)
                            startActivity(intent)
                        }
                    }
                }.models = getDataForSearch(
                    cursor,
                    indexVideoId,
                    indexVideoSize,
                    indexVideoTitle,
                    indexVideoPath,
                    key
                )
            }
        }.start()
    }
    private fun getDataForSearch(
        cursor: Cursor?,
        indexVideoId: Int?,
        indexVideoSize: Int?,
        indexVideoTitle: Int?,
        indexVideoPath: Int?,
        key: String
    ): MutableList<Any> {
        return mutableListOf<Any>().apply {
            while (cursor?.moveToNext() == true) {
                val title = cursor.getString(indexVideoTitle!!)
                val subTitle =
                    ByteToString.byteToString(cursor.getString(indexVideoSize!!).toLong())
                val path = cursor.getString(indexVideoPath!!)
                val videoUri = Uri.withAppendedPath(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    cursor.getString(indexVideoId!!)
                )
                val img = GetVideoThumbnail.getVideoThumbnail(cr = contentResolver, uri = videoUri)
                if (title.contains(key)) {
                    add(HistoryModel(title, subTitle, img, path))
                }
            }
        }
    }

}