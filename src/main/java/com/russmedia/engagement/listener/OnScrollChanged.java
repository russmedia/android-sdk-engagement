package com.russmedia.engagement.listener;

import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.russmedia.engagement.RMWebsocketCommunicator;
import com.russmedia.engagement.classes.Challenge;

import org.json.JSONObject;

public class OnScrollChanged implements ViewTreeObserver.OnScrollChangedListener {

    ScrollView scrollView;
    int threshold;
    Challenge challenge;
    RMWebsocketCommunicator socketClient;


    public OnScrollChanged(ScrollView scrollView, int threshold, Challenge challenge, RMWebsocketCommunicator socketClient) {
        this.scrollView = scrollView;
        this.threshold = threshold;
        this.challenge = challenge;
        this.socketClient = socketClient;
    }


    @Override
    public void onScrollChanged() {
        int pos = scrollView.getScrollY();

        if ( pos > threshold ) {

            String token = challenge.getToken();
            JSONObject data = challenge.getMatched_data();
            String data_str = data.toString();
            String message = "{\"method\":\"log\",\"data\":"+ data_str + ",\"step\":\""+ token +"\"}";
            if (socketClient != null) {
                socketClient.send_message(message);
            }

            scrollView.getViewTreeObserver().removeOnScrollChangedListener(this);
        }
    }
}
