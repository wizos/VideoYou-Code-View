package com.clearpole.videoyou

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.clearpole.videoyou.adapter.MainPageViewPagerAdapter
import com.clearpole.videoyou.databinding.ActivityMainBinding
import com.clearpole.videoyou.databinding.HistoryItemBinding
import com.clearpole.videoyou.model.FolderModel
import com.clearpole.videoyou.model.FolderModelDad
import com.clearpole.videoyou.model.MainVideoItemModel
import com.clearpole.videoyou.objects.SettingObjects
import com.clearpole.videoyou.objects.VideoObjects
import com.clearpole.videoyou.utils.DatabaseStorage
import com.clearpole.videoyou.utils.GetVideoThumbnail
import com.clearpole.videoyou.utils.ReadMediaStore
import com.clearpole.videoyou.utils.RefreshMediaStore
import com.clearpole.videoyou.utils.SetBarTransparent.Companion.setBarTransparent
import com.clearpole.videoyou.utils.SettingsItemsUntil
import com.drake.brv.item.ItemExpand
import com.drake.brv.layoutmanager.HoverGridLayoutManager
import com.drake.brv.utils.BRV
import com.drake.brv.utils.bindingAdapter
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.hjq.toast.ToastUtils
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshListener
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var isFirstLod = true
    // 是否第一次获取焦点
    private var nowIn = 0
    // 现在在哪个界面[主页:0,设置:1]

    @SuppressLint("InflateParams", "MissingInflatedId", "CutPasteId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingsItemsUntil.initializationItems()
        // 初始化设置项目

        BRV.modelId = BR.model
        // 初始化BRK 绑定Model

        setThemeMode()
        // 设置模式(深色|跟随系统|浅色)

        setBarTransparent(binding.mainPageStatusBar, this,resources)
        // 设置状态栏&导航栏透明

        setTopBar(true)
        // 设置主页顶部工具栏显示

        setNavigationDrawer(true)
        // 设置侧滑栏显示

        setBottomNavigation(true)
        // 设置主页底部导航栏显示

        binding.mainPageSettingLayout.intoSettingTheme.setOnClickListener {
            startActivity(Intent(this, SettingItemsActivity::class.java))
            SettingObjects.name = "主题"
        }
        binding.mainPageSettingLayout.intoSettingCurrency.setOnClickListener {
            startActivity(Intent(this, SettingItemsActivity::class.java))
            SettingObjects.name = "通用"
        }
        binding.mainPageSettingLayout.intoSettingAbout.setOnClickListener {
            startActivity(Intent(this, SettingItemsActivity::class.java))
            SettingObjects.name = "关于"
        }
        // 设置”设置“项目的点击事件

        if (SettingObjects.isClickDarkMode) {
            binding.mainPageViewPager.visibility = View.GONE
            binding.mainPageSetting.visibility = View.VISIBLE
            binding.mainPageBottomNavigation.visibility = View.GONE
            binding.mainPageTopBar.title = "设置"
            nowIn = 1
        }
        intentAction(intent)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (nowIn == 1) {
            animationListener(binding.mainPageViewPager, binding.mainPageSetting, true)
            binding.mainPageTopBar.title = "预播放"
            binding.mainPageViewPager.currentItem = 0
            nowIn = 0
            binding.mainPageNavigationDrawer.close()
            binding.mainPageNavigationDrawerView.setCheckedItem(R.id.toHome)
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("ResourceAsColor")
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (isFirstLod) {
            val inflater = layoutInflater
            val bindingViews = setPageViewer(inflater)
            val refresh = bindingViews[1].findViewById<SwipeRefreshLayout>(R.id.page2_re)
            refresh.setColorSchemeColors(R.color.color5)
            refresh.setOnRefreshListener(object : OnRefreshListener,
                SwipeRefreshLayout.OnRefreshListener {
                override fun onRefresh() {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!DatabaseStorage.writeDataToData(
                                ReadMediaStore.start(
                                    contentResolver
                                )
                            )
                        ) {
                            //ToastUtils.show("软件遇到了意外的错误！\n或者是您的手机没有视频？")
                        }
                        launch(Dispatchers.Main) {
                            setHistoryList(bindingViews)
                            refresh.isRefreshing = false
                        }
                    }
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {}

            })
            val re = bindingViews[2].findViewById<SwipeRefreshLayout>(R.id.page3_re)
            re.setOnRefreshListener(object : OnRefreshListener,
                SwipeRefreshLayout.OnRefreshListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {}

                override fun onRefresh() {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!DatabaseStorage.writeDataToData(
                                ReadMediaStore.start(
                                    contentResolver
                                )
                            )
                        ) {
                            //ToastUtils.show("软件遇到了意外的错误！\n或者是您的手机没有视频？")
                        }
                        launch(Dispatchers.Main) {
                            setPage3FolderList(bindingViews)
                            re.isRefreshing = false
                        }
                    }
                }

            })
            if (checkPermissions(Permission.READ_MEDIA_VIDEO)) {
                setHistoryList(bindingViews = bindingViews)
            } else {
                val permission = Permission.READ_MEDIA_VIDEO
                getPermissions(permission = permission)
            }
            val ycKv = MMKV.defaultMMKV()
            if (ycKv.decodeString("isFirst")=="true"){
            }else{
                firstInto(true,bindingViews,ycKv)
            }
            setPage3FolderList(bindingViews)

            isFirstLod = false
        }
    }

    private fun setPage3FolderList(bindingViews: ArrayList<View>) {
        val rv = bindingViews[2].findViewById<RecyclerView>(R.id.page3_rv)
        val layoutManager = HoverGridLayoutManager(this, 2)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position < 0) return 1
                return when (rv.bindingAdapter.getItemViewType(position)) {
                    R.layout.main_page2_folder_item -> 1
                    else -> 2
                }
            }
        }
        rv.layoutManager = layoutManager
        rv.setup {
            addType<FolderModelDad>(R.layout.main_page2_folder_item2_dad)
            addType<FolderModel>(R.layout.main_page2_folder_item)
            R.id.folder_item_dad.onFastClick {
                when (itemViewType) {
                    R.layout.main_page2_folder_item2_dad -> {
                        if (getModel<ItemExpand>().itemExpand) {
                            expandOrCollapse()
                        } else {
                            expandOrCollapse()
                        }
                    }
                }
            }
            R.id.folder_item.onFastClick {
                VideoObjects.paths = getModel<FolderModel>(layoutPosition).path
                VideoObjects.title = getModel<FolderModel>(layoutPosition).title
                VideoObjects.type = "LOCAL"
                val intent = Intent(this@MainActivity, VideoPlayer::class.java)
                startActivity(intent)
            }
        }.models = getData(ReadMediaStore.getFolder(contentResolver))
    }

    @SuppressLint("ResourceType")
    private fun getData(kv: ArrayList<String>): MutableList<FolderModelDad> {
        return mutableListOf<FolderModelDad>().apply {
            for (index in 0 until kv.size) {
                val json = DatabaseStorage.readDataByData()
                add(
                    FolderModelDad(
                        contentResolver,
                        kv[index],
                        json,
                        Drawable.createFromXml(
                            resources,
                            resources.getXml(R.drawable.baseline_keyboard_arrow_down_24)
                        ),
                        Drawable.createFromXml(
                            resources,
                            resources.getXml(R.drawable.baseline_keyboard_arrow_right_24)
                        )
                    )
                )
            }
        }
    }

    private fun setTopBar(isActivation: Boolean) {
        if (isActivation) {
            binding.mainPageTopBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.intoDev -> {
                        startActivity(Intent(this, DevelopActivity::class.java))
                        true
                    }

                    R.id.refreshMedia -> {
                        DatabaseStorage.clearData(contentResolver)
                        RefreshMediaStore.updateMedia(
                            this@MainActivity,
                            Environment.getExternalStorageDirectory().toString()
                        )
                        ToastUtils.show("已通知系统尝试刷新！\n这将需要数秒的时间\n请耐心等待...")
                        true
                    }

                    else -> false
                }
            }
        }
    }

    @SuppressLint("CutPasteId", "SetTextI18n")
    private fun setHistoryList(bindingViews: ArrayList<View>) {
        CoroutineScope(Dispatchers.IO).launch {
            val models = getDataForHistory(DatabaseStorage.readDataByData())
            val isRipple = SettingsItemsUntil.readSettingData("isRipple").toBoolean()
            launch(Dispatchers.Main) {
                bindingViews[1].findViewById<RecyclerView>(R.id.page2_rv).linear().setup {
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
                        if (!isRipple) {
                            binding.historyItemRoot.background = null
                        }
                        binding.historyItemRoot.setOnClickListener {
                            VideoObjects.paths = getModel<MainVideoItemModel>(layoutPosition).path
                            VideoObjects.title = getModel<MainVideoItemModel>(layoutPosition).title
                            VideoObjects.type = "LOCAL"
                            val intent = Intent(this@MainActivity, VideoPlayer::class.java)
                            startActivity(intent)
                        }
                    }
                }.models = models
                binding.mainPageNavigationDrawerView.getHeaderView(0)
                    .findViewById<TextView>(R.id.header_title).text =
                    "您的设备\n共有${bindingViews[1].findViewById<RecyclerView>(R.id.page2_rv)?.adapter?.itemCount}个视频"
            }
        }
    }

    private fun setBottomNavigation(isActivation: Boolean) {
        if (isActivation) {
            binding.mainPageBottomNavigationView.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.page1 -> {
                        binding.mainPageViewPager.currentItem = 0
                        true
                    }

                    R.id.page2 -> {
                        binding.mainPageViewPager.currentItem = 1
                        true
                    }

                    R.id.page3 -> {
                        binding.mainPageViewPager.currentItem = 3
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
        val bindingViews = ArrayList<View>()
        bindingViews.add(page1)
        bindingViews.add(page2)
        bindingViews.add(page3)
        binding.mainPageViewPager.adapter = MainPageViewPagerAdapter(bindingViews)

        binding.mainPageViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.mainPageBottomNavigationView.selectedItemId = R.id.page1
                        binding.mainPageTopBar.title = "预播放"
                    }

                    1 -> {
                        binding.mainPageBottomNavigationView.selectedItemId = R.id.page2
                        binding.mainPageTopBar.title = "媒体库"
                    }

                    2 -> {
                        binding.mainPageBottomNavigationView.selectedItemId = R.id.page3
                        binding.mainPageTopBar.title = "文件夹"
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        }
        )
        return bindingViews
    }

    private fun setNavigationDrawer(isActivation: Boolean) {
        if (isActivation) {
            binding.mainPageTopBar.setNavigationOnClickListener {
                binding.mainPageNavigationDrawer.open()
            }
            binding.mainPageNavigationDrawerView.setCheckedItem(R.id.toHome)
            binding.mainPageNavigationDrawerView.setNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.toHome -> {
                        animationListener(binding.mainPageViewPager, binding.mainPageSetting, true)
                        binding.mainPageTopBar.title = "预播放"
                        nowIn = 0
                        binding.mainPageNavigationDrawer.close()
                        menuItem.isChecked = true
                    }

                    R.id.toSettings -> {
                        animationListener(binding.mainPageSetting, binding.mainPageViewPager, false)
                        binding.mainPageTopBar.title = "设置"
                        nowIn = 1
                        binding.mainPageNavigationDrawer.close()
                        menuItem.isChecked = true
                    }

                    R.id.toSearch -> {
                        startActivity(Intent(this, SearchActivity::class.java))
                    }

                    R.id.toInternet -> {
                        val view = layoutInflater.inflate(R.layout.internet_dev_edit, null)
                        MaterialAlertDialogBuilder(
                            this@MainActivity,
                            com.google.android.material.R.style.MaterialAlertDialog_Material3
                        )
                            .setTitle("输入链接")
                            .setView(view)
                            .setNegativeButton("开看！") { _, _ ->
                                VideoObjects.type = "INTERNET"
                                VideoObjects.title = "网络视频"
                                VideoObjects.paths =
                                    view.findViewById<TextInputEditText>(R.id.dev_internet).text.toString()
                                startActivity(Intent(this, VideoPlayer::class.java))
                            }
                            .show()
                    }

                    else -> {
                        ToastUtils.show(menuItem.itemId)
                    }
                }
                true
            }
            binding.mainPageNavigationDrawerView.getHeaderView(0)
                .findViewById<ImageView>(R.id.header_back).setOnClickListener {
                    binding.mainPageNavigationDrawer.close()
                }
        }
    }

    @SuppressLint("InflateParams")
    private fun animationListener(viewIn: View, viewOut: View, bottomBar: Boolean) {
        val slateAnimaRightSlideIn = TranslateAnimation(
            1000f, -0f, 0f, 0f
        )
        slateAnimaRightSlideIn.duration = 200

        val slateAnimaLeftSlideOut = TranslateAnimation(
            0f, -1000f, 0f, 0f
        )
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

        if (bottomBar) {
            binding.mainPageBottomNavigation.visibility = View.VISIBLE
            binding.mainPageBottomNavigation.startAnimation(slateAnimaBottomSlideIn)
        } else {
            binding.mainPageBottomNavigation.startAnimation(slateAnimaBottomSlideOut)
            binding.mainPageBottomNavigation.visibility = View.GONE
        }
    }


    private fun checkPermissions(permission: String): Boolean {
        return XXPermissions.isGranted(this, permission)
    }

    private fun getPermissions(permission: String) {
        XXPermissions.with(this)
            .permission(permission)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (!allGranted) {
                        ToastUtils.show("获取部分权限成功，但部分权限未正常授予")
                        return
                    }
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

    @Suppress("DEPRECATION")
    private fun firstInto(isBoolean: Boolean, bindingViews: ArrayList<View>, ycKv:MMKV){
        if (isBoolean){
            val view = layoutInflater.inflate(R.layout.app_is_activation, null)
            val tV = view.findViewById<TextView>(R.id.is_activation)
            tV.movementMethod = LinkMovementMethod.getInstance()
            tV.text =
                Html.fromHtml("<a href='https://clearpole.gitee.io/video-you/privacy-agreement/index.html'>前往阅读隐私协议</a>")
            MaterialAlertDialogBuilder(
                this@MainActivity,
                com.google.android.material.R.style.MaterialAlertDialog_Material3
            )
                .setTitle("隐私协议")
                .setCancelable(false)
                .setMessage("您需要阅读并同意隐私协议，否则将无法使用软件。")
                .setView(view)
                .setNegativeButton("不同意") { _, _ ->
                    finish()
                }
                .setPositiveButton("我已知晓并承诺遵循"){_,_ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        ycKv.encode("isFirst","true")
                        if (!DatabaseStorage.writeDataToData(
                                ReadMediaStore.start(
                                    contentResolver
                                )
                            )
                        ) {
                        }
                        launch(Dispatchers.Main) {
                            setHistoryList(bindingViews)
                            setPage3FolderList(bindingViews)
                        }
                    }
                }
                .show()
        }
    }

    private fun getDataForHistory(kv: JSONArray): MutableList<Any> {
        return mutableListOf<Any>().apply {
            for (index in 0 until kv.length()) {
                val jsonObject = JSONObject(kv.getString(index))
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

    private fun setThemeMode(){
        when (SettingsItemsUntil.readSettingData("darkMode")?.toInt()!!) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            intentAction(intent)
        }
    }

    private fun intentAction(intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_VIEW != action) {
            return
        }
        val data = intent.data!!.path ?: return
        VideoObjects.paths = data
        VideoObjects.type = "LOCAL"
        VideoObjects.title = data
        startActivity(Intent(this,VideoPlayer::class.java))
    }
}
