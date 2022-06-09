package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.hvl.dragonteam.DataService.AttendanceService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Attendance;
import com.hvl.dragonteam.Model.Enum.LocationEnum;
import com.hvl.dragonteam.Model.PersonTrainingAttendance;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TrainingAttendanceAdapter extends RecyclerView.Adapter<TrainingAttendanceAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<PersonTrainingAttendance> listTraining = new ArrayList<>();
    private boolean isNext;
    private Context context;
    private Util util;

    public TrainingAttendanceAdapter(Context context, ArrayList<PersonTrainingAttendance> listTraining, boolean isNext) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listTraining = listTraining;
        this.isNext = isNext;
        util = new Util();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_training_attendance, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtTime.setText(Util.parseDate(listTraining.get(position).getTime(),Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss, Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm));
        holder.txtLocation.setText(LocationEnum.toLocationEnum(listTraining.get(position).getLocation()).toString());
        holder.txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationEnum locationEnum =  LocationEnum.toLocationEnum(listTraining.get(position).getLocation());
                String uri = "geo:" + locationEnum.getLat() + "," + locationEnum.getLon();
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
                if(b){
                    saveAttendance(attendance);
                } else {
                    deleteAttendance(attendance, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listTraining.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtTime;
        TextView txtLocation;
        Switch switchAttendance;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtLocation = itemView.findViewById(R.id.txt_context);
            txtLocation.setPaintFlags(txtLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            switchAttendance = itemView.findViewById(R.id.switch_attendance);
            switchAttendance.setClickable(isNext);
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

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
