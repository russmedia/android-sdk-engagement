package com.russmedia.engagement.activities;


import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

import com.russmedia.engagement.DNIWebClient;
import com.russmedia.engagement.R;


public class ActivityDialog extends Activity {

    WebView ww;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dialog);

        Bundle bun = getIntent().getExtras();
        String url = bun.getString("urlToLoad");

        this.setFinishOnTouchOutside(true);

        ww = (WebView) findViewById(R.id.notif_webview);

        ww.getSettings().setJavaScriptEnabled(true);

        ww.setWebViewClient(new DNIWebClient(this));

        ww.loadUrl(url);


    }
}
