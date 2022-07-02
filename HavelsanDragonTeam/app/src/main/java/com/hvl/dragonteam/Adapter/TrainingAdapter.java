package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.Model.TrainingLocationView;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import java.util.ArrayList;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<TrainingLocationView> listTraining = new ArrayList<>();
    private Context context;

    public TrainingAdapter(Context context, ArrayList<TrainingLocationView> listTraining) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listTraining = listTraining;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_training, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtTime.setText(Util.parseDate(listTraining.get(position).getTime(),Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss, Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm));
        holder.txtLocation.setText(listTraining.get(position).getLocation().getName());
        holder.txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "geo:" + listTraining.get(position).getLocation().getLat() + "," + listTraining.get(position).getLocation().getLon()
                        + "?q="+ listTraining.get(position).getLocation().getLat()+","+listTraining.get(position).getLocation().getLon() +"("+listTraining.get(position).getLocation().getName()+")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(intent);
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

        public ViewHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtLocation = itemView.findViewById(R.id.txt_context);
            txtLocation.setPaintFlags(txtLocation.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
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
