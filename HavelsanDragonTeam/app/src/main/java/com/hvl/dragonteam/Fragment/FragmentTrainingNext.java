package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.hvl.dragonteam.Adapter.TrainingAttendanceAdapter;
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

public class FragmentTrainingNext extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;

    private TrainingAttendanceAdapter trainingAdapter;
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
        if(Constants.personTeamView.getRole() == RoleEnum.ADMIN.getValue()) {
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

        getTrainings();

        return view;
    }

    public void getTrainings() {
        view.findViewById(R.id.resultPanel).setVisibility(View.GONE);
        view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        PersonTrainingAttendanceService personTrainingAttendanceService = new PersonTrainingAttendanceService();
        PersonTrainingAttendance personTrainingAttendance = new PersonTrainingAttendance();
        personTrainingAttendance.setPersonId(Constants.person.getId());
        personTrainingAttendance.setTime(new SimpleDateFormat(Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss).format(new Date()));
        personTrainingAttendance.setTeamId(Constants.personTeamView.getTeamId());
        try {
            personTrainingAttendanceService.getPersonTrainingAttendanceListByPersonNext(context, personTrainingAttendance,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonTrainingAttendance> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonTrainingAttendance>>() {
                            }.getType());

                            personTrainingAttendanceList.clear();
                            personTrainingAttendanceList.addAll(list);

                            trainingAdapter = new TrainingAttendanceAdapter(context, personTrainingAttendanceList, true);
                            trainingAdapter.setClickListener(new TrainingAttendanceAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {

                                    Training training = new Training(personTrainingAttendanceList.get(position).getTrainingId(),
                                            personTrainingAttendanceList.get(position).getTime(),
                                            personTrainingAttendanceList.get(position).getLocation(),
                                            Constants.personTeamView.getTeamId());

                                    String json = new Gson().toJson(training, Training.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("OBJ", json);

                                    FragmentLineup fragmentLineup = new FragmentLineup();
                                    fragmentLineup.setArguments(bundle);
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.container, fragmentLineup, "fragmentLineup").addToBackStack("fragmentLineup")
                                            .commit();
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

    private Calendar date;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;
    private void showAddDialog() {

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_add_training, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        final Spinner spinnerLocation = (Spinner) view.findViewById(R.id.spinner_location);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(adapter);

        final TextView txtDate = (TextView) view.findViewById(R.id.txt_date);

        onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker,  int hourOfDay, int minute) {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                txtDate.setText(Util.formatDate(date.getTime(),Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm));
            }
        };

        final ImageView imgDate = (ImageView) view.findViewById(R.id.img_date);
        imgDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker();
            }
        });

        builder.setPositiveButton(R.string.add_training, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Training training = new Training();
                training.setLocation(spinnerLocation.getSelectedItemPosition());
                String timeStamp = new SimpleDateFormat(Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss).format(date.getTime());
                training.setTime(timeStamp);
                training.setTeamId(Constants.personTeamView.getTeamId());
                TrainingService trainingService = new TrainingService();
                try {
                    trainingService.saveTraining(context, training,
                            new VolleyCallback() {
                                @Override
                                public void onSuccessList(JSONArray result) {
                                }
                                @Override
                                public void onSuccess(JSONObject result) {
                                    getTrainings();
                                    mRefreshLayout.setRefreshing(false);
                                }
                                @Override
                                public void onError(String result) {
                                    Activity activity = getActivity();
                                    if (activity != null && isAdded())
                                        Util.toastError(context);
                                }
                            });
                } catch (JSONException e) {
                    e.printStackTrace();
                    Activity activity = getActivity();
                    if (activity != null && isAdded())
                        Util.toastError(context);
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


    public void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                new TimePickerDialog(context, onTimeSetListener, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), true).show();
            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }



    @Override
    public void onResume() {
        super.onResume();
    }

}

