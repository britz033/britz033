package com.example.busnew;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.example.busnew.sub.FavoriteFragment;
import com.example.busnew.sub.GMapFragment;
import com.example.busnew.sub.StationSearchFragment;
import com.example.busnew.sub.StationSearchFragment.OnBusStationInfoListener;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements TabListener,OnBusStationInfoListener{

	private ArrayList<Fragment> flist;
	public static final String PREF_NAME = "save_station_num";

	public enum MyTabs {
		FAVORITE(0, "즐겨찾기"), STATION_LISTVIEW(1, "정류소"), GMAP(2, "맵");
		private final String name;
		private final int num;

		MyTabs(int num, String name) {
			this.num = num;
			this.name = name;
		}

		int getValue() {
			return num;
		}

		String getName() {
			return name;
		}
	}

	private ViewPager vp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		viewPagerSetting();
		actionBarSetting();

	}

	/*
	 * asset에서 db 가져와서 기기에 디렉토리 만들고, 거기에 db를 카피 contentprovider가 먼저 호출되면서 덩달아
	 * dbhelper도 호출 덕분에 이 코드는 망함. 그래서 dbhelper 쪽으로 이사감
	 */

	private void viewPagerSetting() {
		vp = (MainViewPager) findViewById(R.id.viewpager_main);
		FragmentManager fm = getSupportFragmentManager();
		flist = new ArrayList<Fragment>();

		flist.add(new FavoriteFragment());
		flist.add(new StationSearchFragment());
		flist.add(new GMapFragment());

		vp.setAdapter(new FragmentPagerAdapter(fm) {
			@Override
			public int getCount() {
				return flist.size();
			}

			@Override
			public Fragment getItem(int position) {
				return flist.get(position);
			}
		});

		vp.requestTransparentRegion(vp);

		vp.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				getSupportActionBar().setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private void actionBarSetting() {
		ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		MyTabs[] mytabs = MyTabs.values();
		for (MyTabs mytab : mytabs) {
			Tab tab = actionbar.newTab().setText(mytab.getName())
					.setTabListener(this);
			actionbar.addTab(tab);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void OnBusStationInfo(String station_number, String station_name,
			LatLng latLng) {
		vp.setCurrentItem(MyTabs.GMAP.getValue(), true);
		Log.d("onlocationinfo", station_number + "," + station_name + ","
				+ latLng.toString());
		((GMapFragment) flist.get(2)).setGMap(station_number, station_name,
				latLng);
		vp.requestTransparentRegion(vp);
		
		SharedPreferences setting = getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor editor = setting.edit();
		
		editor.putString("station_number", station_number);
		editor.commit();
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		MyTabs[] mytabs = MyTabs.values();
		switch (mytabs[tab.getPosition()]) {
		case FAVORITE:
			vp.setCurrentItem(MyTabs.FAVORITE.getValue(), true);
			break;
		case STATION_LISTVIEW:
			vp.setCurrentItem(MyTabs.STATION_LISTVIEW.getValue(), true);
			break;
		case GMAP:
			vp.setCurrentItem(MyTabs.GMAP.getValue(), true);
			break;
		default:
			Log.d("onTabSelected", "error");
		}
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

	}

}

class MainViewPager extends ViewPager{

	public MainViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected boolean canScroll(View view, boolean checkV, int dx,
			int x, int y) {
		return true;
	}
}
