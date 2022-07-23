package com.hvl.dragonteam.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.hvl.dragonteam.DataService.PersonService;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.LanguageEnum;
import com.hvl.dragonteam.Model.Enum.RemoteDialogTypeEnum;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.Model.RemoteDialogModel;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivitySplashScreen extends AppCompatActivity {

    private SharedPrefHelper sharedPrefHelperInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedPrefHelperInstance = SharedPrefHelper.getInstance(ActivitySplashScreen.this);

/*       ImageView imgLogo = (ImageView) findViewById(R.id.img_logo);

        String lastSelectedTeamId = SharedPrefHelper.getInstance(getApplicationContext()).getString(Constants.TAG_LAST_SELECTED_TEAM, "null");

        Glide.with(ActivitySplashScreen.this)
                .load(Uri.parse(Util.getTeamLogoURL(lastSelectedTeamId)))
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.icon)
                        .error(R.drawable.icon))
                .into(imgLogo);*/

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_params);

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {

                            String remoteConfig = mFirebaseRemoteConfig.getString("RemoteDialog");

                            RemoteDialogModel remoteDialogModel = new Gson().fromJson(remoteConfig, RemoteDialogModel.class);

                            if (remoteDialogModel.getType() == RemoteDialogTypeEnum.UPDATE.getValue()) {

                                String currentVersion = Util.getCurrentVersion(ActivitySplashScreen.this);
                                currentVersion = currentVersion.replace(".", "").trim();
                                int curVer = Integer.parseInt(currentVersion);
                                int newVer = Integer.parseInt(remoteDialogModel.getVersion().replace(".", "").trim());
                                if (newVer > curVer) {

                                    boolean dontShowUpdate = sharedPrefHelperInstance.getBoolean(Constants.REMOTE_DIALOG_PREFIX + remoteDialogModel.getId(), false);

                                    if (remoteDialogModel.isForceUpdate() || (!remoteDialogModel.isForceUpdate() && !dontShowUpdate)) {

                                        LayoutInflater inflater = getLayoutInflater();
                                        View view = inflater.inflate(R.layout.dialog_update, null);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySplashScreen.this);
                                        builder.setView(view);
                                        builder.setCancelable(false);
                                        TextView txtChanges = (TextView) view.findViewById(R.id.txtChanges);

                                        if (LanguageEnum.getLocaleEnumValue() == LanguageEnum.TURKISH.getValue()) {
                                            txtChanges.setText(remoteDialogModel.getTr());
                                        } else {
                                            txtChanges.setText(remoteDialogModel.getEn());
                                        }

                                        Button btnExit = (Button) view.findViewById(R.id.btnExit);

                                        if (!remoteDialogModel.isForceUpdate()) {
                                            btnExit.setText(getString(R.string.later));
                                        }

                                        btnExit.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!remoteDialogModel.isForceUpdate()) {
                                                    processToApp();
                                                } else {
                                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                                    intent.addCategory(Intent.CATEGORY_HOME);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });

                                        Button btnUpdate = (Button) view.findViewById(R.id.btnUpdate);
                                        btnUpdate.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                try {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                                } catch (android.content.ActivityNotFoundException activityNotFoundException) {
                                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                                }
                                            }
                                        });

                                        CheckBox checkBoxUpdate = (CheckBox) view.findViewById(R.id.checkbox_update);
                                        if (remoteDialogModel.isForceUpdate()) {
                                            checkBoxUpdate.setVisibility(View.INVISIBLE);
                                        } else {
                                            checkBoxUpdate.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    sharedPrefHelperInstance.saveBoolean(Constants.REMOTE_DIALOG_PREFIX + remoteDialogModel.getId(), checkBoxUpdate.isChecked());
                                                }
                                            });
                                        }

                                        builder.show();

                                    } else {
                                        processToApp();
                                    }
                                } else {
                                    processToApp();
                                }

                            } else if (remoteDialogModel.getType() == RemoteDialogTypeEnum.INFO.getValue()) {

                                boolean dontShowInfo = sharedPrefHelperInstance.getBoolean(Constants.REMOTE_DIALOG_PREFIX + remoteDialogModel.getId(), false);

                                if (!dontShowInfo) {
                                    LayoutInflater inflater = getLayoutInflater();
                                    View view = inflater.inflate(R.layout.dialog_info, null);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySplashScreen.this);
                                    builder.setView(view);
                                    builder.setCancelable(false);
                                    TextView txtWarning = (TextView) view.findViewById(R.id.txtWarning);

                                    if (LanguageEnum.getLocaleEnumValue() == LanguageEnum.TURKISH.getValue()) {
                                        txtWarning.setText(remoteDialogModel.getTr());
                                    } else {
                                        txtWarning.setText(remoteDialogModel.getEn());
                                    }

                                    Button btnOk = (Button) view.findViewById(R.id.btnOk);
                                    btnOk.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            processToApp();
                                        }
                                    });

                                    CheckBox checkBoxUpdate = (CheckBox) view.findViewById(R.id.checkbox_update);
                                    checkBoxUpdate.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            sharedPrefHelperInstance.saveBoolean(Constants.REMOTE_DIALOG_PREFIX + remoteDialogModel.getId(), checkBoxUpdate.isChecked());
                                        }
                                    });

                                    builder.show();
                                } else {
                                    processToApp();
                                }
                            } else if (remoteDialogModel.getType() == RemoteDialogTypeEnum.UNDER_CONSTRUCTION.getValue()) {

                                LayoutInflater inflater = getLayoutInflater();
                                View view = inflater.inflate(R.layout.dialog_under_construction, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySplashScreen.this);
                                builder.setView(view);
                                builder.setCancelable(false);
                                TextView txtWarning = (TextView) view.findViewById(R.id.txtWarning);

                                if (LanguageEnum.getLocaleEnumValue() == LanguageEnum.TURKISH.getValue()) {
                                    txtWarning.setText(remoteDialogModel.getTr());
                                } else {
                                    txtWarning.setText(remoteDialogModel.getEn());
                                }

                                Button btnOk = (Button) view.findViewById(R.id.btnOk);
                                btnOk.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        intent.addCategory(Intent.CATEGORY_HOME);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                                builder.show();
                            } else {
                                processToApp();
                            }
                        } else {
                            processToApp();
                        }
                    }
                });
    }

    private void processToApp() {

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(ActivitySplashScreen.this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        if (mAuth.getCurrentUser() != null) {
                            PersonService personService = new PersonService();
                            Person _person = new Person();
                            _person.setId(mAuth.getCurrentUser().getUid());

                            try {
                                personService.getPerson(ActivitySplashScreen.this,
                                        _person,
                                        new VolleyCallback() {
                                            @Override
                                            public void onSuccess(JSONObject result) {
                                                Person person = new Gson().fromJson(result.toString(), Person.class);
                                                Constants.person = person;

                                                if (pendingDynamicLinkData != null) {
                                                    Uri deepLink = pendingDynamicLinkData.getLink();
                                                    String teamId = deepLink.getQueryParameter("teamId");

                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("ID", teamId);
                                                    bundle.putString("DIRECT", "JOIN");

                                                    Intent intent = new Intent(ActivitySplashScreen.this, ActivityTeam.class);
                                                    intent.putExtras(bundle);
                                                    startActivity(intent);

                                                } else {

                                                    Bundle bundle = getIntent().getExtras();
                                                    if (bundle != null){
                                                        getPersonTeam(null, bundle );
                                                    } else {

                                                        String lastSelectedTeamId = SharedPrefHelper.getInstance(getApplicationContext()).getString(Constants.TAG_LAST_SELECTED_TEAM, null);

                                                        if (lastSelectedTeamId == null) {
                                                            Intent intent = new Intent(getApplicationContext(), ActivityTeam.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            getPersonTeam(lastSelectedTeamId, null);
                                                        }
                                                    }
                                                }
                                                finish();
                                            }

                                            @Override
                                            public void onError(String result) {
                                                Util.toastError(ActivitySplashScreen.this);
                                            }

                                            @Override
                                            public void onSuccessList(JSONArray result) {
                                            }
                                        });
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Util.toastError(ActivitySplashScreen.this);
                            }
                        } else {
                            Intent intent = new Intent(ActivitySplashScreen.this, ActivityLogin.class);
                            if (pendingDynamicLinkData != null) {
                                Uri deepLink = pendingDynamicLinkData.getLink();
                                String teamId = deepLink.getQueryParameter("teamId");

                                Bundle bundle = new Bundle();
                                bundle.putString("ID", teamId);
                                bundle.putString("DIRECT", "JOIN");
                                intent.putExtras(bundle);
                            }
                            startActivity(intent);
                            finish();
                        }
                    }
                })
                .addOnFailureListener(ActivitySplashScreen.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("", "getDynamicLink:onFailure", e);
                        Util.toastError(ActivitySplashScreen.this, "getDynamicLink:onFailure");
                    }
                });
    }

    private void getPersonTeam(String teamId, Bundle bundle) {

        PersonTeam _personTeam = new PersonTeam();
        _personTeam.setPersonId(FirebaseAuth.getInstance().getCurrentUser().getUid());

        if(bundle != null) {
            NotificationModel notificationModel = new Gson().fromJson(bundle.getString("notificationModel"), NotificationModel.class);
            _personTeam.setTeamId(notificationModel.getTraining().getTeamId());
        } else {
            _personTeam.setTeamId(teamId);
        }

        PersonTeamService personTeamService = new PersonTeamService();

        try {
            personTeamService.getPersonTeam(ActivitySplashScreen.this,
                    _personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            PersonTeamView personTeamView = new Gson().fromJson(result.toString(), PersonTeamView.class);
                            Constants.personTeamView = personTeamView;

                            if (personTeamView != null) {
                                Intent intent = new Intent(getApplicationContext(), ActivityHome.class);
                                if(bundle != null)
                                    intent.putExtras(bundle);
                                SharedPrefHelper.getInstance(getApplicationContext()).saveString(Constants.TAG_LAST_SELECTED_TEAM,  Constants.personTeamView.getTeamId());
                                startActivity(intent);
                                finish();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), ActivityTeam.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onError(String result) {
                            Intent intent = new Intent(getApplicationContext(), ActivityTeam.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Intent intent = new Intent(getApplicationContext(), ActivityTeam.class);
            startActivity(intent);
            finish();
        }
    }
}
