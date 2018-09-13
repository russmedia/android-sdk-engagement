package com.russmedia.engagement;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static void prefs_delete_string(String key, Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit().remove(key).commit();
    }

    public static String prefs_get_string(String key, Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx).getString(key, null);
    }

    public static void animate_textview( TextView view, String end_str, int duration_msec ) {

        try {
            int start = Integer.parseInt( view.getText().toString() );
            int end = Integer.parseInt(end_str);

            if (end <= start) {
                return;
            }

            final TextView fin_textview = view;

            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.setDuration(duration_msec);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    fin_textview.setText(animation.getAnimatedValue().toString());
                }
            });
            animator.start();



        } catch (Exception e) {
            return;
        }



    }

}
