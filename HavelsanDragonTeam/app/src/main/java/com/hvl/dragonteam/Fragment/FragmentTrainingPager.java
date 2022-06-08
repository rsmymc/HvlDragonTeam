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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.hvl.dragonteam.Activity.ActivityHome;
import com.hvl.dragonteam.Activity.ActivityTeam;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;
import com.hvl.dragonteam.Utilities.SharedPrefHelper;

public class FragmentTrainingPager extends Fragment {

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

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mPager.setCurrentItem(bundle.getInt("CURRENT_ITEM",0));
        }

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
                return new FragmentTrainingNext();
            } else if (num == 1) {
                return new FragmentTrainingPast();
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
                        title = getString(R.string.next_training);
                    break;
                case 1:
                    if (isAdded())
                        title = getString(R.string.past_training);
                    break;
            }
            return title;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_announcement, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case (R.id.action_announcement): {
                FragmentAnnouncement fragmentAnnouncement = new FragmentAnnouncement();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragmentAnnouncement, "fragmentAnnouncement").addToBackStack("fragmentAnnouncement")
                        .commit();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle(getString(R.string.training));

        super.onResume();

    }
}
