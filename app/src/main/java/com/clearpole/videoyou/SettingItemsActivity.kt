package com.clearpole.videoyou

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.clearpole.videoyou.databinding.ActivitySettingItemsBinding
import com.clearpole.videoyou.objects.SettingObjects
import com.clearpole.videoyou.utils.SetBarTransparent
import com.clearpole.videoyou.utils.SettingsItemsUntil
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingItemsActivity : BaseActivity<ActivitySettingItemsBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SetBarTransparent.setBarTransparent(
            binding.root.findViewById(R.id.setting_items_status) as LinearLayout,
            this,
            resources
        )
        binding.topAppBar.title = SettingObjects.name
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
        try {
            when (SettingObjects.name) {
                "通用" -> {
                    settingCurrency()
                }
                "主题" -> {
                    settingTheme()
                }
                "关于" -> {
                    settingAbout()
                }
            }
        } catch (e: Exception) {
            MaterialAlertDialogBuilder(this)
                .setTitle("意外的错误")
                .setMessage("您遇到了一个意外的错误：\n${e.javaClass}\n已拦截事件。")
                .setCancelable(false)
                .setPositiveButton("确定") { _, _ -> finish() }
                .show()
        }
    }

    private fun settingAbout(){
        binding.settingAbout.root.visibility = View.VISIBLE
        binding.settingAbout.intoQq.setOnClickListener {
            MaterialAlertDialogBuilder(
                this@SettingItemsActivity,
                com.google.android.material.R.style.MaterialAlertDialog_Material3
            )
                .setTitle("加入QQ频道")
                .setCancelable(false)
                .setMessage("加入频道可获取最新版本更新，是否访问外部链接以加入QQ频道？")
                .setNegativeButton("取消") { _, _ -> }
                .setPositiveButton("加入QQ频道"){_,_ ->
                    val uri = Uri.parse("https://pd.qq.com/s/61vf6d5qi")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(intent)
                }
                .show()
        }
    }

    private fun settingCurrency() {
        binding.settingCurrency.root.visibility = View.VISIBLE
        val mode = SettingsItemsUntil.readSettingData("isAutoPicture")?.toBoolean()!!
        binding.settingCurrency.settingCurrencyPicture.isChecked = mode
        binding.settingCurrency.settingCurrencyPictureRoot.setOnClickListener {
            if (binding.settingCurrency.settingCurrencyPicture.isChecked) {
                binding.settingCurrency.settingCurrencyPicture.isChecked = false
                SettingsItemsUntil.writeSettingData("isAutoPicture", "false")
            } else {
                binding.settingCurrency.settingCurrencyPicture.isChecked = true
                SettingsItemsUntil.writeSettingData("isAutoPicture", "true")
            }
        }
    }

    private fun settingTheme() {
        binding.settingTheme.root.visibility = View.VISIBLE
        binding.settingTheme.settingThemeRippleView.isChecked =
            SettingsItemsUntil.readSettingData("isRipple").toBoolean()
        binding.settingTheme.settingThemeRippleRoot.setOnClickListener {
            if (binding.settingTheme.settingThemeRippleView.isChecked) {
                binding.settingTheme.settingThemeRippleView.isChecked = false
                SettingsItemsUntil.writeSettingData("isRipple", "false")
            } else {
                binding.settingTheme.settingThemeRippleView.isChecked = true
                SettingsItemsUntil.writeSettingData("isRipple", "true")
            }
        }

        val mode = SettingsItemsUntil.readSettingData("darkMode")?.toInt()!!
        binding.settingTheme.settingThemeDarkMode.text =
            when (mode) {
                0 -> {
                    "跟随系统"
                }

                1 -> {
                    "始终开启"
                }

                2 -> {
                    "始终关闭"
                }

                else -> {
                    "错误"
                }
            }

        binding.settingTheme.settingThemeDayNightMode.setOnClickListener {
            val choices = arrayOf<CharSequence>("跟随系统", "始终开启", "始终关闭")
            MaterialAlertDialogBuilder(this)
                .setTitle("深色模式")
                .setSingleChoiceItems(
                    choices,
                    SettingsItemsUntil.readSettingData("darkMode")!!.toInt(),
                    null
                )
                .setPositiveButton("确定") { dialog: DialogInterface, _: Int ->
                    val checkedItemPosition: Int =
                        (dialog as AlertDialog).listView.checkedItemPosition
                    if (checkedItemPosition != AdapterView.INVALID_POSITION) {
                        binding.settingTheme.settingThemeDarkMode.text = choices[checkedItemPosition]
                        SettingsItemsUntil.writeSettingData(
                            "darkMode",
                            checkedItemPosition.toString()
                        )
                    }
                    when (checkedItemPosition) {
                        0 -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                            SettingObjects.isClickDarkMode = true
                        }

                        1 -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            SettingObjects.isClickDarkMode = true
                        }

                        else -> {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            SettingObjects.isClickDarkMode = true
                        }
                    }
                }.show()
        }
    }
}