package com.keerthan.locationalarm2.TabResponders;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.keerthan.locationalarm2.Alarms.AlarmTab;
import com.keerthan.locationalarm2.MapHandlers.MapTabFragment;
import com.keerthan.locationalarm2.R;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles = new String[] {"Map", "Alarm"};
    private Context context;

    public static MapTabFragment mapFrag;
    public static AlarmTab alarmFrag;

    public MyFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
        mapFrag = new MapTabFragment();
        alarmFrag = new AlarmTab();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("MyFragmentPagerAdapter", "getItem("+position+")");
        if(position==0)
            return mapFrag;
        else if(position==1)
            return alarmFrag;

        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Log.d("MyFragmentPagerAdapter", "getPageTitle("+position+")");
        return tabTitles[position];
    }
}

