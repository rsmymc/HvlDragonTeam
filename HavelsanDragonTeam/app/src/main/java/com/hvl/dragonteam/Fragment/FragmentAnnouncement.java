package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.AnnouncementAdapter;
import com.hvl.dragonteam.DataService.AnnouncementService;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Announcement;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.URLs;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentAnnouncement extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;

    private AnnouncementAdapter announcementAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView listView;
    private LinearLayout layoutAdd;
    private ArrayList<Announcement> announcementList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_announcement_list, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fragmentManager = context.getSupportFragmentManager();
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.announcement));

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.announcement_swipe_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAnnouncements();
                mRefreshLayout.setRefreshing(false);
            }
        });

        listView = view.findViewById(R.id.listView_lineup);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        layoutAdd = view.findViewById(R.id.layout_add);
        if(Constants.personTeam.getRole() == RoleEnum.ADMIN.getValue()) {
            layoutAdd.setVisibility(View.VISIBLE);
        } else {
            layoutAdd.setVisibility(View.GONE);
        }
        layoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });

        getAnnouncements();

        return view;
    }

    public void getAnnouncements() {
        view.findViewById(R.id.resultPanel).setVisibility(View.GONE);
        AnnouncementService announcementService = new AnnouncementService();
        try {
            Team team = new Team();
            team.setId(Constants.TEAM_ID);
            announcementService.getAnnouncementList(context, team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<Announcement> list = new Gson().fromJson(result.toString(), new TypeToken<List<Announcement>>() {
                            }.getType());

                            announcementList.clear();
                            announcementList.addAll(list);

                            announcementAdapter = new AnnouncementAdapter(context, announcementList);
                            announcementAdapter.setClickListener(new AnnouncementAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                }
                            });
                            listView.setAdapter(announcementAdapter);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            if (list.size() == 0)
                                view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
                            mRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Activity activity = getActivity();
                            if (activity != null && isAdded())
                                Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Activity activity = getActivity();
            if (activity != null && isAdded())
                Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
        }
    }

    private void showAddDialog() {

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_add_announcement, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final EditText et1 = (EditText) view.findViewById(R.id.txt_location);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String _context = et1.getText().toString();

                Announcement announcement = new Announcement();
                announcement.setContext(_context);
                String timeStamp = new SimpleDateFormat(Util.DATE_FORMAT_yyyy_MM_dd_hh_mm_ss).format(new Date());
                announcement.setTime(timeStamp);
                announcement.setTeamId(Constants.TEAM_ID);
                AnnouncementService announcementService = new AnnouncementService();
                try {
                    announcementService.saveAnnouncement(context, announcement,
                            new VolleyCallback() {
                                @Override
                                public void onSuccessList(JSONArray result) {
                                }
                                @Override
                                public void onSuccess(JSONObject result) {
                                    getAnnouncements();
                                    getPersonsNotification(_context);
                                    mRefreshLayout.setRefreshing(false);
                                }
                                @Override
                                public void onError(String result) {
                                    Activity activity = getActivity();
                                    if (activity != null && isAdded())
                                        Util.toastError(context);
                                    view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                    view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Activity activity = getActivity();
                    if (activity != null && isAdded())
                        Util.toastError(context);
                    view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
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

    private void sendMessageNotify(String tokens, String message) {
        NotificationModel notificationModel = new NotificationModel("",
                NotificationTypeEnum.ANNOUNCEMENT_NOTIFICATION.getValue(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                Constants.person.getName());
        String json = new Gson().toJson(notificationModel, NotificationModel.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("token", tokens);
        params.put("body",  message);
        params.put("title", Constants.person.getName());
        params.put("notificationModel", json);
        Util.postRequest(context, URLs.urlSendNotification, params, null);
    }

    private void getPersonsNotification(String message) {

        NotificationService notificationService = new NotificationService();
        try {
            Team team = new Team();
            team.setId(Constants.personTeam.getTeamId());
            notificationService.getPersonsNotificationList(context, team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonNotification> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonNotification>>() {
                            }.getType());

                            JSONArray tokens = new JSONArray();

                            for (PersonNotification personNotification : list) {
                                if (personNotification.isLoggedIn() && !personNotification.getPersonId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && personNotification.getNotification1()) {
                                    tokens.put(personNotification.getToken());
                                }
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
        super.onCreateOptionsMenu(menu, inflater);
    }
}

