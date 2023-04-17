package kim.hsl.webviewdemo

import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
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
        // 设置是否启用 DOM 存储
        // DOM 存储是一种在 Web 应用程序中存储数据的机制，它使用 JavaScript 对象和属性来存储和检索数据
        settings.domStorageEnabled = true
        // 设置 WebView 是否启用内置缩放控件 ( 自选 非必要 )
        settings.builtInZoomControls = true

        // 5.0 以上需要设置允许 http 和 https 混合加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        } else {
            // 5.0 以下不用考虑  http 和 https 混合加载 问题
            settings.mixedContentMode = WebSettings.LOAD_NORMAL
        }

        // 设置页面自适应
        // Viewport 元标记是指在 HTML 页面中的 <meta> 标签 , 可以设置网页在移动端设备上的显示方式和缩放比例
        // 设置是否支持 Viewport 元标记的宽度
        settings.useWideViewPort = true

        // 设置 WebView 是否使用宽视图端口模式
        // 宽视图端口模式下 , WebView 会将页面缩小到适应屏幕的宽度
        // 没有经过移动端适配的网页 , 不要启用该设置
        settings.loadWithOverviewMode = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // JavaScript 出错不报异常
            try {
                // 启用 调试模式
                // 由于 WebView#setWebContentsDebuggingEnabled 函数不能直接访问
                // 必须使用反射进行访问
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

        // 设置 WebView 是否可以获取焦点 ( 自选 非必要 )
        webview.isFocusable = true
        // 设置 WebView 是否启用绘图缓存 位图缓存可加速绘图过程 ( 自选 非必要 )
        webview.isDrawingCacheEnabled = true
        // 设置 WebView 中的滚动条样式 ( 自选 非必要 )
        // SCROLLBARS_INSIDE_OVERLAY - 在内容上覆盖滚动条 ( 默认 )
        webview.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

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
                Log.i("MainActivity", "访问地址 : $url")
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