package com.russmedia.engagement.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserData {

    private boolean customer_anonymous;
    private String customer_collector_token;
    private int customer_points_accumulated;
    private int customer_points_available;
    private int customer_points_last_update;
    private String customer_name;
    private String customer_pic;
    private String customer_badge;

    private String currency_id;
    private String currency_name;
    private String currency_desc;
    private String currency_pic_url;
    private String currency_pic_url_navigation;

    private boolean currency_visible = true;

    private String level_name;
    private String level_desc;

    private boolean currency_available = true;


    public UserData(JSONObject obj) {

        try {

            if (obj.has("data")) {

                JSONObject data = obj.getJSONObject("data");

                if (data.has("anonymous")) {
                    customer_anonymous = data.getBoolean("anonymous");
                }

                if (data.has("collector_token")) {
                    customer_collector_token = data.getString("collector_token");
                }

                if (data.has("points_accumulated")) {
                    customer_points_accumulated = Integer.parseInt(data.getString("points_accumulated"));
                }

                if (data.has("points_available")) {
                    customer_points_available = Integer.parseInt(data.getString("points_available"));
                }

                if (data.has("customer")) {
                    JSONObject customer = data.getJSONObject("customer");

                    if (customer.has("name")) {
                        customer_name = customer.getString("name");
                    }

                    if (customer.has("picture")) {
                        customer_pic = customer.getString("picture");
                    }
                }

                if (data.has("currency") && !data.isNull("currency")) {
                    JSONObject currency = data.getJSONObject("currency");

                    if (currency.has("name")) {
                        currency_name = currency.getString("name");
                    }

                    if (currency.has("description")) {
                        currency_desc = currency.getString("description");
                    }

                    if (currency.has("icons")) {
                        JSONObject icons = currency.getJSONObject("icons");
                        if (icons.has("default")) {
                            JSONObject defaultIcon = icons.getJSONObject("default");
                            if (defaultIcon.has("url")) {
                                currency_pic_url = defaultIcon.getString("url");
                                currency_pic_url_navigation = defaultIcon.getString("url");
                            }
                        }
                        if (icons.has("navigation")) {
                            JSONObject navigationIcon = icons.getJSONObject("navigation");
                            if (navigationIcon.has("url")) {
                                currency_pic_url_navigation = navigationIcon.getString("url");
                            }
                        }
                    }

                    //@Deprecated
                    if (currency_pic_url == null && currency.has("media")) {
                        JSONArray media = currency.getJSONArray("media");
                        if (media.length() > 0) {
                            JSONObject mediaEntryFirst = media.getJSONObject(0);
                            if (mediaEntryFirst.has("conversions")) {
                                JSONObject conversions = mediaEntryFirst.getJSONObject("conversions");
                                if (conversions.has("thumb")) {
                                    currency_pic_url = conversions.getString("thumb");
                                    currency_pic_url_navigation = conversions.getString("thumb");
                                }
                            }
                        }
                    }

                    if (currency.has("basename")) {
                        currency_id = currency.getString("basename");
                    }

                    if (currency.has("visible")) {
                        currency_visible = currency.getBoolean("visible");
                    }
                } else {
                    currency_available = false;
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void update_points(int points) {

        int updated_points = points - customer_points_available;

        if (updated_points != 0) {
            customer_points_last_update = updated_points;
        }
        customer_points_available = points;
        customer_points_accumulated += customer_points_last_update;
    }

    public int get_last_update_points() {
        return customer_points_last_update;
    }

    /*
    public void increase_points(int points) {
        int int_customer_points_available = Integer.parseInt( customer_points_available );
        int_customer_points_available = int_customer_points_available + points;
        customer_points_available = Integer.toString( int_customer_points_available );


        int int_customer_points_accumulated = Integer.parseInt( customer_points_accumulated );
        int_customer_points_accumulated = int_customer_points_accumulated + points;
        customer_points_accumulated = Integer.toString( int_customer_points_accumulated );

    }
    */

    public int get_customer_points_available() {
        return customer_points_available;
    }

    public boolean is_currency_visible() {
        return currency_visible;
    }

    public boolean is_currency_available() {
        return currency_available;
    }

    public String get_currency_pic_url() {
        return currency_pic_url;
    }

    public String get_currency_pic_url_navigation() {
        return currency_pic_url_navigation;
    }

    public String get_currency_id() {
        return currency_id;
    }

    public String get_customer_collector_token() {
        return customer_collector_token;
    }
}
