package com.russmedia.engagement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class DNIWebClient extends WebViewClient {

    Activity activity;

    public DNIWebClient(Activity activity) {
        this.activity = activity;
    }

    public void onLoadResource(WebView view, String url) {

        String webUrl = view.getUrl();

        int pos = webUrl.indexOf("#");

        if (pos > 0) {

            String hash = webUrl.substring( pos, webUrl.length() );

            if ( hash.equals( "#close" ) ) {
                activity.finish();
            } else if (hash.startsWith("#openTeeOverview")) {

                String[] params = hash.split("\\?");

                if ( params.length == 2 ) {
                    EngagementEngine.getInstance().open_me_view(params[1]);
                }

                activity.finish();
            }
        }
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        if ( Utils.matches(url, activity.getString(R.string.regex_registration)) || Utils.matches(url, activity.getString(R.string.regex_article)) ) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            activity.finish();
            return true;
        }
        return false;
    }

}
