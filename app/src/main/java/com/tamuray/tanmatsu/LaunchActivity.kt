package com.tamuray.tanmatsu

import android.app.Activity
import android.content.Intent
import android.os.Bundle

/** ホーム画面アイコンから起動。アドレス未設定なら設定画面、設定済みならそのまま開く */
class LaunchActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = Prefs.url(this)
        if (url.isBlank() || !Prefs.open(this, url, Prefs.inApp(this))) {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        finish()
    }
}
