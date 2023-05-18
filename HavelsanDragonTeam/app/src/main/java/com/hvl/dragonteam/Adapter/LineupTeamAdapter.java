package com.hvl.dragonteam.Adapter;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hvl.dragonteam.DataService.AttendanceService;
import com.hvl.dragonteam.Interface.DragListener;
import com.hvl.dragonteam.Interface.OnLineupChangeListener;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Attendance;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.FilterModel;
import com.hvl.dragonteam.Model.PersonTrainingAttendance;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LineupTeamAdapter extends RecyclerView.Adapter<LineupTeamAdapter.ViewHolder> implements View.OnTouchListener {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private OnLineupChangeListener lineupChangeListener;
    private Context context;
    private Util util;
    private ArrayList<PersonTrainingAttendance> personTrainingAttendanceList = new ArrayList<>();
    private FilterModel filterModel;
    private Training training;

    public LineupTeamAdapter(Context context, ArrayList<PersonTrainingAttendance> personTrainingAttendanceList, Training training, FilterModel filterModel, OnLineupChangeListener lineupChangeListener) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.personTrainingAttendanceList = personTrainingAttendanceList;
        this.training = training;
        this.filterModel = filterModel;
        this.lineupChangeListener = lineupChangeListener;
        util = new Util();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_lineup_team, parent, false);

        return new LineupTeamAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

         if (filterModel.isHideDontAttend() && !personTrainingAttendanceList.get(position).isAttend()) {
            holder.layout_hide();
        } else if (personTrainingAttendanceList.get(position).getSide() == SideEnum.LEFT.getValue() && !filterModel.isLeft()) {
            holder.layout_hide();
        } else if (personTrainingAttendanceList.get(position).getSide() == SideEnum.RIGHT.getValue() && !filterModel.isRight()) {
            holder.layout_hide();
        } else if (personTrainingAttendanceList.get(position).getSide() == SideEnum.BOTH.getValue() && !filterModel.isBoth()) {
            holder.layout_hide();
        } else {
            holder.layout_show();
            Glide.with(context)
                    .load(personTrainingAttendanceList.get(position).getProfilePictureUrl())
                    .apply(new RequestOptions()
                            .centerInside()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .skipMemoryCache(false)
                            .placeholder(R.drawable.uniform2)
                            .error(R.drawable.uniform2))
                    .into(holder.imgProfile);

            holder.txtName.setText(Util.getShortName(personTrainingAttendanceList.get(position).getName()) + " , " +
                    SideEnum.toSideEnum(personTrainingAttendanceList.get(position).getSide()).name().substring(0, 1) + " ,  " +
                    personTrainingAttendanceList.get(position).getWeight() + "kg");
            holder.checkboxAttendance.setChecked(personTrainingAttendanceList.get(position).isAttend());
            holder.checkboxAttendance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!compoundButton.isPressed()) {
                        return;
                    }
                    Attendance attendance = new Attendance(personTrainingAttendanceList.get(position).getPersonId(), training.getId());
                    if (b) {
                        saveAttendance(attendance, position);
                    } else {
                        deleteAttendance(attendance, position);
                    }
                }
            });

            holder.layoutLineup.setTag(position);
            holder.layoutLineup.setOnTouchListener(this);
            holder.layoutLineup.setOnDragListener(new DragListener(lineupChangeListener));

             if(filterModel.isHideImage()){
                 holder.imgProfile.setVisibility(View.GONE);
                 holder.checkboxAttendance.setVisibility(View.GONE);
             } else {
                 holder.imgProfile.setVisibility(View.VISIBLE);
                 holder.checkboxAttendance.setVisibility(View.VISIBLE);
             }
        }
    }

    @Override
    public int getItemCount() {
        return personTrainingAttendanceList.size();
    }

    private void saveAttendance(final Attendance attendance, int position) {
        AttendanceService attendanceService = new AttendanceService();

        try {
            attendanceService.saveAttendance(context,
                    attendance,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            personTrainingAttendanceList.get(position).setAttend(true);
                            notifyDataSetChanged();
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
                            personTrainingAttendanceList.get(position).setAttend(false);
                            notifyDataSetChanged();
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


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout.LayoutParams params;

        ImageView imgProfile;
        CheckBox checkboxAttendance;
        TextView txtName;
        LinearLayout layoutLineup;

        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_profile);
            checkboxAttendance = itemView.findViewById(R.id.checkbox_attendance);
            txtName = itemView.findViewById(R.id.txt_name);
            layoutLineup = itemView.findViewById(R.id.layout_lineup);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }

        private void layout_hide() {
            params.height = 0;
            layoutLineup.setLayoutParams(params);
        }

        private void layout_show() {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            params.setMargins((int) context.getResources().getDimension(R.dimen.frame_horizontal_margin)
                    , 0,// (int) context.getResources().getDimension(R.dimen.frame_horizontal_margin),
                    (int) context.getResources().getDimension(R.dimen.frame_horizontal_margin),
                    (int) context.getResources().getDimension(R.dimen.frame_horizontal_margin));
            params.gravity = Gravity.CENTER_HORIZONTAL;
            layoutLineup.setLayoutParams(params);

            //layoutLineup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    v.startDragAndDrop(data, shadowBuilder, v, 0);
                } else {
                    v.startDrag(data, shadowBuilder, v, 0);
                }
                return true;
        }
        return false;
    }

    public DragListener getDragInstance() {
        if (lineupChangeListener != null) {
            return new DragListener(lineupChangeListener);
        } else {
            Log.e("ListAdapter", "Listener wasn't initialized!");
            return null;
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

    public ArrayList<PersonTrainingAttendance> getListLineupPerson() {
        return personTrainingAttendanceList;
    }

    public void updateListLineupPerson(ArrayList<PersonTrainingAttendance> list) {
        this.personTrainingAttendanceList = list;
    }

    public FilterModel getFilterModel() {
        return filterModel;
    }

    public void setFilterModel(FilterModel filterModel) {
        this.filterModel = filterModel;
    }
}
