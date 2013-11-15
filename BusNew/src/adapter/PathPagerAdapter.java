package adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class PathPagerAdapter<T extends Fragment> extends FragmentStatePagerAdapter{
	
	public static final String PATH_STATION_NAME = "STATION";
	
	private Class<T> fragmentclass;
	private ArrayList<String> station;

	public PathPagerAdapter(FragmentManager fm, ArrayList<String> path, Class<T> fragmentclass) {
		super(fm);
		this.fragmentclass = fragmentclass;
		this.station = path;
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
		args.putString(PATH_STATION_NAME, station.get(position));
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public float getPageWidth(int position) {
		return 0.20f;
	}

	@Override
	public int getCount() {
		return station.size();
	}

	

}
