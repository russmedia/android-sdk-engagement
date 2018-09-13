package com.russmedia.engagement;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class RMWebsocketClient extends WebSocketClient {

    private RMWebsocketCallbacks callback;
    private Queue<String> message_queue;

    private EngagementEngine engine;

    public RMWebsocketClient(URI serverUri, Draft_6455 protocolDraft, Map<String, String> httpHeaders, int connectTimeout, EngagementEngine engine) {
        super(serverUri, protocolDraft, null, connectTimeout);

        message_queue = new LinkedList<>();

        this.engine = engine;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        try {
            Log.i("Websocket", "Opened");
            poll_message_queue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void poll_message_queue() {
        while ( !message_queue.isEmpty() ) {
            String message = message_queue.poll();
            send( message );
        }
    }

    @Override
    public void onMessage(String message) {

        Log.i("Websocket", "Received: " + message);
        engine.on_message_received( message );
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket", "Closed " + reason);

        callback.websocket_closed(reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.i("Websocket", "Error " + ex.getMessage());
    }

    public void flush_queue() {
        message_queue = new LinkedList<String>();
    }

    public void send_message(String message) {

        try {
            Log.i("Websocket", "Sending: " + message);
            if ( isOpen() ) {
                poll_message_queue();
                send(message);
            } else {
                message_queue.add( message );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

    public void set_callback(RMWebsocketCallbacks callback) {
        this.callback = callback;
    }


}
