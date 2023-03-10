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
import com.clearpole.videoyou.model.WebDavModel
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
import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var isFirstLod = true

    // ???????????????????????????
    private var nowIn = 0
    // ?????????????????????[??????:0,??????:1]

    @SuppressLint("InflateParams", "MissingInflatedId", "CutPasteId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingsItemsUntil.initializationItems()
        // ?????????????????????

        BRV.modelId = BR.model
        // ?????????BRK ??????Model

        setThemeMode()
        // ????????????(??????|????????????|??????)

        setBarTransparent(binding.mainPageStatusBar, this, resources)
        // ???????????????&???????????????

        setTopBar(true)
        // ?????????????????????????????????

        setNavigationDrawer(true)
        // ?????????????????????

        setBottomNavigation(true)
        // ?????????????????????????????????

        binding.mainPageSettingLayout.intoSettingTheme.setOnClickListener {
            startActivity(Intent(this, SettingItemsActivity::class.java))
            SettingObjects.name = "??????"
        }
        binding.mainPageSettingLayout.intoSettingCurrency.setOnClickListener {
            startActivity(Intent(this, SettingItemsActivity::class.java))
            SettingObjects.name = "??????"
        }
        binding.mainPageSettingLayout.intoSettingAbout.setOnClickListener {
            startActivity(Intent(this, SettingItemsActivity::class.java))
            SettingObjects.name = "??????"
        }
        // ???????????????????????????????????????

        if (SettingObjects.isClickDarkMode) {
            binding.mainPageViewPager.visibility = View.GONE
            binding.mainPageSetting.visibility = View.VISIBLE
            binding.mainPageBottomNavigation.visibility = View.GONE
            binding.mainPageTopBar.title = "??????"
            nowIn = 1
        }
        intentAction(intent)
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (nowIn == 1) {
            animationListener(binding.mainPageViewPager, binding.mainPageSetting, true)
            binding.mainPageTopBar.title = "?????????"
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
                            //ToastUtils.show("?????????????????????????????????\n????????????????????????????????????")
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
                            //ToastUtils.show("?????????????????????????????????\n????????????????????????????????????")
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
            if (ycKv.decodeString("isFirst") == "true") {
            } else {
                firstInto(true, bindingViews, ycKv)
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
                        ToastUtils.show("??????????????????????????????\n???????????????????????????\n???????????????...")
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
                    "????????????\n??????${bindingViews[1].findViewById<RecyclerView>(R.id.page2_rv)?.adapter?.itemCount}?????????"
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
                        binding.mainPageTopBar.title = "?????????"
                    }

                    1 -> {
                        binding.mainPageBottomNavigationView.selectedItemId = R.id.page2
                        binding.mainPageTopBar.title = "?????????"
                    }

                    2 -> {
                        binding.mainPageBottomNavigationView.selectedItemId = R.id.page3
                        binding.mainPageTopBar.title = "?????????"
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
                        binding.mainPageTopBar.title = "?????????"
                        nowIn = 0
                        binding.mainPageNavigationDrawer.close()
                        menuItem.isChecked = true
                    }

                    R.id.toSettings -> {
                        animationListener(binding.mainPageSetting, binding.mainPageViewPager, false)
                        binding.mainPageTopBar.title = "??????"
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
                            .setTitle("????????????")
                            .setView(view)
                            .setNegativeButton("?????????") { _, _ ->
                                VideoObjects.type = "INTERNET"
                                VideoObjects.title = "????????????"
                                VideoObjects.paths =
                                    view.findViewById<TextInputEditText>(R.id.dev_internet).text.toString()
                                startActivity(Intent(this, VideoPlayer::class.java))
                            }
                            .show()
                    }

                    R.id.toWebDav -> {
                        val kv = MMKV.mmkvWithID("WebDav")
                        if (kv.decodeInt("isLogin") != 1) {
                            val view = layoutInflater.inflate(R.layout.webdav_edit, null)
                            MaterialAlertDialogBuilder(
                                this@MainActivity,
                                com.google.android.material.R.style.MaterialAlertDialog_Material3
                            )
                                .setTitle("WebDav")
                                .setView(view)
                                .setNegativeButton("GO???") { _, _ ->
                                    val ip =
                                        view.findViewById<TextInputEditText>(R.id.web_dav_ip_edit).text.toString()
                                    val ipRoot =
                                        view.findViewById<TextInputEditText>(R.id.web_dav_ip_root_edit).text.toString()
                                    val username =
                                        view.findViewById<TextInputEditText>(R.id.web_dav_user_edit).text.toString()
                                    val password =
                                        view.findViewById<TextInputEditText>(R.id.web_dav_password_edit).text.toString()
                                    val webdavIp = if (ip.takeLast(1) == "/") {
                                        ip
                                    } else {
                                        "$ip/"
                                    }
                                    val webdavIpRoot = if (ipRoot.takeLast(1) == "/") {
                                        ipRoot
                                    } else {
                                        "$ipRoot/"
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val sardine: Sardine = OkHttpSardine()
                                            sardine.setCredentials(username, password)
                                            if (!ip.contains("http")) {
                                                ToastUtils.show("WebDav ???????????????????????????")
                                                return@launch
                                            }else if (!ip.contains(ipRoot)){
                                                ToastUtils.show("????????????Dav?????????????????????")
                                                return@launch
                                            }
                                            sardine.createDirectory(webdavIp + "VideoYou")
                                            ToastUtils.show("???????????????")
                                            kv.encode("isLogin", 1)
                                            sardine.delete(webdavIp + "VideoYou")
                                        } catch (e: Exception) {
                                            ToastUtils.showLong("????????????\n${e.message}")
                                        }
                                    }
                                    kv.encode("WebDavUser", username)
                                    kv.encode("WebDavPassword", password)
                                    kv.encode("WebDavIp", webdavIp)
                                    kv.encode("WebDavIpRoot", webdavIpRoot)
                                }
                                .show()
                            view.findViewById<TextInputEditText>(R.id.web_dav_ip_edit)
                                .setText(kv.decodeString("WebDavIp"))
                            view.findViewById<TextInputEditText>(R.id.web_dav_user_edit)
                                .setText(kv.decodeString("WebDavUser"))
                            view.findViewById<TextInputEditText>(R.id.web_dav_password_edit)
                                .setText(kv.decodeString("WebDavPassword"))
                            view.findViewById<TextInputEditText>(R.id.web_dav_ip_root_edit)
                                .setText(kv.decodeString("WebDavIpRoot"))
                        } else {
                            val int = Intent(this@MainActivity,WebDavActivity::class.java)
                            int.putExtra("webLine",kv.decodeString("WebDavIp"))
                            int.putExtra("name","/")
                            int.putExtra("dir",kv.decodeString("WebDavIp"))
                            startActivity(int)
                        }
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
                        ToastUtils.show("?????????????????????????????????????????????????????????")
                        return
                    }
                }

                override fun onDenied(
                    permissions: MutableList<String>,
                    doNotAskAgain: Boolean
                ) {
                    if (doNotAskAgain) {
                        ToastUtils.show("?????????????????????????????????")
                        // ??????????????????????????????????????????????????????????????????
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        ToastUtils.show("????????????????????????????????????")
                    }
                }
            })
    }

    @Suppress("DEPRECATION")
    private fun firstInto(isBoolean: Boolean, bindingViews: ArrayList<View>, ycKv: MMKV) {
        if (isBoolean) {
            val view = layoutInflater.inflate(R.layout.app_is_activation, null)
            val tV = view.findViewById<TextView>(R.id.is_activation)
            tV.movementMethod = LinkMovementMethod.getInstance()
            tV.text =
                Html.fromHtml("<a href='https://clearpole.gitee.io/video-you/privacy-agreement/index.html'>????????????????????????</a>")
            MaterialAlertDialogBuilder(
                this@MainActivity,
                com.google.android.material.R.style.MaterialAlertDialog_Material3
            )
                .setTitle("????????????")
                .setCancelable(false)
                .setMessage("?????????????????????????????????????????????????????????????????????")
                .setView(view)
                .setNegativeButton("?????????") { _, _ ->
                    finish()
                }
                .setPositiveButton("???????????????????????????") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        ycKv.encode("isFirst", "true")
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

    private fun setThemeMode() {
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
        startActivity(Intent(this, VideoPlayer::class.java))
    }
}
