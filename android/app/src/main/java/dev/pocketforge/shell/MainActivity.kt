package dev.pocketforge.shell

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.SafeBrowsingResponse
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var errorText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        val root = FrameLayout(this)
        webView = WebView(this)
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        errorText = TextView(this).apply {
            text = "网页暂时无法打开，请检查网络后重试"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(32, 32, 32, 32)
            visibility = View.GONE
        }
        root.addView(webView, FrameLayout.LayoutParams(-1, -1))
        root.addView(progress, FrameLayout.LayoutParams(-1, 8))
        root.addView(errorText, FrameLayout.LayoutParams(-1, -2))
        val downloadsButton = Button(this).apply {
            text = "下载列表"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, DownloadsActivity::class.java))
            }
        }
        val buttonParams = FrameLayout.LayoutParams(-2, -2).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 24, 24)
        }
        root.addView(downloadsButton, buttonParams)
        setContentView(root)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
            safeBrowsingEnabled = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return if (uri.scheme == "https" || uri.scheme == "http") {
                    false
                } else {
                    openExternal(uri)
                    true
                }
            }

            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                errorText.visibility = View.GONE
                progress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView, url: String) {
                progress.visibility = View.GONE
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) {
                    progress.visibility = View.GONE
                    errorText.visibility = View.VISIBLE
                }
            }

            override fun onSafeBrowsingHit(
                view: WebView,
                request: WebResourceRequest,
                threatType: Int,
                callback: SafeBrowsingResponse
            ) {
                callback.backToSafety(true)
                Toast.makeText(this@MainActivity, "已拦截可能存在风险的网页", Toast.LENGTH_LONG).show()
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress.progress = newProgress
                progress.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }
        }

        webView.setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
            try {
                val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
                val request = DownloadManager.Request(Uri.parse(url))
                request.setMimeType(mimeType)
                request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
                request.addRequestHeader("User-Agent", userAgent)
                request.setTitle(fileName)
                request.setDescription("正在下载…")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)
                Toast.makeText(this, "已开始下载，点右下角“下载列表”查看", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(this, "下载失败", Toast.LENGTH_SHORT).show()
            }
        }

        if (savedInstanceState != null) webView.restoreState(savedInstanceState)
        else webView.loadUrl(GeneratedConfig.START_URL)
    }

    private fun openExternal(uri: Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: Exception) {
            Toast.makeText(this, "此链接无法打开", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}

class DownloadsActivity : Activity() {
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var adapter: ArrayAdapter<String>
    private val ids = mutableListOf<Long>()
    private val refreshTask = object : Runnable {
        override fun run() {
            loadDownloads()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val header = TextView(this).apply {
            text = "下载列表"
            textSize = 18f
            setPadding(32, 32, 32, 16)
        }
        val listView = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            if (position >= 0 && position < ids.size) openDownload(ids[position])
        }
        root.addView(header)
        root.addView(listView, LinearLayout.LayoutParams(-1, -1))
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        handler.post(refreshTask)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshTask)
    }

    private fun loadDownloads() {
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val cursor = dm.query(DownloadManager.Query()) ?: return
        val rows = mutableListOf<String>()
        val newIds = mutableListOf<Long>()
        val idCol = cursor.getColumnIndex(DownloadManager.COLUMN_ID)
        val titleCol = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val totalCol = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
        val doneCol = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idCol)
            val name = cursor.getString(titleCol) ?: "未命名文件"
            val status = cursor.getInt(statusCol)
            val total = cursor.getLong(totalCol)
            val done = cursor.getLong(doneCol)
            val state = when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> "已完成，点此打开"
                DownloadManager.STATUS_FAILED -> "下载失败"
                DownloadManager.STATUS_PAUSED -> "已暂停"
                DownloadManager.STATUS_PENDING -> "等待中"
                else -> if (total > 0) "下载中 ${done * 100 / total}%" else "下载中"
            }
            newIds.add(id)
            rows.add("$name\n$state")
        }
        cursor.close()
        newIds.reverse()
        rows.reverse()
        ids.clear()
        ids.addAll(newIds)
        adapter.clear()
        if (rows.isEmpty()) adapter.add("暂无下载") else adapter.addAll(rows)
        adapter.notifyDataSetChanged()
    }

    private fun openDownload(id: Long) {
        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = dm.getUriForDownloadedFile(id)
        if (uri == null) {
            Toast.makeText(this, "文件不存在或未下载完成", Toast.LENGTH_SHORT).show()
            return
        }
        val mime = dm.getMimeTypeForDownloadedFile(id) ?: "*/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "无法打开此文件", Toast.LENGTH_SHORT).show()
        }
    }
}