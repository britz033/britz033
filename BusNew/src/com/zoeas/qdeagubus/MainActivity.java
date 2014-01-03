package com.zoeas.qdeagubus;

import java.util.ArrayList;

import sub.favorite.FavoriteFragment;
import sub.search.station.SearchStationFragment;
import adapter.OnCommunicationActivity;
import adapter.OnCommunicationReceive;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zoeas.util.BackPressStack;

public class MainActivity extends ActionBarActivity implements TabListener, OnCommunicationActivity {
	
	private ArrayList<Fragment> flist; 

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
		FAVORITE(0, "즐겨찾기", "sub.favorite.FavoriteFragment"), STATION_LISTVIEW(1, "정류소", "sub.search.station.SearchStationFragment"), BUS_LISTVIEW(2,
				"버스", "sub.search.bus.SearchBusNumberFragment"), GMAP(3, "주변맵", "sub.gmap.GMapFragment"), DUMMY(4, "설정",
				"subfragment.SettingFragment"), TEST(5, "test", "subfragment.Test"), TEST2(6, "test2", "subfragment.Test2");
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		viewPagerSetting();
		actionBarSetting();
	}

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
		actionbar.setDisplayShowTitleEnabled(false);
		actionbar.setDisplayShowHomeEnabled(false);
		
		MyTabs[] mytabs = MyTabs.values();
		for (MyTabs mytab : mytabs) {
			Tab tab = actionbar.newTab().setText(mytab.getName()).setTabListener(this);
			actionbar.addTab(tab);
		}
	}

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

	@Override
	public void OnFavoriteRefresh() {
		((FavoriteFragment) flist.get(MyTabs.FAVORITE.getValue())).refrashPreview();
		((SearchStationFragment) flist.get(MyTabs.STATION_LISTVIEW.getValue())).refrashFavorite();
	}

	@Override
	public void OnTabMove(MyTabs myTab, Bundle data) {
		int index = myTab.getValue();
		vp.setCurrentItem(index, false);
		if (data != null) {
			OnCommunicationReceive send = (OnCommunicationReceive) flist.get(index);
			send.OnReceive(data);
		}
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
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
