package com.hvl.dragonteam.Adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Util;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonTeamAdapter extends RecyclerView.Adapter<PersonTeamAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<PersonTeamView> listPersonTeam = new ArrayList<>();
    private Context context;
    private Util util;
    private static final int CALL_PERMISSIONS_REQUEST = 102;

    public PersonTeamAdapter(Context context, ArrayList<PersonTeamView> listPersonTeam) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listPersonTeam = listPersonTeam;
        util = new Util();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_person_team, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtName.setText(listPersonTeam.get(position).getPersonName());
        holder.txtHeight.setText(listPersonTeam.get(position).getHeight() + " cm");
        holder.txtWeight.setText(listPersonTeam.get(position).getWeight() + " kg");
        holder.txtSide.setText(SideEnum.toSideEnum(listPersonTeam.get(position).getSide()).name().substring(0, 1));
        Glide.with(context)
                .load(listPersonTeam.get(position).getProfilePictureUrl())
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .skipMemoryCache(false)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into( holder.imgProfile);

        holder.imgCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCall(listPersonTeam.get(position).getPhone());
            }
        });
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
    public int getItemCount() {
        return listPersonTeam.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        TextView txtName;
        TextView txtHeight;
        TextView txtWeight;
        TextView txtSide;
        ImageView imgCall;
        CircleImageView imgProfile;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_person_name);
            txtHeight = itemView.findViewById(R.id.txt_height);
            txtWeight = itemView.findViewById(R.id.txt_weight);
            txtSide = itemView.findViewById(R.id.txt_side);
            imgCall = itemView.findViewById(R.id.img_call);
            imgProfile = itemView.findViewById(R.id.img_profile);
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
