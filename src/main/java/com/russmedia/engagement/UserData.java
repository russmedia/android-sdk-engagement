package com.russmedia.engagement;


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

    private boolean currency_visible = true;

    private String level_name;
    private String level_desc;

    private boolean currency_available = true;


    public UserData(JSONObject obj) {

        try {

            if (obj.has("data")) {

                JSONObject data = obj.getJSONObject("data");

                if ( data.has("anonymous") ) {
                    customer_anonymous = data.getBoolean("anonymous");
                }

                if ( data.has("collector_token") ) {
                    customer_collector_token = data.getString("collector_token");
                }

                if ( data.has("points_accumulated") ) {
                    customer_points_accumulated = Integer.parseInt( data.getString("points_accumulated") ) ;
                }

                if ( data.has("points_available") ) {
                    customer_points_available = Integer.parseInt( data.getString("points_available") );
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

                    if (currency.has("picture")) {
                        currency_pic_url = currency.getString("picture");
                    }

                    if (currency.has("meta")) {
                        JSONArray currency_meta_arr = currency.getJSONArray("meta");
                        currency_id = Utils.parse_value( currency_meta_arr, "className" );
                    }

                    if ( currency.has("visible") ) {
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

        if ( updated_points > 0 ){
            customer_points_last_update = updated_points;
            customer_points_available = points ;
            customer_points_accumulated += customer_points_last_update;
        }
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

    public String get_customer_points_available() {
        return Integer.toString(customer_points_available);
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

    public String get_currency_id() {
        return currency_id;
    }

    public String get_customer_collector_token() {
        return customer_collector_token;
    }
}
