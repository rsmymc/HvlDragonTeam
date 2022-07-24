package com.hvl.dragonteam.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Activity.ActivityTeam;
import com.hvl.dragonteam.Adapter.PersonTeamByTeamAdapter;
import com.hvl.dragonteam.DataService.PersonTeamService;
import com.hvl.dragonteam.DataService.TeamService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Enum.RoleEnum;
import com.hvl.dragonteam.Model.PersonTeam;
import com.hvl.dragonteam.Model.PersonTeamView;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FragmentTeamPersonList extends Fragment {

    private View view;
    private Activity activity;
    private FragmentManager fragmentManager;
    private FragmentActivity context;

    private PersonTeamByTeamAdapter personTeamAdapter;
    private SwipeMenuListView listView;
    private ArrayList<PersonTeamView> personTeamList = new ArrayList<>();
    private ArrayList<String> listLetters = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        view = inflater.inflate(R.layout.fragment_team_person_list, container, false);
        activity = getActivity();
        context = (FragmentActivity) getContext();
        fragmentManager = context.getSupportFragmentManager();
        setHasOptionsMenu(true);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.team));
        toolbar.setSubtitle("");
        listView = view.findViewById(R.id.listView_team);
        listView.setFastScrollEnabled(true);

        if (Constants.personTeamView.getRole() == RoleEnum.ADMIN.getValue()) {
            SwipeMenuCreator creator = new SwipeMenuCreator() {

                @Override
                public void create(SwipeMenu menu) {
                    // create "role" item
                    SwipeMenuItem roleItem = new SwipeMenuItem(context);
                    roleItem.setBackground(new ColorDrawable(getResources().getColor(R.color.infoColor)));
                    roleItem.setWidth(dipToPixels(context, 90));
                    roleItem.setIcon(R.drawable.role);
                    menu.addMenuItem(roleItem);
                    // create "delete" item
                    SwipeMenuItem deleteItem = new SwipeMenuItem(context);
                    deleteItem.setBackground(new ColorDrawable(getResources().getColor(R.color.red)));
                    deleteItem.setWidth(dipToPixels(context, 90));
                    deleteItem.setIcon(R.drawable.delete);
                    menu.addMenuItem(deleteItem);
                }
            };
            listView.setMenuCreator(creator);
            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                    switch (index) {
                        case 0: {
                            LayoutInflater inflater = requireActivity().getLayoutInflater();
                            final View view = inflater.inflate(R.layout.dialog_update_role, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setView(view);
                            builder.setCancelable(false);
                            final RadioGroup radioGroupRole = (RadioGroup) view.findViewById(R.id.radio_group_role);

                            ((RadioButton) radioGroupRole.getChildAt(personTeamList.get(position).getRole())).setChecked(true);

                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    int radioButtonID = radioGroupRole.getCheckedRadioButtonId();
                                    View radioButton = radioGroupRole.findViewById(radioButtonID);
                                    int id = radioGroupRole.indexOfChild(radioButton);
                                    savePersonTeam(personTeamList.get(position), id);
                                }
                            });
                            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                        break;
                        case 1: {
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            deletePersonTeam(personTeamList.get(position));
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage(getString(R.string.warning_delete_person_team))
                                    .setPositiveButton(getString(R.string.ok), dialogClickListener)
                                    .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();
                        }
                        break;
                    }
                    return false;
                }
            });
            listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        }

        EditText myFilter = (EditText) view.findViewById(R.id.txt_filter);
        myFilter.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    personTeamAdapter.getFilter().filter(s.toString());
                } catch (Exception ex) {
                    Log.i("Error", ex.toString());
                }
            }
        });

        getTeamPersons();

        return view;
    }

    public static int dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public void getTeamPersons() {
        view.findViewById(R.id.resultPanel).setVisibility(View.GONE);
        PersonTeamService personTeamService = new PersonTeamService();
        Team team = new Team();
        team.setId(Constants.personTeamView.getTeamId());
        try {
            personTeamService.getPersonListByTeam(context, team,
                    new VolleyCallback() {
                        @Override
                        public void onSuccessList(JSONArray result) {
                            List<PersonTeamView> list = new Gson().fromJson(result.toString(), new TypeToken<List<PersonTeamView>>() {
                            }.getType());

                            personTeamList.clear();
                            personTeamList.addAll(list);

                            for (PersonTeamView personTeamView : personTeamList) {
                                if (!listLetters.contains(String.valueOf(personTeamView.getPersonName().charAt(0)).toUpperCase())) {
                                    listLetters.add(String.valueOf(personTeamView.getPersonName().charAt(0)).toUpperCase());
                                }
                            }
                            Collections.sort(listLetters, new Comparator<String>() {
                                @Override
                                public int compare(String s1, String s2) {
                                    Collator collator = Collator.getInstance(new Locale("tr", "TR"));
                                    return collator.compare(s1, s2);
                                }
                            });

                            String mSections = "";
                            for (String letter : listLetters) {
                                mSections += letter;
                            }

                            personTeamAdapter = new PersonTeamByTeamAdapter(context, personTeamList, mSections);
                            listView.setAdapter(personTeamAdapter);

                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            if (list.size() == 0)
                                view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);

                        }

                        @Override
                        public void onSuccess(JSONObject result) {
                        }

                        @Override
                        public void onError(String result) {
                            Activity activity = getActivity();
                            if (activity != null && isAdded())
                                Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Activity activity = getActivity();
            if (activity != null && isAdded())
                Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            view.findViewById(R.id.resultPanel).setVisibility(View.VISIBLE);
        }
    }

    private void deletePersonTeam(PersonTeamView personTeamView) {
        view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
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
                            if(personTeamView.getPersonId().equals(Constants.personTeamView.getPersonId())){
                                Util.toastInfo(context, context.getString(R.string.info_team_leaved));
                                SharedPrefHelper.getInstance(getContext()).saveString(Constants.TAG_LAST_SELECTED_TEAM, null);
                                Intent intent = new Intent(getContext(), ActivityTeam.class);
                                startActivity(intent);
                                getActivity().finish();
                                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            } else {
                                Util.toastInfo(context, getString(R.string.info_team_updated));
                                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                                getTeamPersons();
                            }
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private void savePersonTeam(PersonTeamView personTeamView, int role) {
        view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        PersonTeamService personTeamService = new PersonTeamService();
        PersonTeam personTeam = new PersonTeam(personTeamView.getPersonId(), personTeamView.getTeamId(), role);

        try {
            personTeamService.savePersonTeam(context,
                    personTeam,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(JSONObject result) {
                            Util.toastInfo(context, getString(R.string.info_team_updated));
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            Constants.personTeamView.setRole(role);
                            getTeamPersons();
                        }

                        @Override
                        public void onError(String result) {
                            Util.toastError(context);
                            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        }

                        @Override
                        public void onSuccessList(JSONArray result) {
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(context);
            view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_team_person_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_leave_team): {

                List<PersonTeamView> filteredPersonList = personTeamList.stream().filter(article -> article.getRole() == RoleEnum.ADMIN.getValue()).collect(Collectors.toList());

                if (Constants.personTeamView.getRole() == RoleEnum.ADMIN.getValue() && filteredPersonList.size() == 1) {
                    Util.toastWarning(context, R.string.warning_admin);
                } else {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    deletePersonTeam(Constants.personTeamView);
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setMessage(context.getString(R.string.warning_leave_team).replace("XXX", Constants.personTeamView.getTeamName()))
                            .setPositiveButton(context.getString(R.string.leave), dialogClickListener)
                            .setNegativeButton(context.getString(R.string.cancel), dialogClickListener).show();
                    break;
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }
}

