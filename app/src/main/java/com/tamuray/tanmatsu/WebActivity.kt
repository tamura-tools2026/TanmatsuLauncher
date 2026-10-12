package com.tamuray.tanmatsu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.window.OnBackInvokedDispatcher
import java.io.ByteArrayInputStream

/**
 * 端末内フォルダの HTML を全画面で表示する。
 *
 * しくみ：
 *  ページには https://tanmatsu.local/ という「架空のアドレス」を与える。
 *  このアドレス宛ての読み込み（HTML・CSS・JS・画像など）はすべてアプリが横取りし、
 *  設定で選んだフォルダの中のファイルを返す。ネットには出ない。
 *  それ以外のアドレス（中継PCなど）は横取りせず、そのまま通信させる。
 */
class WebActivity : Activity() {

    companion object {
        /** 端末内フォルダに割り当てる架空のホスト名 */
        const val HOST = "tanmatsu.local"
        const val BASE = "https://$HOST/"
    }

    private lateinit var webView: WebView
    private lateinit var folder: LocalFolder

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tree = Prefs.folder(this)
        if (tree == null) {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
            return
        }
        folder = LocalFolder(this, tree)

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true          // localStorage を使えるようにする
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            // ページ側は https://tanmatsu.local、中継PCは http:// になる想定なので、
            // https のページから http へ送受信することを許可する
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    val url = request.url
                    if (url.host != HOST) return null   // 横取りしない → 通常の通信
                    val path = (url.path ?: "/").let { if (it == "/" || it.isEmpty()) Prefs.startFile(this@WebActivity) else it }
                    val stream = folder.open(path)
                        ?: return WebResourceResponse(
                            "text/plain", "UTF-8", 404, "Not Found", null,
                            ByteArrayInputStream("見つかりません: $path".toByteArray())
                        )
                    return WebResourceResponse(LocalFolder.mimeOf(path), "UTF-8", stream)
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val scheme = request.url.scheme ?: return false
                    if (scheme == "http" || scheme == "https") return false
                    // tel: / mailto: などは外部アプリへ
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, request.url))
                    } catch (_: ActivityNotFoundException) {
                    }
                    return true
                }
            }
        }
        setContentView(webView)

        // Android 13 以降の戻る操作（targetSdk 36 では onBackPressed が呼ばれない）
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) { if (webView.canGoBack()) webView.goBack() else finish() }
        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            webView.loadUrl(BASE + Prefs.startFile(this))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::webView.isInitialized) webView.saveState(outState)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }
}
