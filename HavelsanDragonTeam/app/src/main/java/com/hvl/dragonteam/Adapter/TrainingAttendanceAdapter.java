package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.DataService.AttendanceService;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Attendance;
import com.hvl.dragonteam.Model.Enum.LanguageEnum;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
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
import java.util.Locale;
import java.util.Map;

public class TrainingAttendanceAdapter extends RecyclerView.Adapter<TrainingAttendanceAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<PersonTrainingAttendance> listTraining = new ArrayList<>();
    private boolean isNext;
    private Context context;
    private static final int LAYOUT_ITEM = 1;
    private static final int LAYOUT_FOOTER = 2;


    public TrainingAttendanceAdapter(Context context, ArrayList<PersonTrainingAttendance> listTraining, boolean isNext) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listTraining = listTraining;
        listTraining.add(new PersonTrainingAttendance());
        this.isNext = isNext;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == listTraining.size() - 1) {
            return LAYOUT_FOOTER;
        } else {
            return LAYOUT_ITEM;
        }
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case LAYOUT_ITEM:
                v = mInflater.inflate(R.layout.list_item_training_attendance, parent, false);
                return new ViewHolder(v);
            case LAYOUT_FOOTER:
                v = mInflater.inflate(R.layout.list_item_footer, parent, false);
                return new ViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (holder.getItemViewType() == LAYOUT_ITEM) {
            holder.txtTime.setText(Util.parseDate(listTraining.get(position).getTime(), Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss, Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm));
            holder.txtLocation.setText(listTraining.get(position).getLocationName());
            holder.txtLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = "geo:" + listTraining.get(position).getLat() + "," + listTraining.get(position).getLon()
                            + "?q="+ listTraining.get(position).getLat()+","+listTraining.get(position).getLon() +"("+listTraining.get(position).getLocationName()+")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    context.startActivity(intent);
                }
            });

            holder.switchAttendance.setChecked(listTraining.get(position).isAttend());
            holder.switchAttendance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!compoundButton.isPressed()) {
                        return;
                    }
                    Attendance attendance = new Attendance(FirebaseAuth.getInstance().getCurrentUser().getUid(), listTraining.get(position).getTrainingId());
                    if (b) {
                        saveAttendance(attendance);
                    } else {
                        deleteAttendance(attendance, position);
                    }
                }
            });

            holder.imgNotify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getPersonsNotification(listTraining.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listTraining.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtTime;
        TextView txtLocation;
        Switch switchAttendance;
        ImageView imgNotify;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txt_time);

            txtLocation = itemView.findViewById(R.id.txt_context);
            if (txtLocation != null)
                txtLocation.setPaintFlags(txtLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            switchAttendance = itemView.findViewById(R.id.switch_attendance);
            if (switchAttendance != null)
                switchAttendance.setClickable(isNext);

            imgNotify = itemView.findViewById(R.id.img_notify);

            if (imgNotify != null && (Constants.personTeamView.getRole() != RoleEnum.ADMIN.getValue() || !isNext)) {
                imgNotify.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    private void saveAttendance(final Attendance attendance) {
        AttendanceService attendanceService = new AttendanceService();

        try {
            attendanceService.saveAttendance(context,
                    attendance,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
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

    private void deleteAttendance(final Attendance attendance, int position) {
        AttendanceService attendanceService = new AttendanceService();

        try {
            attendanceService.deleteAttendance(context,
                    attendance,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
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

    private void sendMessageNotify(String tokens, String message) {

        Training training = new Training();
        training.setTeamId(Constants.personTeamView.getTeamId());

        NotificationModel notificationModel = new NotificationModel("",
                NotificationTypeEnum.NONE.getValue(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                Constants.person.getName(),
                training);
        String json = new Gson().toJson(notificationModel, NotificationModel.class);

        Map<String, String> params = new HashMap<String, String>();
        params.put("token", tokens);
        params.put("body",   message);
        params.put("title", Constants.personTeamView.getTeamName());
        params.put("notificationModel", json);
        Util.postRequest(context, URLs.urlSendNotification, params, null);
    }

    private void getPersonsNotification(PersonTrainingAttendance training) {

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

                            for (PersonNotification personNotification : list) {
                                if (personNotification.isLoggedIn() && !personNotification.getPersonId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && personNotification.getNotification1()) {

                                    JSONArray tokens = new JSONArray();
                                    tokens.put(personNotification.getToken());

                                    String time =  Util.parseDate(training.getTime(), Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss, Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm);
                                    sendMessageNotify(tokens.toString(),  Util.getLocalizedString(context,
                                            new Locale(LanguageEnum.toLanguageEnum(personNotification.getLanguageType()).getLocale()),
                                            R.string.attendance_notification).replace("XXX", time + " "  + training.getLocationName()).replace("null",""));
                                }
                            }

                            Util.toastInfo(context, context.getString(R.string.info_notify_lineup));
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


    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
