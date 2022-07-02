package com.hvl.dragonteam.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hvl.dragonteam.DataService.LocationService;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.LocationModel;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteLocation(listLocation.get(position));
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getString(R.string.warning_delete_location))
                        .setPositiveButton(context.getString(R.string.ok), dialogClickListener)
                        .setNegativeButton(context.getString(R.string.cancel), dialogClickListener).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listLocation.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        ImageView imgDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            imgDelete = itemView.findViewById(R.id.img_delete);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }
    }

    private void deleteLocation(LocationModel locationModel) {
        //view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        LocationService locationService = new LocationService();
        try {
            locationService.deleteLocation(context,
                    locationModel,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Util.toastInfo(context, context.getString(R.string.info_location_deleted));
                            listLocation.remove(locationModel);
                            notifyDataSetChanged();

                            if(listLocation.size() == 0 ) {
                                mDeleteCallback.deleteClicked();
                            }
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
                            //view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
            //view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
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

    OnDeleteClickedListener mDeleteCallback;
    public void setOnDeleteClickedListener(OnDeleteClickedListener mCallback) {
        this.mDeleteCallback = mCallback;
    }

    public interface OnDeleteClickedListener {
        void deleteClicked();
    }

}
