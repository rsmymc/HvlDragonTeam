package com.hvl.dragonteam.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.LocationAdapter;
import com.hvl.dragonteam.DataService.LocationService;
import com.hvl.dragonteam.DataService.TeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.ImageUploadResultTypeEnum;
import com.hvl.dragonteam.Model.ImageUploadResult;
import com.hvl.dragonteam.Model.LocationModel;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.CustomTypingEditText;
import com.hvl.dragonteam.Utilities.ImageProcess;
import com.hvl.dragonteam.Utilities.URLs;
import com.hvl.dragonteam.Utilities.Util;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FragmentTeamSettings extends Fragment implements LocationAdapter.OnDeleteClickedListener {
    View view;
    private ImageView imgLogo;
    private ImageView imgCamera;
    private ImageView imgAdd;
    private CustomTypingEditText editTextName;
    private TextView txtNoLocation;
    private Button btnSave;
    private FirebaseUser firebaseUser;
    private LinearLayout layoutShuttle;
    private MapView mMapView;
    private GoogleMap googleMap;
    private LatLng location;
    private Context context;
    private ProgressDialog progressDialog;
    private LocationAdapter locationAdapter;
    private RecyclerView listView;
    private ArrayList<LocationModel> locationModelList = new ArrayList<>();
    private final static int PICK_PHOTO_REQUEST = 100;
    private final static int TAKE_PHOTO_REQUEST = 101;
    private final static int WRITE_PERMISSION_REQUEST = 102;
    private FusedLocationProviderClient fusedLocationClient;
    public static final int PLACE_PICKER_REQUEST = 9000;
    private final static int PERMISSIONS_REQUEST = 103;
    private PopupWindow popupCropPhoto;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_team_settings, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        context = getContext();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));

        listView = view.findViewById(R.id.listView_location);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        imgLogo = (ImageView) view.findViewById(R.id.img_logo);
        imgCamera = (ImageView) view.findViewById(R.id.img_camera);
        imgAdd = (ImageView) view.findViewById(R.id.img_add);
        editTextName = (CustomTypingEditText) view.findViewById(R.id.txt_name);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        layoutShuttle = (LinearLayout) view.findViewById(R.id.layout_shuttle);
        txtNoLocation = view.findViewById(R.id.txt_no_location);

        Glide.with(context)
                .load(Util.getTeamLogoURL(Constants.personTeamView.getTeamId()))
                .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.icon_rectangle)
                        .error(R.drawable.icon_rectangle))
                .into(imgLogo);

        editTextName.setText(Constants.personTeamView.getTeamName());

        CustomTypingEditText.OnTypingModified onTypingModified = new CustomTypingEditText.OnTypingModified() {
            @Override
            public void onIsTypingModified(EditText view, boolean isTyping) {
                manageSaveButton();
            }
        };

        editTextName.setOnTypingModified(onTypingModified);

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkWritePermission()) {
                    requestWritePermission();
                } else {
                    showPhotoDialog();
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTeam();
            }
        });

        imgAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddLocationDialog(savedInstanceState);
            }
        });

        layoutShuttle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URLs.urlShuttle));
                startActivity(browserIntent);
            }
        });

        manageSaveButton();

        getLocations();

        return view;
    }

    private void getLocations() {

        /* view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);*/
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

                            locationModelList.clear();
                            locationModelList.addAll(list);
                            locationAdapter = new LocationAdapter(context, locationModelList);
                            locationAdapter.setOnDeleteClickedListener(FragmentTeamSettings.this);
                            locationAdapter.setClickListener(new LocationAdapter.ItemClickListener() {
                                @Override
                                public void onItemClick(View view, int position) {
                                }
                            });
                            listView.setAdapter(locationAdapter);
                            if (locationModelList.size() > 0) {
                                txtNoLocation.setVisibility(View.GONE);
                            } else {
                                txtNoLocation.setVisibility(View.VISIBLE);
                            }

                            /* view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);*/
                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Activity activity = getActivity();
                            if (activity != null && isAdded())
                                Util.toastError(context);
                            /* view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);*/
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Activity activity = getActivity();
            if (activity != null && isAdded())
                Util.toastError(context);
            /* view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);*/
        }
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
                                        getLocations();
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

    private void showPhotoDialog() {
        Constants.imageFilePath = ImageProcess.getFilename();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.profilePhoto)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setItems(R.array.profilePhotoArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        File imageFile;
                        Uri imageFileUri;
                        Intent intent;

                        switch (which) {
                            case 0: {
                                imageFile = new File(Constants.imageFilePath);
                                imageFileUri = Uri.fromFile(imageFile); // convert path to Uri
                                intent = new Intent(
                                        Intent.ACTION_PICK,
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);   // set the image file displayName
                                ((FragmentActivity) context).startActivityForResult(
                                        Intent.createChooser(intent, "Select File"),
                                        PICK_PHOTO_REQUEST);

                                break;
                            }
                            case 1: {
                                imageFile = new File(Constants.imageFilePath);
                                imageFileUri = FileProvider.getUriForFile(context, "com.hvl.dragonteam.GenericFileProvider", imageFile);
                                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);   // set the image file displayName
                                ((FragmentActivity) context).startActivityForResult(intent, TAKE_PHOTO_REQUEST);
                                break;
                            }
                            case 2: {
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                deleteImage();
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage(getResources().getString(R.string.question_delete_photo))
                                        .setPositiveButton(getResources().getString(R.string.remove), dialogClickListener)
                                        .setNegativeButton(getResources().getString(R.string.cancel), dialogClickListener).show();

                                break;
                            }
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteImage() {
        Glide.with(context)
                .load(Uri.parse("deleted"))
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.icon_rectangle)
                        .error(R.drawable.icon_rectangle))
                .into(imgLogo);
    }

    private void saveTeam() {
        TeamService teamService = new TeamService();
        progressDialog = ProgressDialog.show(context, getResources().getString(R.string.processing), getResources().getString(R.string.wait), false, false);
        Team team = new Team(Constants.personTeamView.getTeamId(),
                editTextName.getText().toString(),
                Constants.personTeamView.getCreatorPersonId());
        try {
            teamService.saveTeam(context,
                    team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Team team = new Gson().fromJson(result.toString(), Team.class);
                            Constants.personTeamView.setTeamName(team.getName());
                            manageSaveButton();
                            progressDialog.dismiss();
                            Util.toastInfo(context, R.string.info_team_updated);
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
        }
    }

    private void manageSaveButton() {

        boolean isEnable = false;

        if (!editTextName.getText().toString().equals(Constants.personTeamView.getTeamName())) {
            isEnable = true;
        }
        if (isEnable) {
            btnSave.setVisibility(View.VISIBLE);
        } else {
            btnSave.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case TAKE_PHOTO_REQUEST:
                    new ImageCompression().execute(Constants.imageFilePath);
                    break;
                case PICK_PHOTO_REQUEST:
                    Uri uri = data.getData();
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(projection[0]);
                    final String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ImageProcess.copyFile(picturePath, Constants.imageFilePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    new ImageCompression().execute(Constants.imageFilePath);
                    break;
                case PLACE_PICKER_REQUEST:
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    addMarker(place.getLatLng().latitude, place.getLatLng().longitude);

                    break;
            }
        }
    }

    @Override
    public void deleteClicked() {
        txtNoLocation.setVisibility(View.VISIBLE);
    }

    public class ImageCompression extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length == 0 || strings[0] == null)
                return null;
            return ImageProcess.compressImage(strings[0]);
        }

        protected void onPostExecute(String _imagePath) {
            String imagePath = new File(_imagePath).getAbsolutePath();
            showCropPhotoPopup(imagePath);
        }
    }

    private void showCropPhotoPopup(String imagePath) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_image_crop, null);
        popupCropPhoto = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        popupCropPhoto.setElevation(5.0f);

        final CropImageView imgProfilePhoto = popupView.findViewById(R.id.imageview_profile_photo);
        final Button btnCrop = popupView.findViewById(R.id.button_crop);
        final ImageView btnRotate = popupView.findViewById(R.id.button_rotate);
        final ImageView btnBack = popupView.findViewById(R.id.button_back);

        File file = new File(imagePath);
        final Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        imgProfilePhoto.setImageBitmap(myBitmap);
        imgProfilePhoto.setFixedAspectRatio(false);
        imgProfilePhoto.setCropShape(CropImageView.CropShape.RECTANGLE);

        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgProfilePhoto.rotateImage(90);
            }
        });

        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bmp = imgProfilePhoto.getCroppedImage();

                ImageUploadToServerFunction(bmp);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupCropPhoto.dismiss();
            }
        });

        popupCropPhoto.showAtLocation(view, Gravity.CENTER, 0, 0);

    }

    public void ImageUploadToServerFunction(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStreamObject = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
        byte[] byteArray = byteArrayOutputStreamObject.toByteArray();
        final String convertImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String imageName = Constants.personTeamView.getTeamId() + ".jpg";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", imageName);
        params.put("image_data", convertImage);
        params.put("type", Constants.UPLOAD_IMAGE_TYPE_LOGO);
        params.put("path", "/");
        params.put("user_id", firebaseUser.getUid());

        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.processing), getString(R.string.wait), false, false);

        Util.postRequest(context, URLs.urlUploadImage, params, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject jsonResult) {

                ImageUploadResult imageUploadResult = new Gson().fromJson(jsonResult.toString(), ImageUploadResult.class);
                if (imageUploadResult.getResult().equals(ImageUploadResultTypeEnum.OK.getValue())) {
                    Glide.with(context)
                            .load(Uri.parse(imageUploadResult.getUrl()))
                            .apply(new RequestOptions()
                                    .centerInside()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(R.drawable.icon_rectangle)
                                    .error(R.drawable.icon_rectangle))
                            .into(imgLogo);
                } else if (imageUploadResult.getResult().equals(ImageUploadResultTypeEnum.ERROR.getValue())) {
                    Util.toastError(context);
                }
                popupCropPhoto.dismiss();
                progressDialog.dismiss();
            }

            @Override
            public void onSuccessList(JSONArray result) {
            }

            @Override
            public void onError(String result) {
                Util.toastError(context);
                popupCropPhoto.dismiss();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private boolean checkWritePermission() {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestWritePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                WRITE_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == WRITE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPhotoDialog();
            } else {
            }
        }
    }

}

