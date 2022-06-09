package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Model.Announcement;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.Util;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<Announcement> listAnnouncement = new ArrayList<>();
    private Context context;
    private Util util;

    public AnnouncementAdapter(Context context, ArrayList<Announcement> listAnnouncement) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listAnnouncement = listAnnouncement;
        SharedPrefHelper.getInstance(context).getString(Constants.TAG_ANNOUNCEMENT_READ_LIST,null);
        util = new Util();
    }


    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.list_item_announcement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.txtTime.setText(Util.parseDate(listAnnouncement.get(position).getTime(),Util.DATE_FORMAT_yyyy_MM_dd_HH_mm_ss, Util.DATE_FORMAT_dd_MMM_yyyy_EEE_HH_mm_ss));
        holder.txtContext.setText(listAnnouncement.get(position).getContext());

        String jsonList = SharedPrefHelper.getInstance(context).getString(Constants.TAG_ANNOUNCEMENT_READ_LIST,null);

        if(jsonList !=null ) {
            List<Integer> list = new Gson().fromJson(jsonList, new TypeToken<List<Integer>>() {}.getType());
            if(list.contains(listAnnouncement.get(position).getId())){
               holder.imgUnread.setVisibility(View.GONE);
            } else {
                holder.imgUnread.setVisibility(View.VISIBLE);
            }
        } else {
            holder.imgUnread.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listAnnouncement.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {

        TextView txtTime;
        TextView txtContext;
        ImageView imgUnread;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txt_time);
            txtContext = itemView.findViewById(R.id.txt_context);
            imgUnread = itemView.findViewById(R.id.img_unread);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String jsonList = SharedPrefHelper.getInstance(context).getString(Constants.TAG_ANNOUNCEMENT_READ_LIST,null);

                    List<Integer> list;
                    if(jsonList !=null ) {
                        list = new Gson().fromJson(jsonList, new TypeToken<List<Integer>>() {}.getType());
                    } else {
                        list = new ArrayList<>();
                    }
                    if(!list.contains(listAnnouncement.get(getAdapterPosition()).getId())){
                        list.add(listAnnouncement.get(getAdapterPosition()).getId());
                        imgUnread.setVisibility(View.GONE);
                        String json = new Gson().toJson(list, List.class);
                        SharedPrefHelper.getInstance(context).saveString(Constants.TAG_ANNOUNCEMENT_READ_LIST, json);
                    }
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
