package org.sais.rasoid;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PagerAdapter extends FragmentPagerAdapter {
	
	private List<Fragment> fragments;
	
	public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int location) {
		return fragments.get(location);
	}

	@Override
	public int getCount() {
		return 3;
	}

}
