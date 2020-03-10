package com.russmedia.engagement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.russmedia.engagement.helper.Utils;


public class DNIWebClient extends WebViewClient {

    Activity activity;

    public DNIWebClient(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        super.doUpdateVisitedHistory(view, url, isReload);
        checkForDeepLink(view);
    }

    public void onLoadResource(WebView view, String url) {
        checkForDeepLink(view);
    }

    private void checkForDeepLink(WebView view) {
        String webUrl = view.getUrl();

        if (webUrl == null) {
            return;
        }

        int pos = webUrl.indexOf("#");

        if (pos > 0) {

            String hash = webUrl.substring( pos, webUrl.length() );

            if ( hash.equals( "#close" ) ) {
                activity.finish();
            } else if (hash.startsWith("#openTeeOverview")) {

                String[] params = hash.split("\\?");

                if ( params.length == 2 ) {
                    EngagementEngine.getInstance().handleDeepLink(null, params[1]);
                }

                activity.finish();
            }
        }
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        //url = "volat://webview/https://aboshop.vn.at/laendlepunkte.html#deeplink";

        if (url != null && Utils.matches(url, activity.getString(R.string.rm_ee_regex_tee_webview))) {
            activity.finish();
            String param = Utils.getUrlParam(url, "entry");
            EngagementEngine.getInstance().handleDeepLink(param, null);
            return true;
        }

        if (url != null && Utils.matches(url, activity.getString(R.string.rm_ee_regex_tee_extern))) {
            String externUrl = url.split("\\$")[1];
            if (!externUrl.contains("http")) {
                externUrl = "http://" + externUrl;
            }
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(externUrl));
            try {
                activity.startActivity(browserIntent);
            } catch (Exception e) {
                //not a valid url probably, app shouldn't crash tho
            }
            return true;
        }

        if ( Utils.matches(url, activity.getString(R.string.rm_ee_regex_registration)) || Utils.matches(url, activity.getString(R.string.rm_ee_regex_article)) ) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            activity.finish();
            return true;
        } else {

            if (Utils.matches(url, activity.getString(R.string.rm_ee_regex_webview))) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

                activity.startActivity(intent);
                activity.finish();
                return true;
            }

        }
        return false;
    }

}
