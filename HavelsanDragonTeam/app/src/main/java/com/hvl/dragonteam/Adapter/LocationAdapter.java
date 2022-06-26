package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hvl.dragonteam.Model.LocationModel;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<LocationModel> listLocation = new ArrayList<>();
    private Context context;
    private Util util;

    public LocationAdapter(Context context, ArrayList<LocationModel> listLocation) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listLocation = listLocation;
        util = new Util();
    }


    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtName.setText(listLocation.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return listLocation.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
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
