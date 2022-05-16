package com.hvl.dragonteam.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.hvl.dragonteam.Model.Enum.MessageTypeEnum;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Redis.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<ChatMessage> listMessages = new ArrayList<>();
    private HashMap<String, Integer> colorHashMap = new HashMap<>();
    private ArrayList<Integer> colorsArray;
    private Context context;

    private static final int LAYOUT_ME_TEXT = 1;
    private static final int LAYOUT_ME_IMAGE = 2;
    private static final int LAYOUT_OTHER_TEXT = 3;
    private static final int LAYOUT_OTHER_IMAGE = 4;
    private static final int LAYOUT_DATE = 5;

    public MessageAdapter(Context context, ArrayList<ChatMessage> listMessages) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.listMessages = listMessages;
        colorsArray = new ArrayList<Integer>(Arrays.asList(
                context.getColor(R.color.chat_sender_color1),
                context.getColor(R.color.chat_sender_color2),
                context.getColor(R.color.chat_sender_color3),
                context.getColor(R.color.chat_sender_color4),
                context.getColor(R.color.chat_sender_color5),
                context.getColor(R.color.chat_sender_color6),
                context.getColor(R.color.chat_sender_color7),
                context.getColor(R.color.chat_sender_color8),
                context.getColor(R.color.chat_sender_color9),
                context.getColor(R.color.chat_sender_color10),
                context.getColor(R.color.chat_sender_color11),
                context.getColor(R.color.chat_sender_color12),
                context.getColor(R.color.chat_sender_color13)));

    }

    @Override
    public int getItemViewType(int position) {

        if(listMessages.get(position).getMessageType().equals(MessageTypeEnum.DATE)) {
            return  LAYOUT_DATE;
        }
        else if (listMessages.get(position).getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            if(listMessages.get(position).getMessageType().equals(MessageTypeEnum.TEXT))
                return LAYOUT_ME_TEXT;
            else if(listMessages.get(position).getMessageType().equals(MessageTypeEnum.IMAGE))
                return LAYOUT_ME_IMAGE;
        } else {
            if(listMessages.get(position).getMessageType().equals(MessageTypeEnum.TEXT))
                return LAYOUT_OTHER_TEXT;
            else if(listMessages.get(position).getMessageType().equals(MessageTypeEnum.IMAGE))
                return LAYOUT_OTHER_IMAGE;
        }

        return -1;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        switch (viewType) {
            case LAYOUT_ME_TEXT:
                v = mInflater.inflate(R.layout.list_item_message_me_text, parent, false);
                return new ViewHolder(v);
            case LAYOUT_ME_IMAGE:
                v = mInflater.inflate(R.layout.list_item_message_me_image, parent, false);
                return new ViewHolder(v);
            case LAYOUT_OTHER_TEXT:
                v = mInflater.inflate(R.layout.list_item_message_other_text, parent, false);
                return new ViewHolder(v);
            case LAYOUT_OTHER_IMAGE:
                v = mInflater.inflate(R.layout.list_item_message_other_image, parent, false);
                return new ViewHolder(v);
            case LAYOUT_DATE:
                v = mInflater.inflate(R.layout.list_item_message_date, parent, false);
                return new ViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        if(holder.getItemViewType() == LAYOUT_DATE){
            holder.txtDate.setText(getFormattedTime(listMessages.get(position).getTime(),"dd.MM.yyyy"));
        }
        else {
            if(holder.getItemViewType() == LAYOUT_OTHER_TEXT || holder.getItemViewType() == LAYOUT_OTHER_IMAGE) {
                holder.txtSender.setText(listMessages.get(position).getSenderName());
                holder.txtSender.setTextColor(getColor(listMessages.get(position).getSenderId()));
            }
            if(holder.getItemViewType() == LAYOUT_ME_TEXT || holder.getItemViewType() == LAYOUT_OTHER_TEXT) {
                holder.txtMessage.setText(listMessages.get(position).getContent());
            } else if(holder.getItemViewType() == LAYOUT_ME_IMAGE || holder.getItemViewType() == LAYOUT_OTHER_IMAGE) {
                Glide.with(context)
                        .load(listMessages.get(position).getContent())
                        .placeholder(R.drawable.loading_animation)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imgMessage);
            }

            holder.txtTime.setText(getFormattedTime(listMessages.get(position).getTime(),"HH:mm"));
        }
    }

    private int getColor(String senderId){

        if(colorHashMap.get(senderId) == null){
            colorHashMap.put(senderId,colorHashMap.size() % colorsArray.size());
        }
        return  colorsArray.get(colorHashMap.get(senderId));
    }

    private String getFormattedTime(long time, String format) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        String date = DateFormat.format(format, cal).toString();
        return date;
    }


    @Override
    public int getItemCount() {
        return listMessages.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView txtMessage;
        public TextView txtSender;
        public TextView txtTime;
        public TextView txtDate;
        public ImageView imgMessage;

        public ViewHolder(View itemView) {
            super(itemView);

            txtMessage = (TextView) itemView.findViewById(R.id.textview_message);
            txtSender = (TextView) itemView.findViewById(R.id.textview_sender);
            txtTime = (TextView) itemView.findViewById(R.id.textview_time);
            txtDate = (TextView) itemView.findViewById(R.id.textview_date);
            imgMessage = (ImageView) itemView.findViewById(R.id.img_message);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
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
