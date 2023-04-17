package kim.hsl.webviewdemo

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 隐藏状态栏和导航栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // 设置窗口全屏
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // 加载布局
        setContentView(R.layout.activity_main)

        // 获取 WebView 组件
        val webview = findViewById<WebView>(R.id.webview)

        // 获取并设置 Web 设置
        val settings = webview.settings
        settings.javaScriptEnabled = true   // 支持 JavaScript
        settings.domStorageEnabled = true   // 支持 HTML5
        settings.builtInZoomControls = true // 自选 非必要

        // 5.0 以上需要设置允许 http 和 https 混合加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        } else {
            // 5.0 以下不用考虑  http 和 https 混合加载 问题
            settings.mixedContentMode = WebSettings.LOAD_NORMAL
        }

        // 设置页面自适应
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // JavaScript 出错不报异常
            try {
                val method = Class.forName("android.webkit.WebView")
                    .getMethod("setWebContentsDebuggingEnabled", java.lang.Boolean.TYPE)
                if (method != null) {
                    method.isAccessible = true
                    method.invoke(null, true)
                }
            } catch (e: Exception) {
                // JavaScript 出错处理 此处不进行任何操作
            }
        }

        // 设置 WebView 是否可以获取焦点
        webview.isFocusable = true              // 自选 非必要
        // 设置 WebView 是否启用绘图缓存 位图缓存可加速绘图过程
        webview.isDrawingCacheEnabled = true    // 自选 非必要
        // 设置 WebView 中的滚动条样式
        // SCROLLBARS_INSIDE_OVERLAY - 在内容上覆盖滚动条 ( 默认 )
        webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY // 自选 非必要

        // WebChromeClient 是一个用于处理 WebView 界面交互事件的类
        webview.webChromeClient = object : WebChromeClient() {
            // 显示 网页加载 进度条
            override fun onProgressChanged(view: WebView, progress: Int) {
                val txtProgress = findViewById<TextView>(R.id.textview)
                txtProgress.text = String.format(Locale.CHINA, "%d%%", progress)
                txtProgress.visibility =
                    if (progress > 0 && progress < 100) View.VISIBLE else View.GONE
            }

            // 处理 WebView 对地理位置权限的请求
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback)
                callback.invoke(origin, true, false)
            }
        }

        // WebViewClient 是一个用于处理 WebView 页面加载事件的类
        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // 4.0 之后必须添加该设置
                // 只能加载 http:// 和 https:// 页面 , 不能加载其它协议链接
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url)
                    return true
                }
                return false
            }

            // SSL 证书校验出现异常
            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError) {
                when (error.primaryError) {
                    SslError.SSL_INVALID, SslError.SSL_UNTRUSTED -> {
                        handler.proceed()
                    }
                    else -> handler.cancel()
                }
            }
        }

        // 加载网页
        webview.loadUrl("https://www.baidu.com/")
    }
}