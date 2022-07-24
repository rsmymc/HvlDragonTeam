package com.hvl.dragonteam.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.PersonTeamByPersonAdapter;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.DataService.TeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.Util;
import com.nambimobile.widgets.efab.ExpandableFabLayout;
import com.nambimobile.widgets.efab.FabOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityTeam extends AppCompatActivity {

    private PersonTeamByPersonAdapter personTeamByPersonAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView listView;
    private TextView txtQuestionConfirm;
    private ExpandableFabLayout fabLayout;
    private ArrayList<PersonTeamView> teamList = new ArrayList<>();
    private Team _team;
    private LinearLayout layoutCreate;
    private LinearLayout layoutJoin;
    private LinearLayout layoutActionButtons;
    private LinearLayout layoutCode;
    private LinearLayout layoutJoinConfirm;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.team_swipe_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTeams();
                mRefreshLayout.setRefreshing(false);
            }
        });

        listView = findViewById(R.id.listView_team);
        listView.setLayoutManager(new LinearLayoutManager(ActivityTeam.this, LinearLayoutManager.VERTICAL, false));
        getTeams();

        fabLayout = findViewById(R.id.fabLayout);
        layoutCreate = findViewById(R.id.layout_create);
        layoutJoin = findViewById(R.id.layout_join);
        layoutActionButtons = findViewById(R.id.layout_action_buttons);

        layoutCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateDialog();
            }
        });

        layoutJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showJoinDialog("");
            }
        });

        bundle = getIntent().getExtras();

        if (bundle != null && bundle.getString("DIRECT", "").equals("JOIN")) {
            String teamId = bundle.getString("ID", "");
            showJoinDialog(teamId);
        }
    }

    public void myClickMethod(View v) {
        switch (v.getId()) {
            case R.id.fabOptionCreateTeam: {
                showCreateDialog();
                break;
            }
            case R.id.fabOptionJoinTeam: {
                showJoinDialog("");
                break;
            }
        }
    }

    private void showCreateDialog() {

        LayoutInflater inflater = getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_create_team, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTeam.this);
        builder.setView(view);
        builder.setCancelable(false);

        final EditText editTextName = (EditText) view.findViewById(R.id.txt_team_name);

        builder.setPositiveButton(R.string.create_team, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String teamName = editTextName.getText().toString();

                Team team = new Team();
                team.setName(teamName);
                team.setCreatorPersonId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                TeamService teamService = new TeamService();
                try {
                    teamService.saveTeam(ActivityTeam.this, team,
                            new VolleyCallback() {
                                @Override
                                public void onSuccessList(JSONArray result) {
                                }

                                @Override
                                public void onSuccess(JSONObject result) {
                                    Team _team = new Gson().fromJson(result.toString(), Team.class);
                                    PersonTeam personTeam = new PersonTeam(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                            _team.getId(),
                                            RoleEnum.ADMIN.getValue());

                                    savePersonTeam(personTeam, true);
                                    mRefreshLayout.setRefreshing(false);
                                }

                                @Override
                                public void onError(String result) {
                                    Util.toastError(ActivityTeam.this);
                                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Util.toastError(ActivityTeam.this);
                    view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    AlertDialog builder;

    private void showJoinDialog(String teamId) {

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_join_team, null);
        builder = new AlertDialog.Builder(ActivityTeam.this).create();
        builder.setView(view);
        EditText editTextName = (EditText) view.findViewById(R.id.txt_team_name);
        Button btnNext = (Button) view.findViewById(R.id.btn_next);
        Button btnConfirmJoin = (Button) view.findViewById(R.id.btn_join_team);
        txtQuestionConfirm = (TextView) view.findViewById(R.id.txt_question_confirm);
        layoutCode = (LinearLayout) view.findViewById(R.id.layout_code);
        layoutJoinConfirm = (LinearLayout) view.findViewById(R.id.layout_join_confirm);

        editTextName.setText(teamId);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTeam(editTextName.getText().toString());
            }
        });
        btnConfirmJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PersonTeam personTeam = new PersonTeam(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                        _team.getId(),
                        RoleEnum.DEFAULT.getValue());
                savePersonTeam(personTeam, false);
            }
        });

        builder.show();
    }

    private void getTeams() {
        PersonTeamService personTeamService = new PersonTeamService();
        Person person = new Person();
        person.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        try {
            personTeamService.getTeamListByPerson(ActivityTeam.this,
                    person,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonTeamView> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonTeamView>>() {
                            }.getType());

                            teamList.clear();
                            teamList.addAll(list);

                            personTeamByPersonAdapter = new PersonTeamByPersonAdapter(ActivityTeam.this, teamList);
                            personTeamByPersonAdapter.setClickListener(new PersonTeamByPersonAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    Constants.personTeamView = teamList.get(position);

                                    SharedPrefHelper.getInstance(getApplicationContext()).saveString(Constants.TAG_LAST_SELECTED_TEAM,  Constants.personTeamView.getTeamId());

                                    Intent intent = new Intent(getApplicationContext(), ActivityHome.class).putExtra("from", "activityTeam");;
                                    startActivity(intent);
                                   // finish();

                                }
                            });
                            listView.setAdapter(personTeamByPersonAdapter);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            if (teamList.size() == 0) {
                                layoutActionButtons.setVisibility(View.VISIBLE);
                                fabLayout.setVisibility(View.GONE);
                            } else  {
                                layoutActionButtons.setVisibility(View.GONE);
                                fabLayout.setVisibility(View.VISIBLE);
                            }
                            mRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(ActivityTeam.this);
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityTeam.this);
        }
    }

    private void savePersonTeam(PersonTeam personTeam, boolean isCreate) {

        PersonTeamService personTeamService = new PersonTeamService();

        try {
            personTeamService.savePersonTeam(ActivityTeam.this,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            PersonTeam _personTeam = new Gson().fromJson(result.toString(), PersonTeam.class);
                            getTeams();

                            if(isCreate){
                                Util.toastInfo(ActivityTeam.this, getString(R.string.info_team_created));
                            } else {
                                Util.toastInfo(ActivityTeam.this, getString(R.string.info_team_joined));
                                builder.dismiss();
                            }
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(ActivityTeam.this);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityTeam.this);
        }
    }

    private void getTeam(String teamId) {

        TeamService teamService = new TeamService();
        Team team = new Team();
        team.setId(teamId);
        try {
            teamService.getTeam(ActivityTeam.this,
                    team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            _team = new Gson().fromJson(result.toString(), Team.class);
                            txtQuestionConfirm.setText(getString(R.string.question_join_team).replace("XXX", _team.getName()));
                            layoutCode.setVisibility(View.GONE);
                            layoutJoinConfirm.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastWarning(ActivityTeam.this, getString(R.string.warning_team_not_found));
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(ActivityTeam.this);
        }
    }

}

