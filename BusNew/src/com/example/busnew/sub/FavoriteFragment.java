package com.example.busnew.sub;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.busnew.R;

public class FavoriteFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_favorite_layout, null);
		return view;
	}
	
	
	private void viewPagerSetting(View view, final float dpi){
		ViewPager pager = (ViewPager) view.findViewById(R.id.viewpager_favorite);
		pager.setAdapter(new PagerAdapter() {
			
			private int id[] = {R.drawable.station_00001,R.drawable.station_00002,R.drawable.station_00003,
					R.drawable.station_00005,R.drawable.station_00006};
			
			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}
			
			
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				ImageView iv = new ImageView(getActivity());
				iv.setImageResource(id[position]);
				container.addView(iv);
				return iv;
			}

			@Override
			public int getCount() {
				return id.length;
			}

			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {
				container.removeView((ImageView) object);
			}
			
			
		});
		
		pager.setOffscreenPageLimit(6);
		pager.setClipChildren(false);
		pager.setPageMargin(0);
	}
}
