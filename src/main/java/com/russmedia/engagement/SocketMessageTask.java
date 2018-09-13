package com.russmedia.engagement;


import android.os.AsyncTask;
import android.view.View;

public class SocketMessageTask extends AsyncTask<String, Void, Void> {

    private String message;
    private View view;

    public SocketMessageTask(String message, View view, RMWebsocketClient cient) {
        this.message = message;
        this.view = view;
    }

    protected Void doInBackground(String... urls) {



        return null;
    }

    protected void onPostExecute() {



    }
}