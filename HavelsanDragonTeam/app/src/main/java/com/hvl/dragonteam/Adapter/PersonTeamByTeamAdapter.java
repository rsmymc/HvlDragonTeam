package com.hvl.dragonteam.Adapter;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.Enum.SideEnum;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.StringMatcher;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonTeamByTeamAdapter extends ArrayAdapter<PersonTeamView> implements SectionIndexer {

    private Context context;
    private String mSections;
    private OnPersonTeamChangeListener listener;
    private static final int CALL_PERMISSIONS_REQUEST = 102;

    public PersonTeamByTeamAdapter(Context context, ArrayList<PersonTeamView> listPersonTeam, String mSections, OnPersonTeamChangeListener onPersonTeamChangeListener) {
        super(context, 0, listPersonTeam);
        this.mSections = mSections;
        this.context = context;
        this.listener = onPersonTeamChangeListener;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        final PersonTeamView personTeamView = getItem(position);

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_person_team, parent, false);
        }

        TextView txtName = itemView.findViewById(R.id.txt_person_name);
        TextView txtHeight = itemView.findViewById(R.id.txt_height);
        TextView txtWeight = itemView.findViewById(R.id.txt_weight);
        TextView txtSide = itemView.findViewById(R.id.txt_side);
        ImageView imgCall = itemView.findViewById(R.id.img_call);
        ImageView imgMore = itemView.findViewById(R.id.img_more);
        CircleImageView imgProfile = itemView.findViewById(R.id.img_profile);

        txtName.setText(personTeamView.getPersonName());
        txtHeight.setText(personTeamView.getHeight() + " cm");
        txtWeight.setText(personTeamView.getWeight() + " kg");
        txtSide.setText(SideEnum.toSideEnum(personTeamView.getSide()).name().substring(0, 1));
        Glide.with(context)
                .load(personTeamView.getProfilePictureUrl())
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
                onCall(personTeamView.getPhone());
            }
        });

        if (Constants.personTeamView.getRole() == RoleEnum.ADMIN.getValue() && !personTeamView.getPersonId().equals(Constants.personTeamView.getPersonId())) {
            imgMore.setVisibility(View.VISIBLE);
        } else {
            imgMore.setVisibility(View.GONE);
        }

        imgMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                popup.getMenuInflater().inflate(R.menu.menu_team_person_popup, popup.getMenu());

                if (personTeamView.getPersonId().equals(Constants.personTeamView.getPersonId())) {
                    popup.getMenu().getItem(R.id.action_remove_from_team).setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        switch (id) {
                            case (R.id.action_set_role): {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                final View view = inflater.inflate(R.layout.dialog_update_role, null);
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setView(view);
                                builder.setCancelable(false);
                                final RadioGroup radioGroupRole = (RadioGroup) view.findViewById(R.id.radio_group_role);

                                ((RadioButton) radioGroupRole.getChildAt(personTeamView.getRole())).setChecked(true);

                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        int radioButtonID = radioGroupRole.getCheckedRadioButtonId();
                                        View radioButton = radioGroupRole.findViewById(radioButtonID);
                                        int id = radioGroupRole.indexOfChild(radioButton);
                                        savePersonTeam(personTeamView, id);
                                    }
                                });
                                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.dismiss();
                                    }
                                });
                                builder.show();
                                break;
                            }
                            case (R.id.action_remove_from_team): {
                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                deletePersonTeam(personTeamView);
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage(context.getString(R.string.warning_delete_person_team))
                                        .setPositiveButton(context.getString(R.string.ok), dialogClickListener)
                                        .setNegativeButton(context.getString(R.string.cancel), dialogClickListener).show();
                                break;
                            }
                        }
                        return true;
                    }
                });
                popup.show();//showing popup menu
            }
        });

        return itemView;
    }

    private void deletePersonTeam(PersonTeamView personTeamView) {
        PersonTeamService personTeamService = new PersonTeamService();
        try {
            PersonTeam personTeam = new PersonTeam();
            personTeam.setTeamId(personTeamView.getTeamId());
            personTeam.setPersonId(personTeamView.getPersonId());
            personTeamService.deletePersonTeam(context,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Util.toastInfo(context, context.getString(R.string.info_team_updated));
                            listener.personTeamChange();
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

    private void savePersonTeam(PersonTeamView personTeamView, int role) {
        PersonTeamService personTeamService = new PersonTeamService();
        PersonTeam personTeam = new PersonTeam(personTeamView.getPersonId(), personTeamView.getTeamId(), role);

        try {
            personTeamService.savePersonTeam(context,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Util.toastInfo(context, context.getString(R.string.info_team_updated));
                            listener.personTeamChange();
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

    public interface OnPersonTeamChangeListener {
        void personTeamChange();
    }
}
