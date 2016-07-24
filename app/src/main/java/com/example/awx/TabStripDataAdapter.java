package com.example.awx;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by john on 2016/7/16.
 */
public class TabStripDataAdapter extends FragmentPagerAdapter {
    private final String[] TITLES = {"新闻", "设置", "日程", "群发内容", "应答词典", "帮助", "关于"};

    public TabStripDataAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new SetFragment();
            case 2:
                return new ScheFragmen();
            case 3:
                return new MsgFragmen();
            default:
                return new MainFragment();
        }
    }
}
