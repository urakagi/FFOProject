package org.sais.rasoid;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

public class CardViewActivity extends FragmentActivity {

	PagerAdapter mPagerAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.pager);
		FragmentManager fm = getSupportFragmentManager();
		List<Fragment> frags = new ArrayList<Fragment>();
		ArrayList<Integer> ind = Tools.getIndices();
		int cardnum = getIntent().getIntExtra("cardnum", ind.get(0));
		int index = ind.indexOf(cardnum);
		Bundle arg = new Bundle();
		arg.putInt("cardnum", cardnum);
		frags.add(Fragment.instantiate(this, CardViewFragment.class.getName(), arg));
		
		arg = new Bundle();
		index++;
		arg.putInt("cardnum", ind.get(index));
		frags.add(Fragment.instantiate(this, CardViewFragment.class.getName(), arg));
		
		arg = new Bundle();
		index++;
		arg.putInt("cardnum", ind.get(index));
		frags.add(Fragment.instantiate(this, CardViewFragment.class.getName(), arg));
		
		ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
		this.mPagerAdapter = new PagerAdapter(fm, frags);
        pager.setAdapter(mPagerAdapter);
	}
	
}
