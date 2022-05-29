package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.PersonTeamAdapter;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class FragmentTeam extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;

    private PersonTeamAdapter personTeamAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private ListView listView;
    private ArrayList<PersonTeamView> personTeamList = new ArrayList<>();
    private ArrayList<String> listLetters = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_team, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fragmentManager = context.getSupportFragmentManager();
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.team));

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.team_swipe_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTeamPersons();
                mRefreshLayout.setRefreshing(false);
            }
        });

        listView = view.findViewById(R.id.listView_team);
        listView.setFastScrollEnabled(true);

        EditText myFilter = (EditText) view.findViewById(R.id.txt_filter);
        myFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    personTeamAdapter.getFilter().filter(s.toString());
                } catch (Exception ex) {
                    Log.i("Error", ex.toString());
                }
            }
        });

        getTeamPersons();

        return view;
    }

    public void getTeamPersons() {
        view.findViewById(R.id.resultPanel).setVisibility(View.GONE);
        PersonTeamService personTeamService = new PersonTeamService();
        Team team = new Team();
        team.setId(Constants.TEAM_ID);
        try {
            personTeamService.getPersonTeamList(context, team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonTeamView> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonTeamView>>() {
                            }.getType());

                            personTeamList.clear();
                            personTeamList.addAll(list);

                            for (PersonTeamView personTeamView : personTeamList){
                                if (!listLetters.contains(String.valueOf(personTeamView.getPersonName().charAt(0)).toUpperCase())) {
                                    listLetters.add(String.valueOf(personTeamView.getPersonName().charAt(0)).toUpperCase());
                                }
                            }
                            Collections.sort(listLetters, new Comparator<String>() {
                                @Override
                                public int compare(String s1, String s2) {
                                    Collator collator = Collator.getInstance(new Locale("tr", "TR"));
                                    return collator.compare(s1, s2);
                                }
                            });

                            String mSections = "";
                            for (String letter : listLetters) {
                                mSections += letter;
                            }

                            personTeamAdapter = new PersonTeamAdapter(context, personTeamList, mSections);
                            listView.setAdapter(personTeamAdapter);

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

