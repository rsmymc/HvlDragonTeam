package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.LineupAdapter;
import com.hvl.dragonteam.Adapter.LineupTeamAdapter;
import com.hvl.dragonteam.DataService.LineupService;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.DataService.PersonTrainingAttendanceService;
import com.hvl.dragonteam.Interface.OnLineupChangeListener;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.LineupEnum;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.Enum.SaveEnum;
import com.hvl.dragonteam.Model.FilterModel;
import com.hvl.dragonteam.Model.Lineup;
import com.hvl.dragonteam.Model.LineupItem;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.Model.PersonTrainingAttendance;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.URLs;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentLineup extends Fragment implements OnLineupChangeListener {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;
    private Training training;
    private LineupAdapter lineupAdapter;
    private LineupTeamAdapter lineupTeamAdapter;
    private Switch switchOnlyAttendees;
    private TextView txtWeightLeft;
    private TextView txtWeightRight;
    private Button btnDraft;
    private Button btnPublish;
    private Button btnReset;
    private RecyclerView listViewLineup;
    private RecyclerView listViewPerson;
    private CardView layoutTeam;
    private CardView layoutLineup;
    private CardView layoutFilter;
    private CheckBox checkBoxLeft;
    private CheckBox checkBoxRight;
    private CheckBox checkBoxBoth;
    private CheckBox checkBoxHideDontAttend;
    private LinearLayout layoutActionButtons;
    private ArrayList<LineupItem> lineupList = new ArrayList<>();
    private ArrayList<PersonTrainingAttendance> personTrainingAttendanceList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Toolbar toolbar;
    private FilterModel filterModel = new FilterModel();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_lineup, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fragmentManager = context.getSupportFragmentManager();
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            training = new Gson().fromJson(bundle.getString("OBJ"), Training.class);
        }

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.lineup));

        listViewLineup = view.findViewById(R.id.listView_lineup);
        listViewLineup.setLayoutManager(new GridLayoutManager(context, 2));

        listViewPerson = view.findViewById(R.id.listView_team);
        listViewPerson.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));

        checkBoxLeft = view.findViewById(R.id.checkbox_left);
        checkBoxRight = view.findViewById(R.id.checkbox_right);
        checkBoxBoth = view.findViewById(R.id.checkbox_both);
        checkBoxHideDontAttend = view.findViewById(R.id.checkbox_hide_dont_attend);

        checkBoxLeft.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                filterList();
            }
        });
        checkBoxRight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                filterList();
            }
        });
        checkBoxBoth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                filterList();
            }
        });
        checkBoxHideDontAttend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                filterList();
            }
        });


        txtWeightLeft = view.findViewById(R.id.txt_weight_left);
        txtWeightRight = view.findViewById(R.id.txt_weight_right);

        btnPublish = view.findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLineup(SaveEnum.PUBLISH.getValue());
            }
        });
        btnDraft = view.findViewById(R.id.btn_draft);
        btnDraft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLineup(SaveEnum.DRAFT.getValue());
            }
        });

        btnReset = view.findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPersonAttendance();
            }
        });

        layoutLineup = view.findViewById(R.id.layout_lineup);
        layoutTeam = view.findViewById(R.id.layout_team);
        layoutFilter = view.findViewById(R.id.layout_filter);
        layoutActionButtons = view.findViewById(R.id.layout_action_buttons);

        if (Constants.personTeam.getRole() != RoleEnum.ADMIN.getValue()){
            layoutTeam.setVisibility(View.GONE);
            layoutActionButtons.setVisibility(View.GONE);
        }

        getPersonAttendance();

        return view;
    }

    private PersonTrainingAttendance find(String personId) {

        PersonTrainingAttendance emptyPersonTrainingAttendance = new PersonTrainingAttendance();
        emptyPersonTrainingAttendance.setPersonId(getString(R.string.empty));
        emptyPersonTrainingAttendance.setName(getString(R.string.empty));

        if (personId == null)
            return emptyPersonTrainingAttendance;

        PersonTrainingAttendance personTrainingAttendance = personTrainingAttendanceList.stream()
                .filter(item -> personId.equals(item.getPersonId())).findFirst().orElse(emptyPersonTrainingAttendance);

        personTrainingAttendanceList.remove(personTrainingAttendance);

        return personTrainingAttendance;
    }

    private void filterList(){

        filterModel.setLeft(checkBoxLeft.isChecked());
        filterModel.setRight(checkBoxRight.isChecked());
        filterModel.setBoth(checkBoxBoth.isChecked());
        filterModel.setHideDontAttend(checkBoxHideDontAttend.isChecked());

        lineupTeamAdapter.setFilterModel(filterModel);
        lineupTeamAdapter.notifyDataSetChanged();
    }

    public void getLineup() {
        LineupService lineupService = new LineupService();
        try {
            lineupService.getLineup(context, training,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {

                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                            Lineup lineup = new Gson().fromJson(result.toString(), Lineup.class);

                            lineupList.clear();

                            LineupItem lineupItemL1 = new LineupItem(LineupEnum.L1.getValue(), find(lineup.getL1()));
                            LineupItem lineupItemL2 = new LineupItem(LineupEnum.L2.getValue(), find(lineup.getL2()));
                            LineupItem lineupItemL3 = new LineupItem(LineupEnum.L3.getValue(), find(lineup.getL3()));
                            LineupItem lineupItemL4 = new LineupItem(LineupEnum.L4.getValue(), find(lineup.getL4()));
                            LineupItem lineupItemL5 = new LineupItem(LineupEnum.L5.getValue(), find(lineup.getL5()));
                            LineupItem lineupItemL6 = new LineupItem(LineupEnum.L6.getValue(), find(lineup.getL6()));
                            LineupItem lineupItemL7 = new LineupItem(LineupEnum.L7.getValue(), find(lineup.getL7()));
                            LineupItem lineupItemL8 = new LineupItem(LineupEnum.L8.getValue(), find(lineup.getL8()));

                            LineupItem lineupItemR1 = new LineupItem(LineupEnum.R1.getValue(), find(lineup.getR1()));
                            LineupItem lineupItemR2 = new LineupItem(LineupEnum.R2.getValue(), find(lineup.getR2()));
                            LineupItem lineupItemR3 = new LineupItem(LineupEnum.R3.getValue(), find(lineup.getR3()));
                            LineupItem lineupItemR4 = new LineupItem(LineupEnum.R4.getValue(), find(lineup.getR4()));
                            LineupItem lineupItemR5 = new LineupItem(LineupEnum.R5.getValue(), find(lineup.getR5()));
                            LineupItem lineupItemR6 = new LineupItem(LineupEnum.R6.getValue(), find(lineup.getR6()));
                            LineupItem lineupItemR7 = new LineupItem(LineupEnum.R7.getValue(), find(lineup.getR7()));
                            LineupItem lineupItemR8 = new LineupItem(LineupEnum.R8.getValue(), find(lineup.getR8()));

                            List<LineupItem> list = new ArrayList<>();
                            list.add(LineupEnum.L1.getValue(), lineupItemL1);
                            list.add(LineupEnum.R1.getValue(), lineupItemR1);
                            list.add(LineupEnum.L2.getValue(), lineupItemL2);
                            list.add(LineupEnum.R2.getValue(), lineupItemR2);
                            list.add(LineupEnum.L3.getValue(), lineupItemL3);
                            list.add(LineupEnum.R3.getValue(), lineupItemR3);
                            list.add(LineupEnum.L4.getValue(), lineupItemL4);
                            list.add(LineupEnum.R4.getValue(), lineupItemR4);
                            list.add(LineupEnum.L5.getValue(), lineupItemL5);
                            list.add(LineupEnum.R5.getValue(), lineupItemR5);
                            list.add(LineupEnum.L6.getValue(), lineupItemL6);
                            list.add(LineupEnum.R6.getValue(), lineupItemR6);
                            list.add(LineupEnum.L7.getValue(), lineupItemL7);
                            list.add(LineupEnum.R7.getValue(), lineupItemR7);
                            list.add(LineupEnum.L8.getValue(), lineupItemL8);
                            list.add(LineupEnum.R8.getValue(), lineupItemR8);

                            lineupList.addAll(list);

                            lineupAdapter = new LineupAdapter(context, lineupList, FragmentLineup.this);
                            listViewLineup.setAdapter(lineupAdapter);
                            if(Constants.personTeam.getRole() ==  RoleEnum.ADMIN.getValue()) {
                                listViewLineup.setOnDragListener(lineupAdapter.getDragInstance());
                            }

                            lineupTeamAdapter = new LineupTeamAdapter(context, personTrainingAttendanceList, filterModel ,FragmentLineup.this);
                            listViewPerson.setAdapter(lineupTeamAdapter);
                            listViewPerson.setOnDragListener(lineupTeamAdapter.getDragInstance());

                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            lineupChange();
                        }

                        @Override
                        public void onError(String result) {
                            Activity activity = getActivity();
                            if (activity != null && isAdded())
                                Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Activity activity = getActivity();
            if (activity != null && isAdded())
                Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private void getPersonAttendance() {
        PersonTrainingAttendanceService personTrainingAttendanceService = new PersonTrainingAttendanceService();
        try {
            personTrainingAttendanceService.getPersonTrainingAttendanceListByTraining(context, training,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonTrainingAttendance> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonTrainingAttendance>>() {}.getType());

                            personTrainingAttendanceList.clear();
                            personTrainingAttendanceList.addAll(list);
                            getLineup();
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private void saveLineup(int state) {

        List<LineupItem> list = lineupAdapter.getListLineup();

        Lineup _lineup = new Lineup();
        _lineup.setL1(list.get(LineupEnum.L1.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL2(list.get(LineupEnum.L2.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL3(list.get(LineupEnum.L3.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL4(list.get(LineupEnum.L4.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL5(list.get(LineupEnum.L5.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL6(list.get(LineupEnum.L6.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL7(list.get(LineupEnum.L7.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setL8(list.get(LineupEnum.L8.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR1(list.get(LineupEnum.R1.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR2(list.get(LineupEnum.R2.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR3(list.get(LineupEnum.R3.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR4(list.get(LineupEnum.R4.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR5(list.get(LineupEnum.R5.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR6(list.get(LineupEnum.R6.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR7(list.get(LineupEnum.R7.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setR8(list.get(LineupEnum.R8.getValue()).getPersonTrainingAttendance().getPersonId());
        _lineup.setTrainingId(training.getId());
        _lineup.setState(state);

        LineupService lineupService = new LineupService();
        progressDialog = ProgressDialog.show(context, getResources().getString(R.string.processing), getResources().getString(R.string.wait), false, false);
        try {
            lineupService.saveLineup(context, _lineup,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Lineup lineup = new Gson().fromJson(result.toString(), Lineup.class);
                            progressDialog.dismiss();
                            Util.toastInfo(context, R.string.info_lineup_updated);
                            getPersonsNotification(state);
                        }

                        @Override
                        public void onError(String result) {
                            progressDialog.dismiss();
                            Util.toastError(context);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            progressDialog.dismiss();
            Util.toastError(context);
        }
    }

    private void sendMessageNotify(String tokens, String message) {
        NotificationModel notificationModel = new NotificationModel("",
                NotificationTypeEnum.LINEUP_NOTIFICATION.getValue(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                Constants.person.getName());
        String json = new Gson().toJson(notificationModel, NotificationModel.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("token", tokens);
        params.put("body", message);
        params.put("title",  toolbar.getTitle().toString());
        params.put("notificationModel", json);
        Util.postRequest(context, URLs.urlSendNotification, params, null);
    }

    private void getPersonsNotification(int state) {

        NotificationService notificationService = new NotificationService();
        try {
            Team team = new Team();
            team.setId(Constants.personTeam.getTeamId());
            notificationService.getPersonsNotificationList(context,team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonNotification> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonNotification>>() {
                            }.getType());

                            JSONArray tokens = new JSONArray();

                            for (PersonNotification personNotification : list) {
                                if (personNotification.isLoggedIn()
                                        && !personNotification.getPersonId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        && personNotification.getNotification1()
                                        && (state == SaveEnum.PUBLISH.getValue() || personNotification.getRole() == RoleEnum.ADMIN.getValue())) {
                                    tokens.put(personNotification.getToken());
                                }
                            }

                            String message ="";
                            if(state == SaveEnum.PUBLISH.getValue()) {
                                message = getString(R.string.publish_notification);
                            } else if(state == SaveEnum.PUBLISH.getValue()){
                                message = getString(R.string.draft_notification).replace("XXX", Constants.person.getName());
                            }

                            sendMessageNotify(tokens.toString(), message);
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_filter): {
               /* if (layoutFilter.getVisibility() != View.VISIBLE)
                    layoutFilter.setVisibility(View.VISIBLE);
                else
                    layoutFilter.setVisibility(View.GONE);*/

                if (layoutFilter.getVisibility() != View.VISIBLE) {
                    layoutFilter.setVisibility(View.VISIBLE);
                    layoutLineup.setVisibility(View.GONE);
                } else {
                    layoutFilter.setVisibility(View.GONE);
                    layoutLineup.setVisibility(View.VISIBLE);
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void lineupChange() {

        List<LineupItem> list = lineupAdapter.getListLineup();

        int totalLeftWeight = 0;
        int totalRightWeight = 0;

        for (LineupItem lineupItem : list) {

            if (lineupItem.getId() % 2 == 0) {
                totalLeftWeight += lineupItem.getPersonTrainingAttendance().getWeight();
            } else {
                totalRightWeight +=  lineupItem.getPersonTrainingAttendance().getWeight();
            }
        }

        txtWeightLeft.setText(String.valueOf(totalLeftWeight) + " Kg");
        txtWeightRight.setText(String.valueOf(totalRightWeight) + " Kg");

    }

}

