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
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.Utilities.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rasim-pc on 10.10.2018.
 */

public class PersonTeamService {

    public void savePersonTeam(Context context, final PersonTeam personTeam, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(personTeam, PersonTeam.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, URLs.urlSavePersonTeam,new JSONObject(json),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("successSavePersonTeam", response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorSavePersonTeam", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }

    public void deletePersonTeam(Context context, final PersonTeam personTeam, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(personTeam, PersonTeam.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, URLs.urlDeletePersonTeam,new JSONObject(json),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("successDeletePersonTeam", response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorDeletePersonTeam", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }

    public void getPersonTeam(Context context, PersonTeam personTeam, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(personTeam, PersonTeam.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, URLs.urlGetPersonTeam,new JSONObject(json),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("successGetPersonTeam", response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorGetPersonTeam", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }

    public void getPersonListByTeam(Context context, final Team team, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(team, Team.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        CustomJsonArrayRequest postRequest = new CustomJsonArrayRequest(Request.Method.POST, URLs.urlGetPersonListByTeam,new JSONObject(json),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("successGetPersonListByTeam", response.toString());
                        callback.onSuccessList(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorGetPersonListByTeam", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }

    public void getTeamListByPerson(Context context, final Person person, final VolleyCallback callback) throws JSONException {

        String json = new Gson().toJson(person, Person.class);

        RequestQueue queue = Volley.newRequestQueue(context);

        CustomJsonArrayRequest postRequest = new CustomJsonArrayRequest(Request.Method.POST, URLs.urlGetTeamListByPerson,new JSONObject(json),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("successGetTeamListByPerson", response.toString());
                        callback.onSuccessList(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("errorGetTeamListByPerson", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) ;
        queue.add(postRequest);
    }
}
