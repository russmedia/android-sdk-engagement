package com.russmedia.engagement;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class ActivityDialog extends Activity {

    WebView ww;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dialog);

        Bundle bun = getIntent().getExtras();
        String url = bun.getString("urlToLoad");

        this.setFinishOnTouchOutside(false);

        ww = (WebView) findViewById(R.id.notif_webview);

        ww.getSettings().setJavaScriptEnabled(true);

        ww.setWebViewClient(new DNIWebClient(this));

        ww.loadUrl(url);


    }
}
