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
import com.hvl.dragonteam.Model.Announcement;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.Utilities.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rasim-pc on 10.10.2018.
 */

public class AnnouncementService {

    public void saveAnnouncement(Context context, final Announcement announcement, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(announcement, Announcement.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, URLs.urlSaveAnnouncement,new JSONObject(json),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("successSaveAnnouncement", response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorSaveAnnouncement", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }

    public void getAnnouncementList(Context context, Team team, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(team, Team.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        CustomJsonArrayRequest postRequest = new CustomJsonArrayRequest(Request.Method.POST, URLs.urlGetAnnouncementList, new JSONObject(json),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("successGetAnnouncementList", response.toString());
                        callback.onSuccessList(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errGetAnnouncementList", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }

}
