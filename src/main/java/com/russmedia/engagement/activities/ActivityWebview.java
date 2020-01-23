package com.russmedia.engagement.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.russmedia.engagement.DNIChromeClient;
import com.russmedia.engagement.DNIWebClient;
import com.russmedia.engagement.EngagementEngine;
import com.russmedia.engagement.R;

import java.net.URLEncoder;

public class ActivityWebview extends Activity {

    static ActivityWebview activity;

    private WebView extWebView;
    private String urlToLoad;

    public ValueCallback<Uri[]> uploadMessage;

    private String TAG = "ExtWebview";

    public ValueCallback<Uri[]> mFilePathCallback;
    public String mCameraPhotoPath;
    public static final int INPUT_FILE_REQUEST_CODE = 1;

    private PermissionRequest permissionRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);

        ViewGroup wrapper = findViewById(R.id.ee_wrapper_nav_main);
        EngagementEngine.getInstance().addCurrencyView(wrapper);

        activity = this;

        extWebView = findViewById(R.id.ext_webview);

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

        extWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        extWebView.setWebViewClient(new DNIWebClient(this));
        extWebView.setWebChromeClient(new DNIChromeClient(this));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            extWebView.setWebContentsDebuggingEnabled(true);
        }

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            int verCode = pInfo.versionCode;
            urlToLoad += "&app_version=" + URLEncoder.encode(String.valueOf(verCode), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }



        extWebView.loadUrl( urlToLoad );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;

        }
    }

    public void onBackPressed() {

        if (extWebView.canGoBack()) {
            extWebView.goBack();

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && permissionRequest != null) {
            permissionRequest.grant(permissionRequest.getResources());
        }
    }

    public PermissionRequest getPermissionRequest() {
        return permissionRequest;
    }

    public void setPermissionRequest(PermissionRequest permissionRequest) {
        this.permissionRequest = permissionRequest;
    }

    public static ActivityWebview getInstance(){
        return activity;
    }

    public void press_back(View v) {
        onBackPressed();
    }

    public void close_window(View v) {
        this.finish();
    }
}
