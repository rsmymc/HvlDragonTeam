package com.hvl.dragonteam.Fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Adapter.MessageAdapter;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.Interface.MyFunction;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.ImageUploadResultTypeEnum;
import com.hvl.dragonteam.Model.Enum.MessageTypeEnum;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.ImageUploadResult;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Redis.ChatHistory;
import com.hvl.dragonteam.Redis.ChatMessage;
import com.hvl.dragonteam.Redis.ChatMessageFactory;
import com.hvl.dragonteam.Redis.RedisGroupChatProcess;
import com.hvl.dragonteam.Utilities.Constants;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FragmentChat extends Fragment {
    private View view;
    private FragmentActivity context;

    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView listViewMessages;
    private ImageView btnSend;
    private ImageView btnSendImage;
    private EditText txtMessage;

    private MessageAdapter messageAdapter;
    private ArrayList<ChatMessage> messageList = new ArrayList<>();

    private RedisGroupChatProcess chatProcess;
    private ChatHistory chatHistory;
    private Gson gson = new Gson();
    private String channel;
    private Toolbar toolbar;

    private ProgressDialog progressDialog;
    private PopupWindow popupCropPhoto;
    private final static int PICK_PHOTO_REQUEST = 100;
    private final static int TAKE_PHOTO_REQUEST = 101;
    private final static int WRITE_PERMISSION_REQUEST = 102;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.fragment_chat, container, false);
        context = (FragmentActivity) getContext();

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.chat));

        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.message_swipe_layout);
        listViewMessages = view.findViewById(R.id.messages_list);
        listViewMessages.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        txtMessage = view.findViewById(R.id.txtChatMessage);

        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = txtMessage.getText().toString();
                if (msg == null || msg.length() == 0) {
                    return;
                } else {
                    sendButtonPressed(msg);
                    txtMessage.setText("");
                }
            }
        });

        btnSendImage = view.findViewById(R.id.btnSendImage);
        btnSendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkWritePermission()) {
                    requestWritePermission();
                } else {
                    showPhotoDialog();
                }
            }
        });

        registerChat();

        SharedPrefHelper.getInstance(context).saveLong(Constants.personTeamView.getTeamId() + FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new Date().getTime());

        return view;
    }

    private void registerChat() {

        channel = Constants.REDIS_CHAT_PREFIX + Constants.personTeamView.getTeamId();
        chatHistory = new ChatHistory(channel);
        MyFunction<Collection<?>, Boolean> callback = new MyFunction<Collection<?>, Boolean>() {
            @Override
            public Boolean apply(Collection<?> chatList) {

                boolean result = chatHistory.add((List<String>) chatList);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (result == true) {
                            messageList = (chatHistory.getChatsArrayList());
                            messageAdapter = new MessageAdapter(context, messageList);
                            listViewMessages.setAdapter(messageAdapter);
                            messageAdapter.notifyDataSetChanged();
                            listViewMessages.scrollToPosition(messageList.size() - 1);
                            mRefreshLayout.setRefreshing(false);

                            SharedPrefHelper.getInstance(context).saveLong(Constants.personTeamView.getTeamId() + FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    new Date().getTime());
                        }
                    }
                });
                return result;
            }
        };

        chatProcess = new RedisGroupChatProcess(URLs.redisAddress);
        chatProcess.init();
        chatProcess.subscribe(channel, callback);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                chatProcess.getOldChatMessages(channel, callback);
                mRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void sendButtonPressed(String msg) {

        if (msg == null || msg.length() == 0) {
            return;
        } else {
            ChatMessage cm = ChatMessageFactory.getInstance().getChatMessage(
                    msg,
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    Constants.person.getName(),
                    MessageTypeEnum.TEXT);
            String cmJson = gson.toJson(cm);
            chatProcess.publish(channel, cmJson);
            getPersonsNotification(msg);
        }
    }

    private void showPhotoDialog() {
        Constants.imageFilePath = ImageProcess.getFilename();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })
                .setItems(R.array.chatImageArray, new DialogInterface.OnClickListener() {
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
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendMessageNotify(String tokens, String message) {

        Training training = new Training();
        training.setTeamId(Constants.personTeamView.getTeamId());

        NotificationModel notificationModel = new NotificationModel("",
                NotificationTypeEnum.CHAT_MESSAGE_NOTIFICATION.getValue(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                Constants.person.getName(),
                training);
        String json = new Gson().toJson(notificationModel, NotificationModel.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("token", tokens);
        params.put("body", Constants.person.getName() + " : " + message);
        params.put("title", Constants.personTeamView.getTeamName());
        params.put("notificationModel", json);
        Util.postRequest(context, URLs.urlSendNotification, params, null);
    }

    private void getPersonsNotification(String message) {

        NotificationService notificationService = new NotificationService();
        try {

            Team team = new Team();
            team.setId(Constants.personTeamView.getTeamId());

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

        String userId = firebaseUser.getUid();
        String imageName = userId + '_' + System.currentTimeMillis() + ".jpg";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", imageName);
        params.put("image_data", convertImage);
        params.put("type", Constants.UPLOAD_IMAGE_TYPE_CHAT);
        params.put("path", Util.setDirectoryFromFileName(userId));
        params.put("user_id", userId);

        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.processing), getString(R.string.wait), false, false);

        Util.postRequest(context, URLs.urlUploadImage, params, new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject jsonResult) {

                ImageUploadResult imageUploadResult = new Gson().fromJson(jsonResult.toString(), ImageUploadResult.class);

                if (imageUploadResult.getResult().equals(ImageUploadResultTypeEnum.OK.getValue())) {
                    ChatMessage cm = ChatMessageFactory.getInstance().getChatMessage(
                            imageUploadResult.getUrl(),
                            FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            Constants.person.getName(),
                            MessageTypeEnum.IMAGE);
                    String cmJson = gson.toJson(cm);
                    chatProcess.publish(channel, cmJson);
                    getPersonsNotification(getString(R.string.send_image));

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

    @Override
    public void onResume() {
        if (toolbar != null)
            toolbar.setTitle(getString(R.string.chat));
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }
}

