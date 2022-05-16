package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.TrainingAdapter;
import com.hvl.dragonteam.DataService.PersonTrainingAttendanceService;
import com.hvl.dragonteam.DataService.TrainingService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.PersonTrainingAttendance;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FragmentTrainingPast extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;

    private TrainingAdapter trainingAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView listView;
    private LinearLayout layoutAdd;
    private ArrayList<PersonTrainingAttendance> personTrainingAttendanceList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_training_list, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fragmentManager = context.getSupportFragmentManager();
        setHasOptionsMenu(true);

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.training_swipe_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTrainings();
                mRefreshLayout.setRefreshing(false);
            }
        });

        listView = view.findViewById(R.id.listView_lineup);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        layoutAdd = view.findViewById(R.id.layout_add);
        layoutAdd.setVisibility(View.GONE);

        getTrainings();

        return view;
    }

    public void getTrainings() {
        view.findViewById(R.id.resultPanel).setVisibility(View.GONE);
        PersonTrainingAttendanceService personTrainingAttendanceService = new PersonTrainingAttendanceService();
        PersonTrainingAttendance personTrainingAttendance = new PersonTrainingAttendance();
        personTrainingAttendance.setPersonId(Constants.person.getId());
        personTrainingAttendance.setTime(new SimpleDateFormat(Util.DATE_FORMAT_yyyy_MM_dd_hh_mm_ss).format(new Date()));
        try {
            personTrainingAttendanceService.getPersonTrainingAttendanceListByPersonPast(context, personTrainingAttendance,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonTrainingAttendance> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonTrainingAttendance>>() {
                            }.getType());

                            personTrainingAttendanceList.clear();
                            personTrainingAttendanceList.addAll(list);

                            trainingAdapter = new TrainingAdapter(context, personTrainingAttendanceList, false);
                            trainingAdapter.setClickListener(new TrainingAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                }
                            });
                            listView.setAdapter(trainingAdapter);
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

}

