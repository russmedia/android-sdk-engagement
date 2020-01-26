package com.russmedia.engagement;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.russmedia.engagement.activities.ActivityDialog;
import com.russmedia.engagement.activities.ActivityWebview;
import com.russmedia.engagement.classes.Challenge;
import com.russmedia.engagement.classes.OpenMeViewRequest;
import com.russmedia.engagement.classes.UserData;
import com.russmedia.engagement.helper.BMPCache;
import com.russmedia.engagement.helper.Utils;
import com.russmedia.engagement.listener.LifecycleListener;
import com.russmedia.engagement.listener.OnScrollChanged;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class EngagementEngine {

    private static final String TAG = "EngagementEngine";
    public enum CHALLENGE_EVENT { CLICK, SCROLL, SELECT }

    private static final EngagementEngine ourInstance = new EngagementEngine();
    private static final String EE_COLLECTOR_TOKEN_KEY = "ee_collector_token";

    public static final int EE_TEXT_ID = 0;
    public static final int EE_IMG_ID = 1;

    private RMWebsocketCommunicator socket_client = null;

    private Activity act;
    private Context ctx;

    private String userId;
    private String userName;

    private String api_token;
    private String client;

    private String collector_token;

    private UserData user_data;

    private HashMap<Integer, ViewGroup> ee_currency_views;

    private HashMap<String, View> view_map;
    private HashMap<String, Challenge> challenge_map;

    private String ping_token;
    private ViewGroup saved_view = null;
    private boolean currency_changed = false;

    private String frontendUrl;
    private String frontendEntrypoint;
    private RMLifecycleHandler lifecycleHandler;
    private LifecycleListener lifecycleListener;

    private OpenMeViewRequest openMeViewRequest;
    private HashMap<String, String> getParams;

    private String popupUrl;
    private String lang;

    private int displayedPoints = 0;

    private boolean dev;

    public static EngagementEngine getInstance() {
        return ourInstance;
    }

    private EngagementEngine() {
        view_map = new HashMap<>(0);
        challenge_map = new HashMap<>(0);
        ee_currency_views = new HashMap<>(0);
        dev = false;
    }

    public void stopEngine() {
        //@TODO: clean up
        socket_client.close("stopEngine");
        socket_client = null;
    }

    public void fireChallenge(int view_ressource_id ) {

        if (socket_client == null) {
            return;
        }

        String view_ressource_name = getViewId(ctx, view_ressource_id);

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

    private void sendLog(String token) {
        if (token != null && socket_client != null) {
            String message = "{\"method\":\"log\" ,\"step\":\""+ token + "\"}";
            socket_client.send_message( message );
        }
    }

    private String getViewId(Context ctx, int view_id) {
        String viewID = null;
        String pkg_name = ctx.getPackageName();

        try {
            viewID = ctx.getResources().getResourceName(view_id);
            viewID = viewID.replace(pkg_name + ":id/", "");
            viewID = viewID.replace("android:id/", "");
        } catch (Resources.NotFoundException nfx) {}

        return viewID;
    }

    public void setup(Application application) {
        lifecycleHandler = new RMLifecycleHandler();
        application.registerActivityLifecycleCallbacks(lifecycleHandler);
        lifecycleListener = new LifecycleListener() {
            @Override
            public void onBecameForeground() {
                reinit();
                sendPing();
            }

            @Override
            public void onBecameBackground() {
                sendPing();
                stopEngine();
            }
        };
        lifecycleHandler.addListener(lifecycleListener);
    }

    /**
     *
     * @param layout        the view to which the challenges are bound
     * @param challengeUrl         url to which challenges are bound
     * @param viewId          id of the view to which challenges are bound
     */

    public void registerChallengesFor(View layout, String challengeUrl, String viewId) {

        if (socket_client == null) {
            return;
        }

        String url = challengeUrl;
        String id = viewId;

        view_map.put(id, layout);

        String message = "";

        if (challengeUrl == null) {
            message = "{\"method\":\"challenges\",\"for\":{\"view\" : \"" + id + "\"}}";
        } else {
            message = "{\"method\":\"challenges\",\"for\":{\"view\" : \"" + id + "\",  \"id\" : \"" + url + "\"}}";
        }

        socket_client.send_message( message );

    }

    private void sendEmptyChallenge() {
        if (socket_client != null) {
            String message = "{\"method\":\"challenges\",\"for\":\"\"}";
            socket_client.send_message(message);
        }
    }

    private void removeCurrencyViews() {

        for (ViewGroup view : ee_currency_views.values()) {
            view.removeAllViews();
        }

    }

    public void addCurrencyView(ViewGroup view) {


        if ( view != null ) {

            int view_id = view.getId();


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleDeepLink(null,"");
                }
            });

            view.setTag(R.string.rm_ee_order, ee_currency_views.size() + 1 );
            ee_currency_views.put(view_id, view );

            if ( user_data != null ) {
                act.runOnUiThread(new Runnable() {
                    public void run() {
                        updateCurrencyViews( ee_currency_views, user_data, false );
                    }
                });
            }
        }
    }

    public void handleDeepLink(OpenMeViewRequest openMeViewRequest) {
        if (openMeViewRequest != null) {
            handleDeepLink(openMeViewRequest.getEntryPoint(), openMeViewRequest.getAdditionalParams());
        }
    }

    public void handleDeepLink(String deepLink) {
        Intent intent = new Intent(act, ActivityWebview.class);
        Bundle b = new Bundle();
        b.putString("urlToLoad", deepLink);
        intent.putExtras(b);
        act.startActivity(intent);
    }

    public void handleDeepLink(String entryPoint, String additionalParams) {
        if (user_data != null) {

            String token = user_data.get_customer_collector_token();

            Intent intent = new Intent(act, ActivityWebview.class);
            Bundle b = new Bundle();

            if (additionalParams == null) {
                additionalParams = "";
            }

            if (additionalParams.length() > 0 && !additionalParams.startsWith("&")) {
                additionalParams = "&" + additionalParams;
            }

            if (frontendUrl != null && frontendEntrypoint != null && token != null) {
                String finalEntrypoint = frontendEntrypoint;
                if (entryPoint != null) {
                    finalEntrypoint = entryPoint;
                }

                String queryParams = "";

                if (this.getParams != null && this.getParams.size() > 0) {
                    queryParams = Utils.getQueryFromParams(this.getParams);
                }

                String url = frontendUrl + finalEntrypoint + "?collector_token=" + token + "&platform=android" + additionalParams + queryParams;

                Log.i(TAG, url);

                b.putString("urlToLoad", url);
                intent.putExtras(b);
                act.startActivity(intent);
            }
        } else {
            openMeViewRequest = new OpenMeViewRequest(entryPoint, additionalParams);
        }
    }

    public void showDialog(String url) {

        popupUrl = null;
        Intent intent = new Intent(act, ActivityDialog.class);
        Bundle b = new Bundle();

        b.putString("urlToLoad", url);
        intent.putExtras(b);
        act.startActivity(intent);
    }


    private void updateCurrencyViews(HashMap<Integer, ViewGroup> nav_list, UserData data, boolean update) {

        if (data == null) {
            return;
        }

        String currency_id = data.get_currency_id();
        String currency_pic_url = data.get_currency_pic_url_navigation();

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


            //LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //params.height = Utils.dpToPx(20 ,ctx);
            //ee_image.setLayoutParams(params);
            ee_image.setPadding(0, 0, 10, 0);
            ee_text.setGravity(Gravity.CENTER);
            //((LinearLayout)view).setOrientation(LinearLayout.VERTICAL);

            if (addvievs) {
                view.addView(ee_image);
                view.addView(ee_text);

            }

            ee_image.setTag(currency_pic_url);

            if (!isBmpCacheEmpty(currency_pic_url)) {
                ee_image.setImageBitmap(getBmpCache(currency_pic_url));

            } else {
                new DownloadImageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ee_image);
            }


            Integer order = (Integer)view.getTag(R.string.rm_ee_order);

            boolean animate = false;

            if (order >= size) {
                animate = true;
            }

            String points = Integer.toString(data.get_customer_points_available());
            String nr = Utils.setThousandsSeperator(Integer.parseInt(points), ctx.getString(R.string.rm_ee_thousand_seperator));
            view.setVisibility(View.VISIBLE);
            ee_text.setText(nr);
            if (update && animate) {
                animateCurrencyView(view, data);
            }
        }

        if (currency_changed) {
            currency_changed = false;
        }

    }

    private void animateCurrencyView(ViewGroup wrapper, UserData data) {

        int pointsUpdate = data.get_last_update_points();

        if (pointsUpdate == 0) {
            return;
        }

        int animationDurationMsec = 1000;
        Utils.runUpdateAnimation(ctx, wrapper, data);
        Utils.runScaleAnimation(wrapper, animationDurationMsec);

    }

    public void updateLanguage(String language) {
        this.lang = language;
        reinit();
    }

    public void reinit() {
        if (this.act != null) {
            initConnection(this.act, this.userId, this.userName);
        }
    }

    public void addGetParams(HashMap<String, String> getParams) {
        this.getParams = getParams;
    }


    public void initConnection(Activity act, String userId, String userName, boolean isDev) {
        this.dev = isDev;
        initConnection(act, userId, userName);
    }

    /**
     * @param act                   is the current activity
     * @param userId               if an login happened pass user id here, otherwise pass null
     * @param userName            if an login happened pass user email here, otherwise pass null
     */

    public void initConnection(Activity act, String userId, String userName) {

        if ( act != null ) {
            this.act = act;
            this.ctx = act.getApplicationContext();
        }

        if (this.act == null || this.ctx == null) {
            return;
        }

        api_token = ctx.getString(R.string.rm_ee_api_token);
        //@TODO: implement version
        client = ctx.getString(R.string.rm_ee_client);

        if (socket_client == null) {
            socket_client = new RMWebsocketCommunicator( ctx, act, dev, this );
        }

        ping_token = Utils.prefs_get_string( "ping_token", ctx );

        collector_token = Utils.prefs_get_string(EE_COLLECTOR_TOKEN_KEY, ctx);

        String msg = "{\"method\":\"init\",\"api_token\": \"" + api_token + "\",\"client\":\"" + client;

        if (collector_token != null) {
            msg += "\", \"collector_token\":\"" + collector_token;
        }

        if (userId != null) {
            this.userId = userId;
            msg += "\", \"remote_id\":\"" + userId;
            msg += "\", \"update_id\":\"" + "true";
        }

        if (userName != null) {
            this.userName = userName;
            msg += "\", \"username\":\"" + userName;
        }

        if (lang != null) {
            msg += "\", \"lang\":\"" + lang;
        }

        msg += "\"}";
        if (socket_client != null) {
            socket_client.send_message( msg );
        }
    }

    public void handlePushRegistration(String pushToken) {
        if (collector_token != null && pushToken != null) {
            String message = "{\"method\":\"registerDevice\",\"collector_token\":\"" + collector_token + "\",\"device_token\":\"" + pushToken + "\",\"platform\":\"android\"}";
            socket_client.send_message(message);
        }
    }

    public void handlePushRegistration(String pushToken, String pushAlias) {
        if (collector_token != null && pushToken != null) {
            String message = "{\"method\":\"registerDevice\",\"collector_token\":\"" + collector_token + "\",\"device_token\":\"" + pushToken + "\",\"push_alias\":\"" + pushAlias + "\",\"platform\":\"android\"}";
            socket_client.send_message(message);
        }
    }

    private void deleteCollectortoken() {
        Utils.prefsDeleteString(EE_COLLECTOR_TOKEN_KEY, ctx);
    }

    private void removeUser() {
        userId = null;
        userName = null;
    }

    public void logout() {
        deleteCollectortoken();
        removeUser();
        initConnection(act, null, null);
    }

    public void onMessageReceived(String message) {

        if (popupUrl != null) {
            showDialog(popupUrl);
        }

        try {
            JSONObject response = new JSONObject(message);

            if (response.has("msg")) {
                String msg = response.getString("msg");
                if (msg.equals("points_change")) {
                    if (response.has("data")) {
                        JSONObject data = response.getJSONObject("data");
                        if (data.has("points_after")) {
                            int pointsAfter = data.getInt("points_after");
                            if (pointsAfter != displayedPoints) {
                                displayedPoints = pointsAfter;
                                user_data.update_points(pointsAfter);
                                if ( ( ee_currency_views.size() > 0 ) && (user_data != null) ) {
                                    act.runOnUiThread(new Runnable() {
                                        public void run() {
                                            updateCurrencyViews(ee_currency_views, user_data, true);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }

            if (response.has("caller")) {

                String caller = response.getString("caller");

                if (caller.equals("init")) {

                    if (response.has("data")) {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has("collector_token")) {
                            collector_token = data.getString("collector_token");
                            Utils.prefs_save_string(collector_token, EE_COLLECTOR_TOKEN_KEY, ctx);

                            if (socket_client != null) {
                                sendPing();
                                socket_client.send_message("{\"method\":\"me\"}");
                                sendEmptyChallenge();
                            }

                        }

                        if (data.has("frontend_url") && data.has("frontend_entrypoint")) {
                            frontendUrl = data.getString("frontend_url");
                            frontendEntrypoint = data.getString("frontend_entrypoint");
                        }
                    }

                } else if (caller.equals("me")) {
                    user_data = new UserData(response);

                    if (user_data.is_currency_available() == false) {
                        user_data = null;
                        act.runOnUiThread(new Runnable() {
                            public void run() {
                                removeCurrencyViews();
                            }
                        });
                    }

                    if ( saved_view != null ) {
                        addCurrencyView( saved_view );
                        saved_view = null;
                    }

                    if ( ee_currency_views.size() > 0 ) {

                        act.runOnUiThread(new Runnable() {
                            public void run() {
                                updateCurrencyViews(ee_currency_views, user_data, false);
                            }
                        });
                    }

                    if (openMeViewRequest != null){
                        handleDeepLink(openMeViewRequest);
                        openMeViewRequest = null;
                    }

                } else if ( caller.equals("challenges") ) {

                    if (response.has("data")) {
                        JSONObject data = response.getJSONObject("data");

                        if (data.has("steps")) {
                            JSONArray steps = data.getJSONArray("steps");
                            if (steps != null) {
                                traceSteps(steps);
                            }
                        }
                    }

                }

            } else if ( response.has("msg") ) {

                String msg = response.getString("msg");

                if (msg.equals("currency_changed")) {

                    if (ActivityWebview.getInstance() != null) {
                        ActivityWebview.getInstance().finish();
                    }

                    if (socket_client != null) {
                        currency_changed = true;
                        socket_client.send_message("{\"method\":\"me\"}");
                    }
                } else if (msg.equals("popup_notify")) {

                    popupUrl = getNotifyUrl(response);
                    if (canShowPopup() && popupUrl != null) {
                        hideWebview();
                        showDialog(popupUrl);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getNotifyUrl(JSONObject response) {
        String result = null;
        try {
            if (response.has("data") && !response.isNull("data")) {
                JSONObject data = response.getJSONObject("data");
                if (data.has("notify_url") && !data.isNull("notify_url")) {
                    result = data.getString("notify_url");
                }
            }
        } catch (JSONException jex){ }
        return result;
    }

    private void hideWebview() {
        if ( ActivityWebview.getInstance() != null) {
            ActivityWebview.getInstance().finish();
        }
    }

    private boolean canShowPopup() {

        if (user_data != null && !user_data.is_currency_visible()) {
            return false;
        }

        if (lifecycleHandler.isBackground()) {
            return false;
        }

        return true;
    }

    public void sendPing() {
        if (ping_token != null && socket_client != null) {
            String msg = "{\"method\":\"ping\",\"step\":\"" + ping_token + "\"}";
            socket_client.send_message( msg );
        }
    }

    private void traceSteps(JSONArray steps) {

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


                    if (obj.has("token")) {

                        String deferredToken = obj.getString("token");

                        if (deferred) {
                            ping_token = deferredToken;
                            Utils.prefs_save_string(ping_token, "ping_token", ctx);
                            sendPing();
                        } else if (!deferred && view_id == null) {
                            sendLog(deferredToken);
                        }
                    }

                }

                if (view_id != null && view_type != null && token != null && event != null) {

                    Challenge challenge = new Challenge( view_id, view_type, deferred, token, event, matched_data, data );
                    challenge_map.put( view_id, challenge );

                    if (event == CHALLENGE_EVENT.SCROLL && data != null) {
                        handleEventsScrollview(for_view, challenge);
                    }
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleEventsScrollview(String for_view, Challenge challenge) {

        try {

            JSONObject cfg = challenge.getData();

            if (cfg.has("threshold")) {
                int threshold = cfg.getInt("threshold");

                String id = challenge.getView_id();
                View window = view_map.get(for_view);
                int resourceId = ctx.getResources().getIdentifier(id, "id", ctx.getPackageName());

                ScrollView sv = null;

                if (window != null) {
                    sv = window.findViewById(resourceId);
                }

                if (sv != null) {
                    sv.getViewTreeObserver().addOnScrollChangedListener( new OnScrollChanged(sv, threshold, challenge, socket_client));
                }
            }

        } catch (JSONException jex) {
            jex.printStackTrace();
        }
    }



    private Boolean isBmpCacheEmpty(String url) {

        Bitmap bmp = BMPCache.getInstance().getBitmapFromMemCache(url);

        return isBitmapEmpty(bmp);

    }

    private Bitmap getBmpCache(String url) {
        return BMPCache.getInstance().getBitmapFromMemCache(url);
    }

    private Boolean isBitmapEmpty(Bitmap bmp) {

        try {
            Bitmap emptyBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
            return bmp.sameAs(emptyBitmap);
        } catch (Exception e) {
            return true;
        }
    }

    public String getPicUrl() {
        if (user_data != null && user_data.get_currency_pic_url() != null) {
            return user_data.get_currency_pic_url();
        } else return null;
    }


}
