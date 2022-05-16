package com.hvl.dragonteam.Interface;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by rasim-pc on 20.10.2018.
 */

public interface VolleyCallback {
    void onSuccess(JSONObject result);
    void onSuccessList(JSONArray result);
    void onError(String result);

}
