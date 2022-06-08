package com.hvl.dragonteam.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.hvl.dragonteam.Activity.ActivityLogin;
import com.hvl.dragonteam.Activity.ActivityTeam;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.DataService.PersonService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.ImageUploadResultTypeEnum;
import com.hvl.dragonteam.Model.ImageUploadResult;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.CustomTypingEditText;
import com.hvl.dragonteam.Utilities.ImageProcess;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.URLs;
import com.hvl.dragonteam.Utilities.Util;
import com.theartofdev.edmodo.cropper.CropImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FragmentProfile extends Fragment {
    View view;
    private CircleImageView imgProfile;
    private CustomTypingEditText editTextName;
    private TextView editTextPhone;
    private CustomTypingEditText editTextHeight;
    private CustomTypingEditText editTextWeight;
    private Spinner spinnerSide;
    private Button btnSave;
    private Switch switchNotification1;
    private FirebaseUser firebaseUser;
    private TextView txtPrivacy;
    private Context context;
    private ProgressDialog progressDialog;
    private final static int PICK_PHOTO_REQUEST = 100;
    private final static int TAKE_PHOTO_REQUEST = 101;
    private final static int WRITE_PERMISSION_REQUEST = 102;
    private PopupWindow popupCropPhoto;
    private PersonNotification personNotification = new PersonNotification();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        context = getContext();

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.profile));

        imgProfile = (CircleImageView) view.findViewById(R.id.img_profile);
        editTextName = (CustomTypingEditText) view.findViewById(R.id.txt_name);
        editTextPhone = (TextView) view.findViewById(R.id.txt_phone);
        editTextHeight = (CustomTypingEditText) view.findViewById(R.id.txt_height);
        editTextWeight = (CustomTypingEditText) view.findViewById(R.id.txt_weight);
        spinnerSide = (Spinner) view.findViewById(R.id.spinner_side);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.side_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSide.setAdapter(adapter);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        switchNotification1 = (Switch) view.findViewById(R.id.switchNotification1);
        txtPrivacy = (TextView) view.findViewById(R.id.txt_privacy);

        Glide.with(context)
                .load(Constants.person.getProfilePictureUrl())
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .skipMemoryCache(false)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into(imgProfile);

        editTextName.setText(Constants.person.getName() == null ?  "" : Constants.person.getName());
        editTextPhone.setText(Constants.person.getPhone());
        editTextHeight.setText(String.valueOf(Constants.person.getHeight()));
        editTextWeight.setText(String.valueOf(Constants.person.getWeight()));
        spinnerSide.setSelection(Constants.person.getSide());

        CustomTypingEditText.OnTypingModified onTypingModified = new CustomTypingEditText.OnTypingModified() {
            @Override
            public void onIsTypingModified(EditText view, boolean isTyping) {
                manageSaveButton();
            }
        };

        editTextName.setOnTypingModified(onTypingModified);
        editTextHeight.setOnTypingModified(onTypingModified);
        editTextWeight.setOnTypingModified(onTypingModified);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkWritePermission()) {
                    requestWritePermission();
                } else {
                    showPhotoDialog();
                }
            }
        });

        spinnerSide.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 manageSaveButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUser();
            }
        });

        txtPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URLs.urlPrivacyPolicy));
                startActivity(browserIntent);
            }
        });

        switchNotification1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (!compoundButton.isPressed()) {
                    return;
                }
                personNotification.setNotification1(b);
                saveNotification();
            }
        });

        getNotification();

        return view;
    }

    private void getNotification() {

        NotificationService notificationService = new NotificationService();
        PersonNotification _personNotification = new PersonNotification(firebaseUser.getUid(), FirebaseInstanceId.getInstance().getToken());
        try {
            notificationService.getNotification(context,
                    _personNotification,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {

                            personNotification = new Gson().fromJson(result.toString(), PersonNotification.class);

                            switchNotification1.setChecked(personNotification.getNotification1());
                        }

                        @Override
                        public void onError(String result) {
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
        }
    }

    private void saveNotification() {

        NotificationService notificationService = new NotificationService();

        try {
            notificationService.saveNotification(context, personNotification, null);
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
        }
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
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse("deleted"))
                .build();

        firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Glide.with(context)
                            .load(Uri.parse("deleted"))
                            .apply(new RequestOptions()
                                    .centerInside()
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .skipMemoryCache(false)
                                    .placeholder(R.drawable.user_placeholder)
                                    .error(R.drawable.user_placeholder))
                            .into(imgProfile);
                    Constants.person.setProfilePictureUrl("deleted");
                    saveUser();
                } else {
                    Util.toastError(context, task.getException().getMessage());
                }
            }
        });
    }

    private void saveUser() {
        PersonService personService = new PersonService();
        progressDialog = ProgressDialog.show(context, getResources().getString(R.string.processing), getResources().getString(R.string.wait), false, false);
        Person person = new Person(firebaseUser.getUid(),
                editTextName.getText().toString(),
                editTextPhone.getText().toString(),
                Integer.parseInt(editTextHeight.getText().toString()),
                Integer.parseInt(editTextWeight.getText().toString()),
                spinnerSide.getSelectedItemPosition(),
                Constants.person.getProfilePictureUrl());
        try {
            personService.savePerson(context,
                    person,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Person person = new Gson().fromJson(result.toString(), Person.class);
                            Constants.person = person;
                            manageSaveButton();
                            progressDialog.dismiss();
                            Util.toastInfo(context, R.string.info_profile_updated);
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

    private void manageSaveButton(){

        boolean isEnable = false;

        if(!editTextName.getText().toString().equals(Constants.person.getName())){
            isEnable = true;
        } else if(Integer.parseInt(editTextHeight.getText().toString()) != Constants.person.getHeight()){
            isEnable = true;
        } else if(Integer.parseInt(editTextWeight.getText().toString()) != Constants.person.getWeight()){
            isEnable = true;
        } else if(spinnerSide.getSelectedItemPosition() != Constants.person.getSide()){
            isEnable = true;
        }
        if(isEnable){
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
            }
        }
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
        imgProfilePhoto.setFixedAspectRatio(true);
        imgProfilePhoto.setAspectRatio(1, 1);
        imgProfilePhoto.setCropShape(CropImageView.CropShape.OVAL);

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

        String userId = firebaseUser.getUid();
        String imageName = userId + ".jpg";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", imageName);
        params.put("image_data", convertImage);
        params.put("type", Constants.UPLOAD_IMAGE_TYPE_PROFILE);
        params.put("path", Util.setDirectoryFromFileName(userId));
        params.put("user_id", userId);

        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.processing), getString(R.string.wait), false, false);

        Util.postRequest(context, URLs.urlUploadImage, params, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject jsonResult) {

                ImageUploadResult imageUploadResult = new Gson().fromJson(jsonResult.toString(), ImageUploadResult.class);

                if (imageUploadResult.getResult().equals(ImageUploadResultTypeEnum.OK.getValue())) {
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(imageUploadResult.getUrl()))
                            .build();

                    firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Glide.with(context)
                                        .load(Uri.parse(imageUploadResult.getUrl()))
                                        .apply(new RequestOptions()
                                                .centerInside()
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                .skipMemoryCache(false)
                                                .placeholder(R.drawable.user_placeholder)
                                                .error(R.drawable.user_placeholder))
                                        .into(imgProfile);
                                Constants.person.setProfilePictureUrl(imageUploadResult.getUrl());
                                saveUser();
                            } else {
                                Util.toastError(context, task.getException().getMessage());
                            }
                        }
                    });
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
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_change_team): {
                SharedPrefHelper.getInstance(getContext()).saveString(Constants.TAG_LAST_SELECTED_TEAM, null);
                Intent intent = new Intent(getContext(), ActivityTeam.class);
                startActivity(intent);
                getActivity().finish();
                break;
            }
            case (R.id.action_logout): {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                personNotification.setLoggedIn(false);
                                saveNotification();
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getContext(), ActivityLogin.class);
                                startActivity(intent);
                                getActivity().finish();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(getString(R.string.warning_logout))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
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

