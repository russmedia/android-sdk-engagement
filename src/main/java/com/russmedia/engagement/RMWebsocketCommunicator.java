package com.russmedia.engagement;

import android.app.Activity;
import android.content.Context;

import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.extensions.IExtension;
import org.java_websocket.protocols.IProtocol;
import org.java_websocket.protocols.Protocol;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

public class RMWebsocketCommunicator implements RMWebsocketCallbacks{

    private RMWebsocketClient mWebSocketClient;

    private Context ctx;
    private Activity act;

    private final int connecttimeout = 1000;

    private EngagementEngine engine;
    private Draft_6455 draft;

    public RMWebsocketCommunicator(Context ctx, Activity act, EngagementEngine engine) {

        this.engine = engine;
        this.ctx = ctx;
        this.act = act;

        init_websocket(ctx);

    }

    private void init_websocket(Context ctx ) {
        String str_uri = ctx.getString( R.string.url_websocket );

        URI uri;
        try {
            uri = new URI( str_uri );
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Draft_6455 draft_echoprot = new Draft_6455(Collections.<IExtension>emptyList(), Collections.<IProtocol>singletonList(new Protocol("echo-protocol")));

        mWebSocketClient = new RMWebsocketClient(uri, draft_echoprot, null, connecttimeout, this.engine);

        mWebSocketClient.set_callback(this);
        mWebSocketClient.flush_queue();
    }

    public void send_message(String message) {

        if ( !mWebSocketClient.isOpen() ){
            try {
                mWebSocketClient.connect();
            } catch (IllegalStateException ex) {
                init_websocket(ctx);
            }
        }

        mWebSocketClient.send_message(message);

    }

    @Override
    public void websocket_closed(String reason) {

        //Deactivate for following errors:
        //failed to connect to tee-ws.russmedia.com/194.183.143.22 (port 443) after 1000ms
        //java.security.cert.CertPathValidatorException: Trust anchor for certification path not found.

        if (reason.startsWith("Host is unresolved") || reason.startsWith("failed to connect") || reason.startsWith("java.security.cert") || reason.startsWith("disconnect")) {
            EngagementEngine.getInstance().stop_engine();
        } else {

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {}

                    if (act != null) {
                        act.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                init_websocket(ctx);
                                EngagementEngine.getInstance().reinit();
                            }
                        });
                    }
                }

            };
            thread.start();
        }
    }
}
