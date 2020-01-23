package com.russmedia.engagement.helper;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.russmedia.engagement.EngagementEngine;
import com.russmedia.engagement.classes.UserData;
import com.russmedia.engagement.listener.AnimatorListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static int dpToPx(int dp, Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static boolean matches(String string_to_check, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string_to_check);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getQueryFromParams(HashMap<String, String> params) {
        Uri.Builder builder = new Uri.Builder();

        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            String key = (String)pair.getKey();
            String value = (String)pair.getValue();
            builder.appendQueryParameter(key, value);
        }

        String query = builder.build().getEncodedQuery();
        if (!query.startsWith("&")) {
            query = "&" + query;
        }
        return query;
    }

    public static String parse_value(JSONArray array, String key) {

        try {

            if (array.length() > 0) {

                for (int i = 0; i <= array.length(); i++) {
                    JSONObject currency_meta = array.getJSONObject(i);

                    if (currency_meta.has("key")) {
                        String key_str = currency_meta.getString("key");
                        if (key_str.equals(key)) {
                            if (currency_meta.has("value")) {
                                String value = currency_meta.getString("value");
                                return value;
                            }
                        }
                    }
                }

            }

        } catch (JSONException jex) {
            return null;
        }

        return null;

    }

    public static void prefs_save_string(String string, String key, Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().putString(key, string).commit();
    }

    public static void prefsDeleteString(String key, Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().remove(key).commit();
    }

    public static String prefs_get_string(String key, Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(key, null);
    }

    public static void runUpdateAnimation(Context ctx, ViewGroup wrapper, UserData data) {
        int pointsUpdate = data.get_last_update_points();

        TextView ee_text = wrapper.findViewById(EngagementEngine.EE_TEXT_ID);
        String updatedPoints = Integer.toString(data.get_customer_points_available());
        ee_text.setVisibility(View.GONE);


        TextView tv = new TextView(ctx);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setText((pointsUpdate > 0 ? "+" : "-") + Integer.toString(Math.abs(pointsUpdate)));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);

        wrapper.addView(tv);
        tv.animate().setDuration(500).translationY(-50).setListener(new AnimatorListener(tv, ee_text, updatedPoints));
    }

    public static void runScaleAnimation(ViewGroup wrapper, int animationDurationMsec) {
        Animation scale = new ScaleAnimation(1, 1.2f, 1, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(animationDurationMsec);
        wrapper.startAnimation(scale);
    }

    public static String setThousandsSeperator(int nr, String seperator) {

        if ( nr <= 0 || nr > Integer.MAX_VALUE) {
            return "0";
        }

        String nr_str = String.valueOf(nr);

        int length = nr_str.length();

        StringBuilder  builder = new StringBuilder();

        int dec = 0;
        int mod = nr_str.length() % 3;
        for ( int i = ( length - 1) ; i >= 0; i-- ) {

            builder.insert( 0, nr_str.charAt(i) );
            dec++;

            if (dec == 3 && i > 0) {
                builder.insert( 0, seperator );
                dec = 0;
            }
        }

        return builder.toString();
    }

    public static String getUrlParam(String url, String param) {
        Uri uri=Uri.parse(url);
        String value = uri.getQueryParameter(param);

        return value;
    }

}
