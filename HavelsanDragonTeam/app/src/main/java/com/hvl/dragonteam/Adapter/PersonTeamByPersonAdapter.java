package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Model.Announcement;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.Util;

import java.util.ArrayList;
import java.util.List;

public class PersonTeamByPersonAdapter extends RecyclerView.Adapter<PersonTeamByPersonAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<PersonTeamView> listPersonTeam = new ArrayList<>();
    private Context context;
    private Util util = new Util();

    public PersonTeamByPersonAdapter(Context context, ArrayList<PersonTeamView> listPersonTeam) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listPersonTeam = listPersonTeam;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_team, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtTeamName.setText(listPersonTeam.get(position).getTeamName());
        holder.txtTeamId.setText(listPersonTeam.get(position).getTeamId());
        holder.imgCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                util.setClipboard(context, listPersonTeam.get(position).getTeamId());
                Util.toastInfo(context, R.string.info_clipboard);
            }
        });
        holder.imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnCompleteListener<ShortDynamicLink> onCompleteListener = new OnCompleteListener<ShortDynamicLink>() {
                    @Override
                    public void onComplete(@NonNull Task<ShortDynamicLink> task) {
                        if (task.isSuccessful()) {
                            Uri shortLink = task.getResult().getShortLink();
                            //Uri previewLink = task.getResult().getPreviewLink();
                            util.shareTextIntent(context.getString(R.string.info_share_team)
                                    .replace("LLL", shortLink.toString())
                                    .replace("XXX", listPersonTeam.get(position).getTeamId())
                                    .replace("YYY", listPersonTeam.get(position).getTeamName()), context);
                        } else {
                        }
                    }
                };
                util.createDynamicLink(listPersonTeam.get(position).getTeamId(),onCompleteListener);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listPersonTeam.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {

        TextView txtTeamName;
        TextView txtTeamId;
        ImageView imgCopy;
        ImageView imgShare;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTeamName = itemView.findViewById(R.id.txt_team_name);
            txtTeamId = itemView.findViewById(R.id.txt_team_id);
            imgCopy = itemView.findViewById(R.id.img_copy);
            imgShare = itemView.findViewById(R.id.img_share);
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
