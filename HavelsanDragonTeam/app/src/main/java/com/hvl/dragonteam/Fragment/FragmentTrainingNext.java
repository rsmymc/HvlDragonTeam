package com.hvl.dragonteam.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.LocationAdapter;
import com.hvl.dragonteam.Adapter.TrainingAttendanceAdapter;
import com.hvl.dragonteam.DataService.LocationService;
import com.hvl.dragonteam.DataService.PersonTrainingAttendanceService;
import com.hvl.dragonteam.DataService.TrainingService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.LocationModel;
import com.hvl.dragonteam.Model.PersonTrainingAttendance;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.ImageProcess;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static android.app.Activity.RESULT_OK;

public class FragmentTrainingNext extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;
    private MapView mMapView;
    private GoogleMap googleMap;
    private LatLng location;
    private TrainingAttendanceAdapter trainingAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView listView;
    private LinearLayout layoutAdd;
    private ArrayList<PersonTrainingAttendance> personTrainingAttendanceList = new ArrayList<>();
    private ArrayList<LocationModel> locationModelList = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    public static final int PLACE_PICKER_REQUEST = 9000;
    private final static int PERMISSIONS_REQUEST = 103;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_training_list, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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
        if (Constants.personTeamView.getRole() == RoleEnum.ADMIN.getValue()) {
            layoutAdd.setVisibility(View.VISIBLE);
        } else {
            layoutAdd.setVisibility(View.GONE);
        }
        layoutAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog(savedInstanceState);
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
                                    //if (personTrainingAttendanceList.get(position).getPersonId() != null)
                                    {
                                        Training training = new Training(personTrainingAttendanceList.get(position).getTrainingId(),
                                                personTrainingAttendanceList.get(position).getTime(),
                                                personTrainingAttendanceList.get(position).getLocationId(),
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

    public void getLocations(LocationModel _locationModel) {
        LocationService locationService = new LocationService();
        Team team = new Team();
        team.setId(Constants.personTeamView.getTeamId());
        try {
            locationService.getLocationList(context, team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<LocationModel> list = new Gson().fromJson(result.toString(), new TypeToken<List<LocationModel>>() {
                            }.getType());

                            LocationModel emptyLocation = new LocationModel(-1,"","-", 0, 0);
                            list.add(0, emptyLocation);

                            locationModelList.clear();
                            locationModelList.addAll(list);

                            ArrayAdapter<LocationModel> adapter =new ArrayAdapter<LocationModel>(context, android.R.layout.simple_spinner_item, list);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerLocation.setAdapter(adapter);

                            if(_locationModel != null){
                                LocationModel model = locationModelList.stream().filter(a -> a.getId() == _locationModel.getId()).collect(Collectors.toList()).get(0);
                                int index = locationModelList.indexOf(model);
                                spinnerLocation.setSelection(index);
                            } else  if (locationModelList.size()>1){
                                spinnerLocation.setSelection(1);
                            }
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
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

    private Spinner spinnerLocation;
    private Calendar date;
    private TimePickerDialog.OnTimeSetListener onTimeSetListener;

    private void showAddDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = requireActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_add_training, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        spinnerLocation = (Spinner) view.findViewById(R.id.spinner_location);
        getLocations(null);

        final TextView txtDate = (TextView) view.findViewById(R.id.txt_date);

        onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                txtDate.setText(Util.formatDate(date.getTime(), Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm));
            }
        };

        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker();
            }
        });

        final ImageView imgInfo = (ImageView) view.findViewById(R.id.img_info);

        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Util.toastInfo(context, R.string.info_location_add_tip);
                showAddLocationDialog(savedInstanceState);
            }
        });

        builder.setPositiveButton(R.string.add_training, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Training training = new Training();
                training.setLocation(((LocationModel)spinnerLocation.getSelectedItem()).getId());
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

    private void showAddLocationDialog(Bundle savedInstanceState){
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_places, null);
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        builder.setView(dialogView);

        mMapView = dialogView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                }

                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(true);
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    addMarker(location.getLatitude(), location.getLongitude());
                                }
                            }
                        });
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        location = mMap.getCameraPosition().target;
                    }
                });
            }
        });

        EditText txtName = dialogView.findViewById(R.id.txt_name);

        Button btnSetLocation = dialogView.findViewById(R.id.btnSetLocation);
        btnSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!txtName.getText().toString().trim().equals("")) {
                    LocationModel locationModel = new LocationModel();
                    locationModel.setName(txtName.getText().toString());
                    locationModel.setTeamId(Constants.personTeamView.getTeamId());
                    locationModel.setLat(location.latitude);
                    locationModel.setLon(location.longitude);
                    LocationService locationService = new LocationService();
                    try {
                        locationService.saveLocation(context, locationModel,
                                new VolleyCallback() {
                                    @Override
                                    public void onSuccessList(JSONArray result) {
                                    }

                                    @Override
                                    public void onSuccess(JSONObject result) {
                                        Util.toastInfo(context, R.string.info_location_added);
                                        LocationModel _locationModel = new Gson().fromJson(result.toString(), LocationModel.class);
                                        getLocations(_locationModel);
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
                    builder.dismiss();

                } else {
                    Util.toastWarning(context, R.string.warning_cant_empty);
                }
            }
        });

        ImageView imgClose = dialogView.findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });

        LinearLayout layoutSearch = dialogView.findViewById(R.id.layoutSearch);
        layoutSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);

                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .build(context);
                ((Activity) context).startActivityForResult(intent, PLACE_PICKER_REQUEST);
            }
        });

        LinearLayout layoutCurrentLocation = dialogView.findViewById(R.id.layoutCurrent);
        layoutCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //Util.toastWarning(context, R.string.warning_allow_location);
                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST);
                    return;
                }
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    addMarker(location.getLatitude(), location.getLongitude());
                                }
                                        /*else {
                                            Util.showSettingsAlert(context);
                                        }*/
                            }
                        });
            }
        });

        builder.show();
    }

    private void addMarker(double lat, double lon) {
        location = new LatLng(lat, lon);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(17).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PLACE_PICKER_REQUEST:
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    addMarker(place.getLatLng().latitude, place.getLatLng().longitude);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}

