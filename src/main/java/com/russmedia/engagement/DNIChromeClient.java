package com.russmedia.engagement;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

public class DNIChromeClient extends WebChromeClient {

    private ActivityWebview act;

    private static final String TAG = "ArticleDetailChromeClient";


    public DNIChromeClient(ActivityWebview act) {
        this.act = act;
    }


    //The undocumented magic method override
    // For Android 3.0-

    public void openFileChooser(ValueCallback<Uri> uploadMsg) {

        Toast.makeText(act, "Es tut uns leid, der Fileupload wird erst ab Android 5.0 Lollipop unterstützt", Toast.LENGTH_LONG).show();

    }

    // For Android 3.0+
    public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType ) {

        Toast.makeText(act, "Es tut uns leid, der Fileupload wird erst ab Android 5.0 Lollipop unterstützt", Toast.LENGTH_LONG).show();

    }


    // For Android 4.1+ doesnt work for 4.4
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){

        Toast.makeText(act, "Es tut uns leid, der Fileupload wird erst ab Android 5.0 Lollipop unterstützt", Toast.LENGTH_LONG).show();
    }


    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            if (act.uploadMessage != null) {
                act.uploadMessage.onReceiveValue(null);
                act.uploadMessage = null;
            }

            act.uploadMessage = filePathCallback;

            Intent intent = null;

            intent = fileChooserParams.createIntent();

            try {
                act.startActivityForResult(intent, act.INPUT_FILE_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                act.uploadMessage = null;
                Toast.makeText(act, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }

            return true;

        } else {
            Toast.makeText(act, "Es tut uns leid, der Fileupload wird erst ab Android 5.0 Lollipop unterstützt", Toast.LENGTH_LONG).show();
            return false;
        }
    }


}
