package com.russmedia.engagement.classes;


import com.russmedia.engagement.EngagementEngine;

import org.json.JSONObject;

public class Challenge {

    private String view_id;
    private String view_type;
    private boolean deferred;
    private String token;
    private EngagementEngine.CHALLENGE_EVENT challenge_event;
    private JSONObject matched_data;
    private JSONObject data;

    public Challenge(String view_id, String view_type, boolean deferred, String token, EngagementEngine.CHALLENGE_EVENT challenge_event, JSONObject matched_data, JSONObject data) {
        this.view_id = view_id;
        this.view_type = view_type;
        this.deferred = deferred;
        this.token = token;
        this.challenge_event = challenge_event;
        this.matched_data = matched_data;
        this.data = data;
    }


    public String getView_id() {
        return view_id;
    }

    public String getView_type() {
        return view_type;
    }

    public boolean isDeferred() {
        return deferred;
    }

    public String getToken() {
        return token;
    }
    public EngagementEngine.CHALLENGE_EVENT getChallenge_event() {
        return challenge_event;
    }

    public JSONObject getMatched_data() {
        return matched_data;
    }

    public JSONObject getData() {
        return data;
    }
}
