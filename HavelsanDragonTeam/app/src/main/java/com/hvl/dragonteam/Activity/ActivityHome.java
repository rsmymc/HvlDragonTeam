package com.hvl.dragonteam.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.hvl.dragonteam.DataService.PersonService;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.BottomNavigationViewHelper;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivityHome extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PersonTeamService personTeamService = new PersonTeamService();//TODO takım seçimi yapılan ekranda çağrılacak
        PersonTeam personTeam = new PersonTeam();
        personTeam.setPersonId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        personTeam.setTeamId(Constants.TEAM_ID);

        try {
            personTeamService.getPersonTeam(ActivityHome.this,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            PersonTeam _personTeam = new Gson().fromJson(result.toString(), PersonTeam.class);
                            Constants.personTeam = _personTeam;

                            initComponents();

                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(ActivityHome.this);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityHome.this);
        }
    }

    private void initComponents() {

        Constants.setInitialValues();
        Constants.bottomBar = (BottomNavigationView) findViewById(R.id.bottomBar);
        BottomNavigationViewHelper.disableShiftMode(Constants.bottomBar);
        Constants.bottomBar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                if (item.getItemId() == R.id.action_training) {
                    if (!Constants.frgTrainingPager.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgTrainingPager, Constants.frgTagTrainingPager)
                                .addToBackStack(Constants.frgTagTrainingPager)
                                .commit();
                    }
                }

                if (item.getItemId() == R.id.action_team) {
                    if (!Constants.frgTeam.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgTeam, Constants.frgTagTeam)
                                .addToBackStack(Constants.frgTagTeam)
                                .commit();
                    }
                }

                if (item.getItemId() == R.id.action_stats) {
                    if (!Constants.frgEquipment.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgEquipment, Constants.frgTagEquipment)
                                .addToBackStack(Constants.frgTagEquipment)
                                .commit();
                    }
                }

                if (item.getItemId() == R.id.action_chat) {
                    if (!Constants.frgChat.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgChat, Constants.frgTagChat)
                                .addToBackStack(Constants.frgTagChat)
                                .commit();
                    }
                }
                if (item.getItemId() == R.id.action_profile) {
                    if (!Constants.frgProfile.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgProfile, Constants.frgTagProfile)
                                .addToBackStack(Constants.frgTagProfile)
                                .commit();
                    }
                }


                return true;
            }
        });

        Bundle bundle = getIntent().getExtras();


        if (bundle != null && bundle.getString("DIRECT", "").equals("NOTIFICATION")) {

           /* ShuttleNotificationModel shuttleNotificationModel = new Gson().fromJson(bundle.getString("shuttleNotificationModel"), ShuttleNotificationModel.class);
            String json = new Gson().toJson(shuttleNotificationModel.getTransportation(), Transportation.class);
            bundle.putString("OBJ", json);

            if (shuttleNotificationModel.getShuttleNotificationType() == NotificationTypeEnum.TRANSPORTATION_NOTIFICATION.getValue()) {
                FragmentJoinedShuttlePager fragmentJoinedShuttlePager = new FragmentJoinedShuttlePager();
                fragmentJoinedShuttlePager.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentJoinedShuttlePager, "fragmentJoinedShuttlePager").addToBackStack("fragmentJoinedShuttlePager")
                        .commit();
            } else if (shuttleNotificationModel.getShuttleNotificationType() == NotificationTypeEnum.CHAT_MESSAGE_NOTIFICATION.getValue()) {
                FragmentChat fragmentChat = new FragmentChat();
                fragmentChat.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentChat, "fragmentChat").addToBackStack("fragmentChat")
                        .commit();
            } else if (shuttleNotificationModel.getShuttleNotificationType() == NotificationTypeEnum.LOCATION_NOTIFICATION.getValue()) {
                Constants.frgChat.setArguments(bundle);
                Constants.bottomBar.setSelectedItemId(R.id.action_active_shuttle);
            }
*/
        }  else if (bundle != null && bundle.getString("DIRECT", "").equals("CHAT")) {
            /*FragmentChat fragmentChat = new FragmentChat();
            fragmentChat.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragmentChat, "fragmentChat").addToBackStack("fragmentChat")
                    .commit();*/
        } else {
            if (bundle != null)
                Constants.frgChat.setArguments(bundle);
            Constants.bottomBar.setSelectedItemId(R.id.action_training);
        }


    }

    @Override
    public void onBackPressed() {

        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount > 1) {
            getSupportFragmentManager().popBackStack();
            if (backStackCount > 1) {
                String tag = getSupportFragmentManager().getBackStackEntryAt(backStackCount - 2).getName();

                if (Constants.mainFragmentTags.contains(tag)) {
                    Constants.bottomBar.getMenu().getItem(Constants.mainFragmentTags.indexOf(tag)).setChecked(true);
                    getSupportFragmentManager().beginTransaction()
                            .show(Constants.mainFragments.get(Constants.mainFragmentTags.indexOf(tag))).commit();
                    Constants.mainFragments.get(Constants.mainFragmentTags.indexOf(tag)).onResume();
                } else {
                    for (Fragment frg : getSupportFragmentManager().getFragments()) {
                        if (frg != null) {
                            if (frg.getTag().equals(tag)) {
                                getSupportFragmentManager().beginTransaction()
                                        .show(frg).commit();
                                frg.onResume();
                            }
                        }
                    }
                }
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this);
            builder.setMessage(getString(R.string.question_exit))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    //@SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}

