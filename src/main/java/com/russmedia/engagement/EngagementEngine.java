package com.russmedia.engagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EngagementEngine {


    private static final String TAG = "EngagementEngine";
    public enum CHALLENGE_EVENT { CLICK, SCROLL }

    private static final EngagementEngine ourInstance = new EngagementEngine();
    private static final String EE_COLLECTOR_TOKEN_KEY = "ee_collector_token";

    private static final int EE_TEXT_ID = 0;
    private static final int EE_IMG_ID = 1;

    private RMWebsocketCommunicator socket_client = null;

    private Activity act;
    private Context ctx;

    private String userId;
    private String userEmail;

    private String api_token;
    private String client;

    private String collector_token;

    private UserData user_data;

    private HashMap<Integer, ViewGroup> ee_currency_views;

    private HashMap<String, View> view_map;
    private HashMap<String, Challenge> challenge_map;

    private boolean found = false;

    private String ping_token;
    private int reconnect_amts = 0;
    private ViewGroup saved_view = null;
    private boolean currency_changed = false;

    public static EngagementEngine getInstance() {
        return ourInstance;
    }


    private EngagementEngine() {
        view_map = new HashMap<>(0);
        challenge_map = new HashMap<>(0);
        ee_currency_views = new HashMap<>(0);
    }

    public boolean check_force_close() {

        if ( reconnect_amts >= 10 ) {
            reconnect_amts = 0;
            return true;
        } else {
            reconnect_amts++;
            return false;
        }

    }

    public void stop_engine() {
        socket_client = null;
    }

    public void track_challenge(JSONObject cfg) {

        try {

            if (cfg.has("meta")) {
                JSONObject meta = cfg.getJSONObject("meta");

                if (meta.has("permalink")) {
                    String permalink = meta.getString("permalink");

                    String msg = "{\"method\":\"challenges\",\"for\":\"" + permalink + "\"}";
                    socket_client.send_message( msg );
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void track_event( CHALLENGE_EVENT event, int view_ressource_id ) {

        if (socket_client == null) {
            return;
        }

        String view_ressource_name = get_view_id(ctx, view_ressource_id);

        Challenge challenge = challenge_map.get( view_ressource_name );

        if (challenge != null) {

            String token = challenge.getToken();

            JSONObject matched_data = challenge.getMatched_data();
            String data_string = "";

            if (matched_data != null) {
                data_string = ", \"data\" : " + matched_data.toString();
            }

            String message = "{\"method\":\"log\"" + data_string + "  ,\"step\":\""+ token + "\"}";
            socket_client.send_message( message );
        }

    }

    private String get_view_id(Context ctx, int view_id) {
        String viewID = null;
        String pkg_name = ctx.getPackageName();

        try {
            viewID = ctx.getResources().getResourceName(view_id);
            viewID = viewID.replace(pkg_name + ":id/", "");
            viewID = viewID.replace("android:id/", "");
        } catch (Resources.NotFoundException nfx) {}

        return viewID;
    }

    private String get_view_id(Context ctx, View v) {

        String viewID = null;
        String pkg_name = ctx.getPackageName();

        try {
            viewID = ctx.getResources().getResourceName(v.getId());
            viewID = viewID.replace(pkg_name + ":id/", "");
            viewID = viewID.replace("android:id/", "");
        } catch (Resources.NotFoundException nfx) {}

        return viewID;
    }

    public void add_layout(View layout, String url_p, String id_p) {

        if (socket_client == null) {
            return;
        }

        String url = url_p;
        String id = id_p;

        view_map.put(id, layout);

        String message = "";

        if (url_p == null) {
            message = "{\"method\":\"challenges\",\"for\":{\"view\" : \"" + id + "\"}}";
        } else {
            message = "{\"method\":\"challenges\",\"for\":{\"view\" : \"" + id + "\",  \"id\" : \"" + url + "\"}}";
        }

        socket_client.send_message( message );

    }

    private void remove_currency_views() {

        for (ViewGroup view : ee_currency_views.values()) {
            view.removeAllViews();
        }

    }

    public void add_currency_view(ViewGroup view) {

        if ( view != null ) {

            int view_id = view.getId();

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    open_me_view("");
                }
            });

            view.setTag(R.string.order, ee_currency_views.size() + 1 );
            ee_currency_views.put(view_id, view );

            if ( user_data != null ) {
                act.runOnUiThread(new Runnable() {
                    public void run() {
                        update_currency_views( ee_currency_views, user_data, false );
                    }
                });
            }
        }
    }

    public void open_me_view(String additional_params) {
        if (user_data != null) {

            String token = user_data.get_customer_collector_token();

            Intent intent = new Intent(act, ActivityWebview.class);
            Bundle b = new Bundle();

            if (additional_params == null) {
                additional_params = "";
            }

            if (additional_params.length() > 0 && !additional_params.startsWith("&")) {
                additional_params = "&" + additional_params;
            }

            String url = ctx.getString(R.string.url_webserver) + "me?collector_token=" + token + "&platform=android" + additional_params;

            b.putString("urlToLoad", url);
            intent.putExtras(b);
            act.startActivity(intent);
        }
    }

    public void show_dialog( String url) {

        Intent intent = new Intent(act, ActivityDialog.class);
        Bundle b = new Bundle();

        b.putString("urlToLoad", url);
        intent.putExtras(b);
        act.startActivity(intent);

    }


    private void update_currency_views(HashMap<Integer, ViewGroup> nav_list, UserData data, boolean update) {

        if (data == null) {
            return;
        }

        String currency_id = data.get_currency_id();
        String currency_pic_url = data.get_currency_pic_url();

        if (currency_id == null || currency_id.equals("")) {
            return;
        }

        boolean currency_visible = data.is_currency_visible();

        if (!currency_visible) {
            return;
        }

        int size = nav_list.size();

        for (ViewGroup view : nav_list.values()) {

            boolean addvievs = false;
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (currency_changed) {
                view.removeAllViews();
            }

            TextView ee_text = view.findViewById(EE_TEXT_ID);

            if (ee_text == null) {
                ee_text = (TextView) inflater.inflate(R.layout.ee_text, null);
                ee_text.setId(EE_TEXT_ID);
                addvievs = true;
            }

            ImageView ee_image = view.findViewById(EE_IMG_ID);

            if (ee_image == null) {
                ee_image = (ImageView) inflater.inflate(R.layout.ee_image, null);
                ee_image.setId(EE_IMG_ID);
                addvievs = true;
            }


            if (currency_id.equals("bergtaler")) {

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.height = Utils.dpToPx(20 ,ctx);

                ee_image.setLayoutParams(params);
                ee_text.setGravity(Gravity.CENTER);
                ((LinearLayout)view).setOrientation(LinearLayout.VERTICAL);

                if (addvievs) {
                    view.addView(ee_image);
                    view.addView(ee_text);
                }

            } else if (currency_id.equals("glueckskaefer")) {

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                llp.setMargins(0, 20, 10, 0);
                ((LinearLayout)view).setOrientation(LinearLayout.HORIZONTAL);

                if (addvievs) {
                    view.addView(ee_text);
                    view.addView(ee_image);
                }

            } else {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.height = Utils.dpToPx(20 ,ctx);
                ee_image.setLayoutParams(params);
                ee_text.setGravity(Gravity.CENTER);
                ((LinearLayout)view).setOrientation(LinearLayout.VERTICAL);

                if (addvievs) {
                    view.addView(ee_image);
                    view.addView(ee_text);
                }
            }

            ee_image.setTag(currency_pic_url);
            new DownloadImageTask(ctx).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ee_image);

            Integer order = (Integer)view.getTag(R.string.order);

            boolean animate = true;

            if (order < size) {
                animate = false;
            }

            if (!update || !animate) {

                ee_text.setText( data.get_customer_points_available() );

            } else {

                animate_currency_view( view, data, currency_id );

            }
        }

        if (currency_changed) {
            currency_changed = false;
        }

    }

    private void animate_currency_view(ViewGroup wrapper, UserData data, String currency_id) {

        TextView ee_text = wrapper.findViewById(EE_TEXT_ID);

        ee_text.setVisibility(View.GONE);
        final TextView fintext = ee_text;

        final String customer_points = data.get_customer_points_available();

        Utils.animate_textview( ee_text, customer_points, 1000 );


        Animation scale = new ScaleAnimation(1, 1.5f, 1, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(1000);

        wrapper.startAnimation(scale);

        int last_update_points = data.get_last_update_points();


        final TextView tv = new TextView(ctx);

        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setText("+" + Integer.toString(  last_update_points) );
        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        if (currency_id.equals("glueckskaefer")) {
            wrapper.addView(tv, 0);
        } else {
            wrapper.addView(tv);
        }

        final TextView tv_fin = tv;

        tv.animate().setDuration(500)
            .translationY(-50)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    tv_fin.setVisibility(View.GONE);
                    fintext.setVisibility(View.VISIBLE);
                    Utils.animate_textview( fintext, customer_points, 1000 );
                }
            });
    }

    public void reinit() {
        if (this.act != null) {
            init(this.act, this.userId, this.userEmail);
        }
    }

    public void init(Activity act, String user_id, String user_email) {

        if ( act != null ) {
            this.act = act;
            this.ctx = act.getApplicationContext();
        }

        if (this.act == null || this.ctx == null) {
            return;
        }


        api_token = ctx.getString(R.string.api_token);
        //@TODO: implement version
        client = ctx.getString(R.string.client);

        if (socket_client == null) {
            socket_client = new RMWebsocketCommunicator( ctx, act, this );
        }

        ping_token = Utils.prefs_get_string( "ping_token", ctx );

        collector_token = Utils.prefs_get_string(EE_COLLECTOR_TOKEN_KEY, ctx);

        String msg = "{\"method\":\"init\",\"api_token\": \"" + api_token + "\",\"client\":\"" + client;

        if (collector_token != null) {
            msg += "\", \"collector_token\":\"" + collector_token;
        }

        if (user_id != null) {
            userId = user_id;
            msg += "\", \"remote_id\":\"" + user_id;
        }

        if (user_email != null) {
            userEmail = user_email;
            msg += "\", \"email\":\"" + user_email;
        }

        msg += "\"}";
        if (socket_client != null) {
            socket_client.send_message( msg );
        }
    }

    private void delete_collectortoken() {
        Utils.prefs_delete_string(EE_COLLECTOR_TOKEN_KEY, ctx);
    }

    public void logout() {
        delete_collectortoken();
    }

    public void on_message_received(String message) {

        try {
            JSONObject response = new JSONObject(message);

             if (response.has("caller")) {

                String caller = response.getString("caller");

                if (caller.equals("init")) {

                    if (response.has("data")) {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has( "collector_token" )) {
                            collector_token = data.getString( "collector_token" );
                            Utils.prefs_save_string(collector_token, EE_COLLECTOR_TOKEN_KEY, ctx);

                            if (socket_client != null) {
                                send_ping();
                                socket_client.send_message("{\"method\":\"me\"}");
                            }

                        }
                    }

                } else if (caller.equals("me")) {
                    user_data = new UserData(response);

                    if (user_data.is_currency_available() == false) {
                        user_data = null;
                        act.runOnUiThread(new Runnable() {
                            public void run() {
                                remove_currency_views();
                            }
                        });
                    }

                    if ( saved_view != null ) {
                        add_currency_view( saved_view );
                        saved_view = null;
                    }

                    if ( ee_currency_views.size() > 0 ) {

                        act.runOnUiThread(new Runnable() {
                            public void run() {
                                update_currency_views(ee_currency_views, user_data, false);
                            }
                        });
                    }
                } else if ( caller.equals("challenges") ) {

                    if (response.has("data")) {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has("steps")) {
                            JSONArray steps = data.getJSONArray("steps");
                            if (steps != null) {
                                trace_steps(steps);
                            }
                        }
                    }

                }

            } else if ( response.has("msg") ) {

                String msg = response.getString("msg");

                if (msg.equals("points_change")) {

                    if (response.has("data")) {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has("points_after")) {

                            int points = data.getInt("points_after");

                            user_data.update_points(points);

                            if ( ( ee_currency_views.size() > 0 ) && (user_data != null) ) {

                                act.runOnUiThread(new Runnable() {
                                    public void run() {
                                        update_currency_views(ee_currency_views, user_data, true);
                                    }
                                });
                            }
                        }
                    }
                } else if (msg.equals("currency_changed")) {

                    if (ActivityWebview.getInstance() != null) {
                        ActivityWebview.getInstance().finish();
                    }

                    if (socket_client != null) {
                        currency_changed = true;
                        socket_client.send_message("{\"method\":\"me\"}");
                    }
                } else if (msg.equals("popup_notify")) {

                    if ( user_data != null && user_data.is_currency_visible() == false ) {
                        return;
                    }

                    if (response.has("data") && !response.isNull("data")) {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has("notify_url") && !data.isNull("notify_url")) {

                            final String url = data.getString("notify_url");

                            if ( ActivityWebview.getInstance() != null) {
                                ActivityWebview.getInstance().finish();
                            }

                            show_dialog( url );

                        }
                    }

                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void send_ping() {
        if (ping_token != null && socket_client != null) {
            String msg = "{\"method\":\"ping\",\"step\":\"" + ping_token + "\"}";
            socket_client.send_message( msg );
        }
    }

    private void trace_steps(JSONArray steps) {

        int length = steps.length();

        for ( int i = 0; i < length; i++ ) {

            try {
                JSONObject obj = steps.getJSONObject(i);

                boolean deferred = false;
                String view_id = null;
                String view_type = null;
                String token = null;
                JSONObject matched_data = null;
                JSONObject data = null;
                CHALLENGE_EVENT event = null;
                String for_view  = null;

                if ( obj.has("token") ){
                    token = obj.getString("token");
                }

                if (obj.has("matched_data")) {
                    matched_data = obj.getJSONObject("matched_data");
                }

                if (obj.has("bind_to")) {
                    JSONObject bind_to = obj.getJSONObject("bind_to");

                    if (bind_to.has("view")) {
                        for_view = bind_to.getString("view");
                    }

                    if (bind_to.has("assert_container")) {
                        view_id = bind_to.getString("assert_container");
                    }

                    if (bind_to.has("assert_viewtype")) {
                        view_type = bind_to.getString("assert_viewtype");
                    }

                    if (bind_to.has("triggered_by")) {
                        JSONObject triggered_by = bind_to.getJSONObject("triggered_by");

                        if ( triggered_by.has("event") ){
                            String event_str = triggered_by.getString("event");

                            if ( event_str.equals("click") ) {
                                event = CHALLENGE_EVENT.CLICK;
                            } else if ( event_str.equals("scroll") ) {
                                event = CHALLENGE_EVENT.SCROLL;
                            }
                        }

                        if (triggered_by.has("data")) {
                            data = triggered_by.getJSONObject("data");
                        }
                    }
                }

                if (obj.has("deferred")) {
                    deferred = obj.getBoolean("deferred");

                    if (deferred) {

                        if (obj.has("token")) {

                            if ( ping_token == null ) {
                                String p_token = obj.getString("token");
                                ping_token = p_token;
                                Utils.prefs_save_string(ping_token, "ping_token", ctx);
                                send_ping();
                            }
                        }
                    }
                }

                if (view_id != null && view_type != null && token != null && event != null) {

                    Challenge challenge = new Challenge( view_id, view_type, deferred, token, event, matched_data, data );
                    challenge_map.put( view_id, challenge );

                    if (event == CHALLENGE_EVENT.SCROLL && data != null) {
                        handle_events_scrollview(for_view, challenge);
                    }
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void handle_events_scrollview(String for_view, final Challenge challenge) {

       try {

           JSONObject cfg = challenge.getData();


           if (cfg.has("threshold")) {
               final int threshold = cfg.getInt("threshold");

               String id = challenge.getView_id();
               View window = view_map.get(for_view);
               int resourceId = ctx.getResources().getIdentifier(id, "id", ctx.getPackageName());

               ScrollView sv = null;

               if (window != null) {
                   sv = window.findViewById(resourceId);
               }

               if (sv != null) {

                   final ScrollView sv_fin = sv;

                   ViewTreeObserver.OnScrollChangedListener victim = new ViewTreeObserver.OnScrollChangedListener() {
                       @Override
                       public void onScrollChanged() {
                           int pos = sv_fin.getScrollY();

                           if ( pos > threshold ) {

                               String token = challenge.getToken();
                               JSONObject data = challenge.getMatched_data();
                               String data_str = data.toString();
                               String message = "{\"method\":\"log\",\"data\":"+ data_str + ",\"step\":\""+ token +"\"}";
                               if (socket_client != null) {
                                   socket_client.send_message(message);
                               }

                               sv_fin.getViewTreeObserver().removeOnScrollChangedListener(this);
                           }
                       }
                   };

                   sv.getViewTreeObserver().addOnScrollChangedListener( victim );

               }

           }


       } catch (JSONException jex) {
           jex.printStackTrace();
       }
    }

}
