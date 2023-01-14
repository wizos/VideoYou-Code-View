package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.clearpole.videoyou.adapter.MainPageViewPagerAdapter
import com.clearpole.videoyou.databinding.ActivityMainBinding
import com.clearpole.videoyou.databinding.HistoryItemBinding
import com.clearpole.videoyou.model.HistoryModel
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.untils.ByteToString
import com.clearpole.videoyou.untils.GetVideoThumbnail
import com.clearpole.videoyou.untils.SetBarTransparent.Companion.setBarTransparent
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils


class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var isFirstLod = true

    @SuppressLint("InflateParams", "MissingInflatedId", "CutPasteId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtils.init(this.application)
        //ToastUtils.setView(R.layout.toast_view)

        setBarTransparent(statusBarView = mV.mainPageStatusBar, activity = this, resources = resources)
        setTopBar(isActivation = true)
        setNavigationDrawer(isActivation = true)
        setBottomNavigation(isActivation = true)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isFirstLod) {
            val inflater = layoutInflater
            val mViews = setPageViewer(inflater)
            if (checkPermissions(Permission.READ_MEDIA_VIDEO)) {
                setHistoryList(mViews = mViews)
            } else {
                val permission = Permission.READ_MEDIA_VIDEO
                getPermissions(permission = permission, mViews = mViews)
            }
            isFirstLod = false
        }
    }

    private fun setTopBar(isActivation: Boolean) {
        if (isActivation) {
            mV.mainPageTopBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.upDate -> {
                        startActivity(Intent(this,ScrollingActivity::class.java))
                        true
                    }

                    else -> false
                }
            }
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun setHistoryList(mViews: ArrayList<View>) {
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
                mViews[0].findViewById<RecyclerView>(R.id.page1_rv).linear().setup {
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
                            val intent = Intent(this@MainActivity, VideoPlayer::class.java)
                            startActivity(intent)
                        }
                    }
                }.models = getDataForHistory(
                    cursor,
                    indexVideoId,
                    indexVideoSize,
                    indexVideoTitle,
                    indexVideoPath
                )
                mV.mainPageNavigationDrawerView.getHeaderView(0)
                    .findViewById<TextView>(R.id.header_title).text =
                    "您的设备\n共有${mViews[0].findViewById<RecyclerView>(R.id.page1_rv)?.adapter?.itemCount}个视频"
            }
        }.start()
    }

    private fun setBottomNavigation(isActivation: Boolean) {
        if (isActivation) {
            mV.mainPageBottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.page1 -> {
                        mV.mainPageViewPager.currentItem = 0
                        true
                    }

                    R.id.page2 -> {
                        mV.mainPageViewPager.currentItem = 1
                        true
                    }

                    R.id.page3 -> {
                        mV.mainPageViewPager.currentItem = 3
                        true
                    }

                    else -> false
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    fun setPageViewer(inflater: LayoutInflater): ArrayList<View> {
        val page1 = inflater.inflate(R.layout.main_navigation_page1, null)
        val page2 = inflater.inflate(R.layout.main_navigation_page2, null)
        val page3 = inflater.inflate(R.layout.main_navigation_page3, null)
        val mViews = ArrayList<View>()
        mViews.add(page1)
        mViews.add(page2)
        mViews.add(page3)
        mV.mainPageViewPager.adapter = MainPageViewPagerAdapter(mViews)

        mV.mainPageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        mV.mainPageBottomNavigationView.selectedItemId = R.id.page1
                        mV.mainPageTopBar.title = "历史播放"
                    }

                    1 -> {
                        mV.mainPageBottomNavigationView.selectedItemId = R.id.page2
                        mV.mainPageTopBar.title = "文件夹"
                    }

                    2 -> {
                        mV.mainPageBottomNavigationView.selectedItemId = R.id.page3
                        mV.mainPageTopBar.title = "媒体库"
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        }
        )
        return mViews
    }

    private fun setNavigationDrawer(isActivation: Boolean) {

        var nowIn = 0

        if (isActivation) {
            mV.mainPageTopBar.setNavigationOnClickListener {
                mV.mainPageNavigationDrawer.open()
            }
            mV.mainPageNavigationDrawerView.setCheckedItem(R.id.toHome)
            mV.mainPageNavigationDrawerView.setNavigationItemSelectedListener { menuItem ->
                when(menuItem.itemId){
                    R.id.toHome -> {
                        animationListener(mV.mainPageViewPager,mV.mainPageSetting,true)
                        mV.mainPageTopBar.title = "历史播放"
                        nowIn = 0
                        mV.mainPageNavigationDrawer.close()
                        menuItem.isChecked = true
                    }
                    R.id.toSettings -> {
                        animationListener(mV.mainPageSetting,mV.mainPageViewPager,false)
                        mV.mainPageTopBar.title = "设置"
                        nowIn = 1
                        mV.mainPageNavigationDrawer.close()
                        menuItem.isChecked = true
                    }
                    R.id.toSearch -> {
                        startActivity(Intent(this, SearchActivity::class.java))
                    }
                    else -> {
                        ToastUtils.show(menuItem.itemId)
                    }
                }
                true
            }
            mV.mainPageNavigationDrawerView.getHeaderView(0)
                .findViewById<ImageView>(R.id.header_back).setOnClickListener {
                    mV.mainPageNavigationDrawer.close()
                }
        }
    }

    private fun animationListener(viewIn: View,viewOut: View,bottomBar: Boolean){
        val slateAnimaRightSlideIn = TranslateAnimation(
            1000f, -0f, 0f, 0f)
        slateAnimaRightSlideIn.duration = 200

        val slateAnimaLeftSlideOut = TranslateAnimation(
            0f, -1000f, 0f, 0f)
        slateAnimaLeftSlideOut.duration = 200

        val slateAnimaBottomSlideIn = AnimationUtils.loadAnimation(
            this,
            com.google.android.material.R.anim.abc_slide_in_bottom
        )
        //slateAnimaBottomSlideIn.duration = 150L
        val slateAnimaBottomSlideOut = AnimationUtils.loadAnimation(
            this,
            com.google.android.material.R.anim.abc_slide_out_bottom
        )
        //slateAnimaBottomSlideOut.duration = 150L

        viewIn.visibility = View.VISIBLE
        viewIn.startAnimation(slateAnimaRightSlideIn)
        viewOut.startAnimation(slateAnimaLeftSlideOut)
        viewOut.visibility = View.GONE

        if (bottomBar){
            mV.mainPageBottomNavigation.visibility = View.VISIBLE
            mV.mainPageBottomNavigation.startAnimation(slateAnimaBottomSlideIn)
        }else{
            mV.mainPageBottomNavigation.startAnimation(slateAnimaBottomSlideOut)
            mV.mainPageBottomNavigation.visibility = View.GONE
        }
    }


    private fun checkPermissions(permission: String): Boolean {
        return XXPermissions.isGranted(this, permission)
    }

    @Suppress("DEPRECATION")
    private fun getPermissions(permission: String, mViews: ArrayList<View>) {
        XXPermissions.with(this)
            .permission(permission)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        ToastUtils.show("获取部分权限成功，但部分权限未正常授予")
                        return
                    }
                    val view = layoutInflater.inflate(R.layout.app_is_activation, null)
                    val tV = view.findViewById<TextView>(R.id.is_activation)
                    tV.movementMethod = LinkMovementMethod.getInstance()
                    tV.text = Html.fromHtml("<a href='https://ys.mihoyo.com/'>前往阅读隐私协议</a>")

                    MaterialAlertDialogBuilder(
                        this@MainActivity,
                        com.google.android.material.R.style.MaterialAlertDialog_Material3
                    )
                        .setTitle("权限获取成功")
                        .setCancelable(false)
                        .setMessage("权限申请成功，但您需要阅读并同意隐私协议，否则将无法使用软件。")
                        .setView(view)
                        .setNegativeButton("我已知晓并承诺遵循") { _, _ ->
                            setHistoryList(mViews)
                        }
                        .show()
                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    if (doNotAskAgain) {
                        ToastUtils.show("请授权读取视频文件权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        ToastUtils.show("获取读取视频文件权限失败")
                    }
                }
            })
    }

    private fun getDataForHistory(
        cursor: Cursor?,
        indexVideoId: Int?,
        indexVideoSize: Int?,
        indexVideoTitle: Int?,
        indexVideoPath: Int?,
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
                add(HistoryModel(title, subTitle, img, path))
            }
        }
    }

}
