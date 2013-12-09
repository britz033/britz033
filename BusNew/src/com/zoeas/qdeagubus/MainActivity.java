package com.zoeas.qdeagubus;

import java.util.ArrayList;

import sub.favorite.FavoriteFragment;
import util.BackPressStack;
import adapter.OnCommunicationActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.util.Log;
import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends ActionBarActivity implements TabListener,OnCommunicationActivity {

	private ArrayList<Fragment> flist; // 액티비티가 관리하는 애들

	public interface CallFragmentMethod {
		public void OnCalled();
	}

	public static final String PREF_NAME = "save_station_num"; // SharedPreferance
																// 키값
	public static final BackPressStack backAction = new BackPressStack();
	private OnBackAction subFragment;

	public interface OnBackAction {
		public void onBackPressed();
		public void onClear();
	}

	public enum MyTabs {
		FAVORITE(0, "즐겨찾기", "sub.favorite.FavoriteFragment"), STATION_LISTVIEW(1, "정류소",
				"sub.search.station.SearchStationFragment"), BUS_LISTVIEW(2, "버스", "sub.search.bus.SearchBusNumberFragment"), GMAP(
				3, "주변맵", "sub.gmap.GMapFragment"), DUMMY(4, "설정", "subfragment.SettingFragment"), 
				TEST(5, "test",	"subfragment.Test"), TEST2(6, "test2",	"subfragment.Test2") ;
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

		String getFragmentName() {
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

	// commitAllowingStateLoss 를 사용해야 에러가 안난다. 이부분 주의
	// http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void viewPagerSetting() {
		vp = (MainViewPager) findViewById(R.id.viewpager_main);
		FragmentManager fm = getSupportFragmentManager();
		flist = new ArrayList<Fragment>();
		Fragment addFragment = null;

		MyTabs[] mytabs = MyTabs.values();
		try {
			for (MyTabs mytab : mytabs) {
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
				backAction.init();
				subFragment.onClear();
				subFragment = (OnBackAction) flist.get(position);

				getSupportActionBar().setSelectedNavigationItem(position);
				if (MyTabs.GMAP.getValue() == position) {
					CallFragmentMethod call = (CallFragmentMethod) flist.get(position);
					call.OnCalled();
				}

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(vp.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		subFragment = (OnBackAction) flist.get(0);
	}

	private void actionBarSetting() {
		ActionBar actionbar = getSupportActionBar();
		actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		MyTabs[] mytabs = MyTabs.values();
		for (MyTabs mytab : mytabs) {
			Tab tab = actionbar.newTab().setText(mytab.getName()).setTabListener(this);
			actionbar.addTab(tab);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// 일단 보류. 현재 위젯에서 이 정보를 사용해서 스테이션 결정중
//	@Override
//	public void OnSaveBusStationInfo(String station_number, String station_name, LatLng latLng) {
//
//		SharedPreferences setting = getSharedPreferences(PREF_NAME, 0);
//		SharedPreferences.Editor editor = setting.edit();
//
//		editor.putString("station_number", station_number);
//		editor.putString("station_name", station_name);
//		editor.putString("station_longitude", String.valueOf(latLng.longitude));
//		editor.putString("station_latitude", String.valueOf(latLng.latitude));
//		editor.commit();
//
//		this.stationNumber = station_number;
//		this.stationName = station_name;
//		this.latlng = latLng;
//
//		Toast.makeText(this, latLng.toString() + " 저장되었습니다", 0).show();
//	}

	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		vp.setCurrentItem(tab.getPosition(), false);
	}

	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

	}


	@Override
	public void onBackPressed() {
		switch (backAction.pop()) {
		case BackPressStack.FINISH:
			super.onBackPressed();
			break;
		case BackPressStack.FINISH_READY:
			Toast.makeText(this, "뒤로가기를 한번 더 누르시면 종료됩니다", Toast.LENGTH_SHORT).show();
			break;
		case BackPressStack.DO_SOMETHING:
			subFragment.onBackPressed();
			break;
		}
	}

	// 정류장 검색에서 리스트뷰 즐겨찾기 추가시 불러짐
	@Override
	public void OnFavoriteRefresh() {
		((FavoriteFragment) flist.get(MyTabs.FAVORITE.getValue())).refreshPreview();
	}

	@Override
	public void OnTabMove(MyTabs myTab) {
		int index = myTab.getValue();
		vp.setCurrentItem(index, true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 1001) {
			Uri selectImage = data.getData();
			String[] projection = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectImage, projection, null, null, null);
			cursor.moveToFirst();

			int index = cursor.getColumnIndex(projection[0]);
			String picturePath = cursor.getString(index);
			cursor.close();

			ImageView iv = new ImageView(this);
			iv.setImageBitmap(BitmapFactory.decodeFile(picturePath));

			new AlertDialog.Builder(this).setView(iv).create().show();
		}
	}

}
