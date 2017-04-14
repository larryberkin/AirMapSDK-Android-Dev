package com.airmap.airmapsdk.ui.activities;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.util.Constants;

public class WebActivity extends AppCompatActivity {

    private static final String TAG = "WebActivity";

    Toolbar toolbar;
    ProgressBar progressBar;
    WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        webView = (WebView) findViewById(R.id.web_view);

        progressBar.setMax(100);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) { //Using this deprecated version for backwards compatibility on older API levels
                if (webView != null) {
                    Snackbar.make(webView, description, Snackbar.LENGTH_SHORT).show();
                }
            }


        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                    if (newProgress == 100) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

        });

        String url = getIntent().getStringExtra(Constants.URL_EXTRA);
        if (url.endsWith(".pdf")) {
            // This enables the user to view PDF's within the app's built in web browser
            url = Uri.parse("http://docs.google.com/viewer?url=" + url).toString();
        }
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
