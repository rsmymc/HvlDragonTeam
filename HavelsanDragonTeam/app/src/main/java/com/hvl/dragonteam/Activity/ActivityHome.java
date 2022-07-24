package com.hvl.dragonteam.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.libraries.places.api.Places;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.hvl.dragonteam.Fragment.FragmentAnnouncement;
import com.hvl.dragonteam.Fragment.FragmentLineup;
import com.hvl.dragonteam.Interface.MyFunction;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Redis.ChatHistory;
import com.hvl.dragonteam.Redis.ChatMessage;
import com.hvl.dragonteam.Redis.RedisGroupChatProcess;
import com.hvl.dragonteam.Utilities.BottomNavigationViewHelper;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.URLs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ActivityHome extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initComponents();
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
                    if (!Constants.frgTeamPager.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgTeamPager, Constants.frgTagTeamPager)
                                .addToBackStack(Constants.frgTagTeamPager)
                                .commit();
                    }
                }

                if (item.getItemId() == R.id.action_stats) {
                    if (!Constants.frgStats.isAdded()) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, Constants.frgStats, Constants.frgTagStats)
                                .addToBackStack(Constants.frgTagStats)
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

            NotificationModel notificationModel = new Gson().fromJson(bundle.getString("notificationModel"), NotificationModel.class);
            String json = new Gson().toJson(notificationModel.getTraining(), Training.class);
            bundle.putString("OBJ", json);

            if (notificationModel.getNotificationType() == NotificationTypeEnum.LINEUP_NOTIFICATION.getValue()) {
                FragmentLineup fragmentLineup = new FragmentLineup();
                fragmentLineup.setArguments(bundle);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentLineup, "fragmentLineup")
                        .addToBackStack("fragmentLineup")
                        .commit();
            } else if (notificationModel.getNotificationType() == NotificationTypeEnum.CHAT_MESSAGE_NOTIFICATION.getValue()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, Constants.frgChat, Constants.frgTagChat)
                        .addToBackStack(Constants.frgTagChat)
                        .commit();
                Constants.bottomBar.setSelectedItemId(R.id.action_chat);
            } else if (notificationModel.getNotificationType() == NotificationTypeEnum.ANNOUNCEMENT_NOTIFICATION.getValue()) {
                FragmentAnnouncement fragmentAnnouncement = new FragmentAnnouncement();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentAnnouncement, "fragmentAnnouncement").addToBackStack("fragmentAnnouncement")
                        .commit();
            }

        }  else {
            Constants.bottomBar.setSelectedItemId(R.id.action_training);
        }

        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey);
        }

        registerChat();
    }

    private void registerChat() {
        String channel = Constants.REDIS_CHAT_PREFIX + Constants.personTeamView.getTeamId();
        ChatHistory chatHistory = new ChatHistory(channel);
        MyFunction<Collection<?>, Boolean> callback = new MyFunction<Collection<?>, Boolean>() {
            @Override
            public Boolean apply(Collection<?> chatList) {

                boolean result = chatHistory.add((List<String>) chatList);

                ActivityHome.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (result == true) {
                            long lastSeen = SharedPrefHelper.getInstance(ActivityHome.this).getLong(Constants.REDIS_CHAT_LAST_SEEN_PREFIX + Constants.personTeamView.getTeamId() + FirebaseAuth.getInstance().getCurrentUser().getUid());
                            if(lastSeen < chatHistory.getOrderedChatMessageList().last().getTime()){
                                Constants.bottomBar.getOrCreateBadge(R.id.action_chat).setVisible(true);

                            } else {
                                Constants.bottomBar.getOrCreateBadge(R.id.action_chat).setVisible(false);
                            }
                        }
                    }
                });
                return result;
            }
        };

        RedisGroupChatProcess chatProcess = new RedisGroupChatProcess(URLs.redisAddress);
        chatProcess.init();
        chatProcess.subscribe(channel, callback);

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
            if (getIntent().getStringExtra("from") == null || !getIntent().getStringExtra("from").equals("activityTeam")) {
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
            } else {
                finish();
            }
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

