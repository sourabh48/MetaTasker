package com.example.soura.metatasker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by soura on 22-12-2017.
 */

class SectionPagerAdapter extends FragmentPagerAdapter
{

    public SectionPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                FragmentProjects fragmentTasks=new FragmentProjects();
                return fragmentTasks;
            case 1:
                FragmentNotifications fragmentNotifications=new FragmentNotifications();
                return fragmentNotifications;

            default:
                return null;
        }

    }

    @Override
    public int getCount()
    {
        return 2;
    }

    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Projects";
            case 1:
                return "Conversations";

            default:
                return null;
        }
    }
}
