package com.hvl.dragonteam.DataService;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.hvl.dragonteam.Interface.CustomJsonArrayRequest;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.Utilities.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rasim-pc on 10.10.2018.
 */

public class StatsService {

    public void getStatsByPerson(Context context, final Team team, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(team, Team.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        CustomJsonArrayRequest postRequest = new CustomJsonArrayRequest(Request.Method.POST, URLs.urlGetStatsByPerson,new JSONObject(json),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("successGetStatsByPerson", response.toString());
                        callback.onSuccessList(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorGetStatsByPerson", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }
}
