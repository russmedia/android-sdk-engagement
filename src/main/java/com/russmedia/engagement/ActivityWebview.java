package com.russmedia.engagement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ActivityWebview extends Activity {

    static ActivityWebview activity;

    private WebView extWebView;
    private String urlToLoad;

    public ValueCallback<Uri[]> uploadMessage;
    public int INPUT_FILE_REQUEST_CODE = 1501;

    private String TAG = "ExtWebview";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();

        setContentView(R.layout.activity_webview);

        activity = this;

        extWebView = (WebView) findViewById(R.id.ext_webview);

        Bundle bun = getIntent().getExtras();
        urlToLoad = bun.getString("urlToLoad");

        WebSettings s = extWebView.getSettings();

        s.setJavaScriptEnabled(true);

        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);


        String path = getCacheDir().getAbsolutePath();
        extWebView.getSettings().setAppCachePath(path);
        extWebView.getSettings().setAllowFileAccess(true);
        extWebView.getSettings().setAppCacheEnabled(true);

        extWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        extWebView.setWebViewClient(new DNIWebClient(this));
        extWebView.setWebChromeClient(new DNIChromeClient(this));

        extWebView.loadUrl( urlToLoad );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INPUT_FILE_REQUEST_CODE) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                if (uploadMessage == null) return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                uploadMessage = null;
            }
        }
    }

    public static ActivityWebview getInstance(){
        return activity;
    }

    public void close_window(View v) {
        this.finish();
    }
}
