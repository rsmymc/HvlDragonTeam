package com.hvl.dragonteam.Adapter;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.hvl.dragonteam.DataService.PersonService;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.OnIntentReceived;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.ImageUploadResultTypeEnum;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.ImageUploadResult;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.CustomTypingEditText;
import com.hvl.dragonteam.Utilities.ImageProcess;
import com.hvl.dragonteam.Utilities.StringMatcher;
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
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PersonTeamByTeamAdapter extends ArrayAdapter<PersonTeamView> implements SectionIndexer, OnIntentReceived, ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;
    private String mSections;
    private PopupWindow popupCropPhoto;
    private ProgressDialog progressDialog;
    private PersonTeamView selectedPersonTeamView;
    private View editProfileView ;
    private OnPersonTeamChangeListener listener;
    private final static int PICK_PHOTO_REQUEST = 300;
    private final static int TAKE_PHOTO_REQUEST = 301;
    public final static int WRITE_PERMISSION_REQUEST = 302;
    private final static int CALL_PERMISSIONS_REQUEST = 303;

    public PersonTeamByTeamAdapter(Context context, ArrayList<PersonTeamView> listPersonTeam, String mSections, OnPersonTeamChangeListener onPersonTeamChangeListener) {
        super(context, 0, listPersonTeam);
        this.mSections = mSections;
        this.context = context;
        this.listener = onPersonTeamChangeListener;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        PersonTeamView personTeamView = getItem(position);

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_person_team, parent, false);
        }

        TextView txtName = itemView.findViewById(R.id.txt_person_name);
        TextView txtHeight = itemView.findViewById(R.id.txt_height);
        TextView txtWeight = itemView.findViewById(R.id.txt_weight);
        TextView txtSide = itemView.findViewById(R.id.txt_side);
        ImageView imgCall = itemView.findViewById(R.id.img_call);
        ImageView imgMore = itemView.findViewById(R.id.img_more);
        CircleImageView imgProfile = itemView.findViewById(R.id.img_profile);

        txtName.setText(personTeamView.getPersonName());
        txtHeight.setText(personTeamView.getHeight() + " cm");
        txtWeight.setText(personTeamView.getWeight() + " kg");
        txtSide.setText(SideEnum.toSideEnum(personTeamView.getSide()).name().substring(0, 1));
        Glide.with(context)
                .load(personTeamView.getProfilePictureUrl())
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into(imgProfile);

        imgCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCall(personTeamView.getPhone());
            }
        });

        if (Constants.personTeamView.getRole() == RoleEnum.ADMIN.getValue() && !personTeamView.getPersonId().equals(Constants.personTeamView.getPersonId())) {
            imgMore.setVisibility(View.VISIBLE);
        } else {
            imgMore.setVisibility(View.INVISIBLE);
        }

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenuInflater().inflate(R.menu.menu_team_person_popup, popup.getMenu());

                if (personTeamView.getPersonId().equals(Constants.personTeamView.getPersonId())) {
                    popup.getMenu().getItem(R.id.action_remove_from_team).setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case (R.id.action_set_role): {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View view = inflater.inflate(R.layout.dialog_update_role, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setView(view);
                                builder.setCancelable(false);
                                final RadioGroup radioGroupRole = (RadioGroup) view.findViewById(R.id.radio_group_role);

                                ((RadioButton) radioGroupRole.getChildAt(personTeamView.getRole())).setChecked(true);

                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        int radioButtonID = radioGroupRole.getCheckedRadioButtonId();
                                        View radioButton = radioGroupRole.findViewById(radioButtonID);
                                        int id = radioGroupRole.indexOfChild(radioButton);
                                        savePersonTeam(personTeamView, id);
                                    }
                                });
                                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                                break;
                            }
                            case (R.id.action_remove_from_team): {
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                deletePersonTeam(personTeamView);
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage(context.getString(R.string.warning_delete_person_team))
                                        .setPositiveButton(context.getString(R.string.ok), dialogClickListener)
                                        .setNegativeButton(context.getString(R.string.cancel), dialogClickListener).show();
                                break;
                            }

                            case (R.id.action_edit_person): {
                                showEditDialog(personTeamView);
                            }
                        }
                        return true;
                    }
                });
                popup.show();//showing popup menu
            }
        });

        return itemView;
    }

    private void showEditDialog(PersonTeamView personTeamView) {

        selectedPersonTeamView = personTeamView;

        LayoutInflater inflater = LayoutInflater.from(context);

        editProfileView = inflater.inflate(R.layout.layout_edit_profile, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(editProfileView);
        builder.setCancelable(false);

        CircleImageView imgProfile = (CircleImageView) editProfileView.findViewById(R.id.img_profile);
        CustomTypingEditText editTextName = (CustomTypingEditText) editProfileView.findViewById(R.id.txt_name);
        TextView editTextPhone = (TextView) editProfileView.findViewById(R.id.txt_phone);
        CustomTypingEditText editTextHeight = (CustomTypingEditText) editProfileView.findViewById(R.id.txt_height);
        CustomTypingEditText editTextWeight = (CustomTypingEditText) editProfileView.findViewById(R.id.txt_weight);
        Spinner spinnerSide = (Spinner) editProfileView.findViewById(R.id.spinner_side);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.side_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSide.setAdapter(adapter);

        Glide.with(context)
                .load(personTeamView.getProfilePictureUrl())
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into(imgProfile);

        editTextName.setText(personTeamView.getPersonName() == null ?  "" : personTeamView.getPersonName());
        editTextPhone.setText(personTeamView.getPhone());
        editTextHeight.setText(String.valueOf(personTeamView.getHeight()));
        editTextWeight.setText(String.valueOf(personTeamView.getWeight()));
        spinnerSide.setSelection(personTeamView.getSide());

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int height = editTextHeight.getText().toString().trim().equals("") ? 0 : Integer.parseInt(editTextHeight.getText().toString());
                int weight = editTextWeight.getText().toString().trim().equals("") ? 0 : Integer.parseInt(editTextWeight.getText().toString());
                Person person = new Person(selectedPersonTeamView.getPersonId(),
                        editTextName.getText().toString(),
                        editTextPhone.getText().toString(),
                        height,
                        weight,
                        spinnerSide.getSelectedItemPosition(),
                        selectedPersonTeamView.getProfilePictureUrl());

                saveUser(person);
            }
        });


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

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void showPhotoDialog() {
        Constants.imageFilePath = ImageProcess.getFilename();

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                                intent.putExtra("TEST", "YourExtraStringData"); // Set the extra string data
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);   // set the image file displayName
                                ((FragmentActivity) context).startActivityForResult(Intent.createChooser(intent, "Select File"),
                                        PICK_PHOTO_REQUEST);

                                break;
                            }
                            case 1: {
                                imageFile = new File(Constants.imageFilePath);
                                imageFileUri = FileProvider.getUriForFile(context, "com.hvl.dragonteam.GenericFileProvider", imageFile);
                                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra("TEST", "YourExtraStringData"); // Set the extra string data
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
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
                                builder.setMessage(context.getString(R.string.question_delete_photo))
                                        .setPositiveButton(context.getString(R.string.remove), dialogClickListener)
                                        .setNegativeButton(context.getString(R.string.cancel), dialogClickListener).show();

                                break;
                            }
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteImage() {
        CircleImageView imgProfile = (CircleImageView) editProfileView.findViewById(R.id.img_profile);

        Glide.with(context)
                .load( "deleted")
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into(imgProfile);

        Person person = new Person(selectedPersonTeamView.getPersonId(),
                selectedPersonTeamView.getPersonName(),
                selectedPersonTeamView.getPhone(),
                selectedPersonTeamView.getHeight(),
                selectedPersonTeamView.getWeight(),
                selectedPersonTeamView.getSide(),
                "deleted");
        selectedPersonTeamView.setProfilePictureUrl("deleted");

        saveUser(person);
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
        final ImageView btnCrop = popupView.findViewById(R.id.img_tick);
        final ImageView btnRotate = popupView.findViewById(R.id.img_rotate);
        final ImageView btnBack = popupView.findViewById(R.id.img_back);

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

        popupCropPhoto.showAtLocation(editProfileView, Gravity.CENTER, 0, 0);

    }

    public void ImageUploadToServerFunction(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStreamObject = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
        byte[] byteArray = byteArrayOutputStreamObject.toByteArray();
        final String convertImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        String userId = selectedPersonTeamView.getPersonId();
        String imageName = userId + ".jpg";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", imageName);
        params.put("image_data", convertImage);
        params.put("type", Constants.UPLOAD_IMAGE_TYPE_PROFILE);
        params.put("path", Util.setDirectoryFromFileName(userId));
        params.put("user_id", userId);

        progressDialog = ProgressDialog.show(context, context.getString(R.string.processing), context.getString(R.string.wait), false, false);

        Util.postRequest(context, URLs.urlUploadImage, params, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject jsonResult) {

                ImageUploadResult imageUploadResult = new Gson().fromJson(jsonResult.toString(), ImageUploadResult.class);

                if (imageUploadResult.getResult().equals(ImageUploadResultTypeEnum.OK.getValue())) {

                    CircleImageView imgProfile = (CircleImageView) editProfileView.findViewById(R.id.img_profile);

                    Glide.with(context)
                            .load( imageUploadResult.getUrl())
                            .apply(new RequestOptions()
                                    .centerInside()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(R.drawable.user_placeholder)
                                    .error(R.drawable.user_placeholder))
                            .into(imgProfile);

                    Person person = new Person(selectedPersonTeamView.getPersonId(),
                            selectedPersonTeamView.getPersonName(),
                            selectedPersonTeamView.getPhone(),
                            selectedPersonTeamView.getHeight(),
                            selectedPersonTeamView.getWeight(),
                            selectedPersonTeamView.getSide(),
                            imageUploadResult.getUrl());

                    selectedPersonTeamView.setProfilePictureUrl(imageUploadResult.getUrl());

                    saveUser(person);

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

    private void saveUser(Person person) {
        PersonService personService = new PersonService();
        ProgressDialog progressDialog = ProgressDialog.show(context, context.getString(R.string.processing), context.getString(R.string.wait), false, false);
        try {
            personService.savePerson(context,
                    person,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Person person = new Gson().fromJson(result.toString(), Person.class);
                            progressDialog.dismiss();
                            listener.personTeamChange();
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

    private void deletePersonTeam(PersonTeamView personTeamView) {
        PersonTeamService personTeamService = new PersonTeamService();
        try {
            PersonTeam personTeam = new PersonTeam();
            personTeam.setTeamId(personTeamView.getTeamId());
            personTeam.setPersonId(personTeamView.getPersonId());
            personTeamService.deletePersonTeam(context,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Util.toastInfo(context, context.getString(R.string.info_team_updated));
                            listener.personTeamChange();
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
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

    private void savePersonTeam(PersonTeamView personTeamView, int role) {
        PersonTeamService personTeamService = new PersonTeamService();
        PersonTeam personTeam = new PersonTeam(personTeamView.getPersonId(), personTeamView.getTeamId(), role);

        try {
            personTeamService.savePersonTeam(context,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Util.toastInfo(context, context.getString(R.string.info_team_updated));
                            listener.personTeamChange();
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
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

    public void onCall(String phoneNumber) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSIONS_REQUEST);
        } else {
            context.startActivity(new Intent(Intent.ACTION_DIAL).setData(Uri.parse("tel:" + phoneNumber)));
        }
    }

    @Override
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(getItem(j).getPersonName().charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(getItem(j).getPersonName().charAt(0)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
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
        ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
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

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    @Override
    public void onIntent(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case TAKE_PHOTO_REQUEST:
                new ImageCompression().execute(Constants.imageFilePath);
                break;
            case PICK_PHOTO_REQUEST:

                Uri uri = data.getData();
                String[] projection = {MediaStore.Images.Media.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(projection[0]);
                final String picturePath = cursor.getString(columnIndex);
                cursor.close();

                ((FragmentActivity) context).runOnUiThread(new Runnable() {
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

    public interface OnPersonTeamChangeListener {
        void personTeamChange();
    }
}
