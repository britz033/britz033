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
import android.widget.Toast;

import com.example.busnew.sub.GMapFragment;
import com.example.busnew.sub.OnBusStationInfoListener;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements TabListener,
		OnBusStationInfoListener {

	public interface CallFragmentMethod{
		public void OnCalled();
	}
	
	private ArrayList<Fragment> flist; 			// 액티비티가 관리하는 애들
	public static final String PREF_NAME = "save_station_num";	// SharedPreferance 키값

	public enum MyTabs {
		FAVORITE(0, "즐겨찾기", "com.example.busnew.sub.FavoriteFragment"), 
		STATION_LISTVIEW(1, "정류소", "com.example.busnew.sub.StationSearchFragment"), 
		BUS_LISTVIEW(2, "버스", "com.example.busnew.sub.BusNumberSearchFragment"), 
		GMAP(3, "주변맵", "com.example.busnew.sub.GMapFragment"),
		DUMMY(4, "설정", "com.example.busnew.sub.BusNumberSearchFragment");
		private final String name;
		private final String fragmentName;
		private final int num;

		MyTabs(int num, String name, String fragmentName) {
			this.num = num;
			this.name = name;
			this.fragmentName = fragmentName;
		}

		int getValue() {
			return num;
		}

		String getName() {
			return name;
		}
		
		String getFragmentName(){
			return fragmentName;
		}
	}

	private ViewPager vp;
	private String stationNumber;
	private String stationName;
	private LatLng latlng;

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
		Fragment addFragment = null;
		
		MyTabs[] mytabs = MyTabs.values(); 
		try {
			for(MyTabs mytab : mytabs){
				addFragment = (Fragment) Class.forName(mytab.getFragmentName()).newInstance();
				flist.add(addFragment);
			}
		} catch (Exception e) {
			Log.d("페이지뷰 동적 클래스 생성", "실패했습니다");
			e.printStackTrace();
		}

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
				if(MyTabs.GMAP.getValue() == position){
					CallFragmentMethod call = (CallFragmentMethod)flist.get(position);
					call.OnCalled();
				}
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

		SharedPreferences setting = getSharedPreferences(PREF_NAME, 0);
		SharedPreferences.Editor editor = setting.edit();

		editor.putString("station_number", station_number);
		editor.putString("station_name", station_name);
		editor.putString("station_latitude", String.valueOf(latLng.latitude));
		editor.putString("station_longitude", String.valueOf(latLng.longitude));
		editor.commit();

		this.stationNumber = station_number;
		this.stationName = station_name;
		this.latlng = latLng;
		
		Toast.makeText(this, latLng.toString() +" 저장되었습니다", 0).show();
	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		vp.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

	}

	@Override
	public void OnBusStationInfo(String station_number, String station_name) {
		// TODO Auto-generated method stub

	}

	//StationSearchFragment 의 버튼
	// gmap 탭 호출
	public void btnOnclick(View view) {
		int index = MyTabs.GMAP.getValue();		// gmap탭의 번호를 가져온다
//		Toast.makeText(this, latlng.toString(), 0).show();
		vp.setCurrentItem(index, true);
		((GMapFragment) flist.get(index)).setGMap(stationNumber, stationName,
				latlng);
		vp.requestTransparentRegion(vp);
	}

	

}

class MainViewPager extends ViewPager {

	public MainViewPager(Context context) {
		super(context);
	}
	public MainViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View view, boolean checkV, int arg2, int arg3,
			int arg4) {
		if(view != this && view instanceof ViewPager){
			return true;
		}
		
		return super.canScroll(view, checkV, arg2, arg3, arg4);
	}
}
