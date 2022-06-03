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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
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
import com.hvl.dragonteam.Utilities.StringMatcher;
import com.hvl.dragonteam.Utilities.Util;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonTeamByTeamAdapter extends ArrayAdapter<PersonTeamView> implements SectionIndexer {

    private Context context;
    private String mSections;
    private static final int CALL_PERMISSIONS_REQUEST = 102;

    public PersonTeamByTeamAdapter(Context context, ArrayList<PersonTeamView> listPersonTeam, String mSections) {
        super(context, 0, listPersonTeam);
        this.mSections = mSections;
        this.context = context;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        final PersonTeamView item = getItem(position);

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_person_team, parent, false);
        }

        TextView txtName = itemView.findViewById(R.id.txt_person_name);
        TextView txtHeight = itemView.findViewById(R.id.txt_height);
        TextView txtWeight = itemView.findViewById(R.id.txt_weight);
        TextView txtSide = itemView.findViewById(R.id.txt_side);
        ImageView imgCall = itemView.findViewById(R.id.img_call);
        CircleImageView imgProfile = itemView.findViewById(R.id.img_profile);

        txtName.setText(item.getPersonName());
        txtHeight.setText(item.getHeight() + " cm");
        txtWeight.setText(item.getWeight() + " kg");
        txtSide.setText(SideEnum.toSideEnum(item.getSide()).name().substring(0, 1));
        Glide.with(context)
                .load(item.getProfilePictureUrl())
                .apply(new RequestOptions()
                        .centerInside()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .skipMemoryCache(false)
                        .placeholder(R.drawable.user_placeholder)
                        .error(R.drawable.user_placeholder))
                .into(imgProfile);

        imgCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCall(item.getPhone());
            }
        });

        return itemView;
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
    public Object[] getSections() {
        String[] sections = new String[mSections.length()];
        for (int i = 0; i < mSections.length(); i++)
            sections[i] = String.valueOf(mSections.charAt(i));
        return sections;
    }

    @Override
    public int getPositionForSection(int section) {
        // If there is no item for current section, previous section will be selected
        for (int i = section; i >= 0; i--) {
            for (int j = 0; j < getCount(); j++) {
                if (i == 0) {
                    // For numeric section
                    for (int k = 0; k <= 9; k++) {
                        if (StringMatcher.match(String.valueOf(getItem(j).getPersonName().charAt(0)), String.valueOf(k)))
                            return j;
                    }
                } else {
                    if (StringMatcher.match(String.valueOf(getItem(j).getPersonName().charAt(0)), String.valueOf(mSections.charAt(i))))
                        return j;
                }
            }
        }
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }
}
