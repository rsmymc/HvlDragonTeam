package com.hvl.dragonteam.Adapter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hvl.dragonteam.Model.Enum.LineupEnum;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.LineupItem;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.Util;
import com.hvl.dragonteam.Interface.DragListener;
import com.hvl.dragonteam.Interface.OnLineupChangeListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class LineupAdapter extends RecyclerView.Adapter<LineupAdapter.ViewHolder>  implements View.OnTouchListener {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private OnLineupChangeListener listener;
    private ArrayList<LineupItem> listLineup = new ArrayList<>();
    private Context context;

    private static final int LAYOUT_LEFT = 1;
    private static final int LAYOUT_RIGHT = 2;

    public LineupAdapter(Context context, ArrayList<LineupItem> listLineup, OnLineupChangeListener listener) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listLineup = listLineup;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {

        if(listLineup.get(position).getId() % 2 == 0) {
            return  LAYOUT_LEFT;
        } else {
            return  LAYOUT_RIGHT;
        }
    }


    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case LAYOUT_LEFT:
                v = mInflater.inflate(R.layout.list_item_lineup_left, parent, false);
                return new ViewHolder(v);
            case LAYOUT_RIGHT:
                v = mInflater.inflate(R.layout.list_item_lineup_right, parent, false);
                return new ViewHolder(v);
        }
        return null;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.txtPersonName.setText(Util.getShortName(listLineup.get(position).getPersonTrainingAttendance().getName()));
        holder.txtSide.setText(LineupEnum.toLineupEnum(listLineup.get(position).getId()).toString());
        Glide.with(context)
                .load(listLineup.get(position).getPersonTrainingAttendance().getProfilePictureUrl())
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .skipMemoryCache(false)
                        .placeholder(R.drawable.uniform2)
                        .error(R.drawable.uniform2))
                .into( holder.imgProfile);

        if(!listLineup.get(position).getPersonTrainingAttendance().getPersonId().equals(context.getString(R.string.empty))) {
            if( listLineup.get(position).getPersonTrainingAttendance().getSide() == SideEnum.BOTH.getValue() ||
                    position % 2 == listLineup.get(position).getPersonTrainingAttendance().getSide()) {
                holder.txtSide.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.chat_sender_color10)));
            } else {
                holder.txtSide.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.red)));
            }
            holder.txtPersonName.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.colorPrimaryLight)));
            holder.imgProfile.setAlpha(1f);
        } else {
            holder.txtSide.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.darkGray)));
            holder.txtPersonName.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.darkGray)));
            holder.imgProfile.setAlpha(0.5f);
        }
        holder.layoutLineup.setTag(position);
        holder.layoutLineup.setOnTouchListener(this);
        holder.layoutLineup.setOnDragListener(new DragListener(listener));
    }

    @Override
    public int getItemCount() {
        return listLineup.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView imgProfile;
        TextView txtPersonName;
        TextView txtSide;
        LinearLayout layoutLineup;

        public ViewHolder(View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.img_profile);
            txtPersonName = itemView.findViewById(R.id.txt_person_name);
            txtSide = itemView.findViewById(R.id.txt_side);
            layoutLineup = itemView.findViewById(R.id.layout_lineup);

        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(Constants.personTeamView.getRole() ==  RoleEnum.ADMIN.getValue()) {
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
        }
        return false;
    }

    public DragListener getDragInstance() {
        if (listener != null) {
            return new DragListener(listener);
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

    public ArrayList<LineupItem> getListLineup() {
        return listLineup;
    }

    public void updateListLineup(ArrayList<LineupItem> list) {
        this.listLineup = list;
    }

}
