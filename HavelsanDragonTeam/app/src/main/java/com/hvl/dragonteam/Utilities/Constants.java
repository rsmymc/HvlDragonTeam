
package com.hvl.dragonteam.Utilities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hvl.dragonteam.Fragment.FragmentAnnouncement;
import com.hvl.dragonteam.Fragment.FragmentChat;
import com.hvl.dragonteam.Fragment.FragmentProfile;
import com.hvl.dragonteam.Fragment.FragmentStats;
import com.hvl.dragonteam.Fragment.FragmentTeamPager;
import com.hvl.dragonteam.Fragment.FragmentTeamPersonList;
import com.hvl.dragonteam.Fragment.FragmentTrainingPager;
import com.hvl.dragonteam.Model.Person;
import com.hvl.dragonteam.Model.PersonTeamView;

import java.util.ArrayList;
import java.util.List;


public class Constants {

    public static String frgTagTrainingPager = "fragment_training_pager";
    public static String frgTagTeamPager = "fragment_team_pager";
    public static String frgTagChat = "fragment_chat";
    public static String frgTagProfile = "fragment_profile";
    public static String frgTagAnnouncement = "fragment_announcement";
    public static String frgTagEquipment = "fragment_equipment";
    public static Fragment frgTrainingPager;
    public static Fragment frgTeamPager;
    public static Fragment frgChat;
    public static Fragment frgProfile;
    public static Fragment frgAnnouncement;
    public static Fragment frgEquipment;
    public static List<String> mainFragmentTags;
    public static List<Fragment> mainFragments;
    public static BottomNavigationView bottomBar;

    public static String REDIS_CHAT_PREFIX = "dragonchatchannel_";
    public static String REDIS_CHAT_LAST_SEEN_PREFIX = "lastseenchannel_";
    public static String imageFilePath;

    public static final String UPLOAD_IMAGE_TYPE_PROFILE = "profile_images";
    public static final String UPLOAD_IMAGE_TYPE_CHAT = "chat_images";
    public static final String UPLOAD_IMAGE_TYPE_LOGO = "logo_images";
    public static final String TAG_ANNOUNCEMENT_READ_LIST ="ANNOUNCEMENT_READ_LIST";
    public static final String TAG_LAST_SELECTED_TEAM="LAST_SELECTED_TEAM";

    public static Person person;
    public static PersonTeamView personTeamView;

    public static String REMOTE_DIALOG_PREFIX ="remoteDialog";
    public static Bundle bundle;

    public static void setInitialValues() {
        mainFragmentTags = new ArrayList<>();
        mainFragmentTags.add(frgTagTrainingPager);
        mainFragmentTags.add(frgTagTeamPager);
        mainFragmentTags.add(frgTagAnnouncement);
        mainFragmentTags.add(frgTagChat);
        mainFragmentTags.add(frgTagProfile);

        frgTrainingPager = new FragmentTrainingPager();
        frgTeamPager = new FragmentTeamPager();
        frgAnnouncement = new FragmentAnnouncement();
        frgChat = new FragmentChat();
        frgProfile = new FragmentProfile();

        mainFragments = new ArrayList<>();
        mainFragments.add(frgTrainingPager);
        mainFragments.add(frgTeamPager);
        mainFragments.add(frgAnnouncement);
        mainFragments.add(frgChat);
        mainFragments.add(frgProfile);
    }
}

