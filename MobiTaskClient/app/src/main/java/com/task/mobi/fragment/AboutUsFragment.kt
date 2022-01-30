package com.task.mobi.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.task.mobi.R
import com.task.mobi.retrofitManager.ProgressDialog
import kotlinx.android.synthetic.main.fg_about_us.*

/**
 * Created by Neeraj Narwal on 5/5/17.
 */
class AboutUsFragment : BaseFragment() {

    private var progress: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setToolbar(true, "About Us")
        return inflater!!.inflate(R.layout.fg_about_us, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = ProgressDialog.getInstance(baseActivity)
        progress?.initiateProgressDialog()

        webview.webViewClient = myWebClient()
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("http://www.pugmarks.in/about-pugmarks/about-pugmarks.aspx")


    }

    inner class myWebClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progress?.startProgressDialog()
        }

        override fun onPageCommitVisible(view: WebView, url: String) {
            super.onPageCommitVisible(view, url)
            progress?.stopProgressDialog()
        }
    }
}
