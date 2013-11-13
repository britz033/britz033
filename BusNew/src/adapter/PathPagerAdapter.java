package adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PathPagerAdapter<T extends Fragment> extends FragmentStatePagerAdapter{
	
	private Class<T> fragmentclass;
	private String[] station;

	public PathPagerAdapter(FragmentManager fm, String[] station, Class<T> fragmentclass) {
		super(fm);
		this.fragmentclass = fragmentclass;
		this.station = station;
	}

	@Override
	public T getItem(int position) {
		
		T fragment = null;
		try {
			fragment = fragmentclass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		Bundle args = new Bundle();
		args.putString("station", station[position]);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public float getPageWidth(int position) {
		return 0.25f;
	}

	@Override
	public int getCount() {
		return station.length;
	}

	

}
