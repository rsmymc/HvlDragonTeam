package com.hvl.dragonteam.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hvl.dragonteam.Activity.ActivityTeam;
import com.hvl.dragonteam.DataService.AnnouncementService;
import com.hvl.dragonteam.Interface.VolleyCallback;
import com.hvl.dragonteam.Model.Announcement;
import com.hvl.dragonteam.Model.Team;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentTeamPager extends Fragment {

    private ViewPagerAdapter mAdapter;
    private ViewPager mPager;
    private TabLayout tabLayout;
    private static final int NUM_ITEMS = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_pager, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new ViewPagerAdapter(getChildFragmentManager(), NUM_ITEMS);
        mPager = (ViewPager) getView().findViewById(R.id.pager);
        tabLayout = (TabLayout) getView().findViewById(R.id.tab_layout);
        mPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(mPager);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(mPager);
            }
        });

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        int mNumOfTabs;

        public ViewPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int num) {
            if (num == 0) {
                return new FragmentTeamSettings();
            } else if (num == 1) {
                return new FragmentTeamPersonList();
            }
            return null;
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = " ";
            switch (position) {
                case 0:
                    if (isAdded())
                        title = getString(R.string.team_info);
                    break;
                case 1:
                    if (isAdded())
                        title = getString(R.string.team_members);
                    break;
            }
            return title;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (Fragment fragment : getChildFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
