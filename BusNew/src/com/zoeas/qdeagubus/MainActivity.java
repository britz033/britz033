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
import android.os.Handler;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.mocoplex.adlib.AdlibAdViewContainer;
import com.mocoplex.adlib.AdlibConfig;
import com.mocoplex.adlib.AdlibManager;
import com.mocoplex.adlib.AdlibManager.AdlibVersionCheckingListener;
import com.nbpcorp.mobilead.sdk.MobileAdListener;
import com.zoeas.util.BackPressStack;

public class MainActivity extends ActionBarActivity implements TabListener, OnCommunicationActivity, MobileAdListener {
	
	private ArrayList<Fragment> flist; 
	private AdlibManager _amanager;

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
				"버스", "sub.search.bus.SearchBusNumberFragment"), GMAP(3, "주변맵", "sub.gmap.GMapFragment");
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
		
		_amanager = new AdlibManager();
		_amanager.onCreate(this);
		
		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		viewPagerSetting();
		actionBarSetting();
		this.setAdsContainer(R.id.ads);
		initAds();
		
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
	public void onReceive(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void onResume()
	{		
		_amanager.onResume(this);
		super.onResume();
	}
	
	@Override
	protected void onPause()
	{    	
		_amanager.onPause();
		super.onPause();
	}
    
	@Override
	protected void onDestroy()
	{    	
		_amanager.onDestroy(this);
		super.onDestroy();
	}

	// xml 에 지정된 ID 값을 이용하여 BIND 하는 경우
	public void setAdsContainer(int rid)
	{
		_amanager.setAdsContainer(rid);
	}
	
	// AndroidManifest.xml에 권한과 activity를 추가하여야 합니다.     
    protected void initAds()
    {
    	// AdlibActivity 를 상속받은 액티비티이거나,
    	// 일반 Activity 에서는 AdlibManager 를 동적으로 생성한 후 아래 코드가 실행되어야 합니다. (AdlibTestProjectActivity4.java)

    	// Manifest 에서 <uses-permission android:name="android.permission.GET_TASKS" /> 부분 권한 추가를 확인해주세요.

    	// 광고 스케줄링 설정을 위해 아래 내용을 프로그램 실행시 한번만 실행합니다. (처음 실행되는 activity에서 한번만 호출해주세요.)    	
    	// 광고 subview 의 패키지 경로를 설정합니다. (실제로 작성된 패키지 경로로 수정해주세요.)

    	// 쓰지 않을 광고플랫폼은 삭제해주세요.
        AdlibConfig.getInstance().bindPlatform("ADAM","adlib.zoeas.SubAdlibAdViewAdam");
        AdlibConfig.getInstance().bindPlatform("ADMOB","adlib.zoeas.SubAdlibAdViewAdmob");
        AdlibConfig.getInstance().bindPlatform("NAVER","adlib.zoeas.SubAdlibAdViewNaverAdPost");
        
        
//        AdlibConfig.getInstance().bindPlatform("CAULY","test.adlib.project.ads.SubAdlibAdViewCauly");
//        AdlibConfig.getInstance().bindPlatform("TAD","test.adlib.project.ads.SubAdlibAdViewTAD");
//        AdlibConfig.getInstance().bindPlatform("SHALLWEAD","test.adlib.project.ads.SubAdlibAdViewShallWeAd");
//        AdlibConfig.getInstance().bindPlatform("INMOBI","test.adlib.project.ads.SubAdlibAdViewInmobi");
//        AdlibConfig.getInstance().bindPlatform("MMEDIA","test.adlib.project.ads.SubAdlibAdViewMMedia");
//        AdlibConfig.getInstance().bindPlatform("MOBCLIX","test.adlib.project.ads.SubAdlibAdViewMobclix");
//        AdlibConfig.getInstance().bindPlatform("ADMOBECPM","test.adlib.project.ads.SubAdlibAdViewAdmobECPM");
//        AdlibConfig.getInstance().bindPlatform("UPLUSAD","test.adlib.project.ads.SubAdlibAdViewUPlusAD");
//        AdlibConfig.getInstance().bindPlatform("MEZZO","test.adlib.project.ads.SubAdlibAdViewMezzo");
//        AdlibConfig.getInstance().bindPlatform("AMAZON","test.adlib.project.ads.SubAdlibAdViewAmazon");
//        AdlibConfig.getInstance().bindPlatform("ADHUB","test.adlib.project.ads.SubAdlibAdViewAdHub");
        // 쓰지 않을 플랫폼은 JAR 파일 및 test.adlib.project.ads 경로에서 삭제하면 최종 바이너리 크기를 줄일 수 있습니다.        
        
        // SMART* dialog 노출 시점 선택시 / setAdlibKey 키가 호출되는 activity 가 시작 activity 이며 해당 activity가 종료되면 app 종료로 인식합니다.
        // adlibr.com 에서 발급받은 api 키를 입력합니다.
        // https://sec.adlibr.com/admin/dashboard.jsp
        // ADLIB - API - KEY 설정
        AdlibConfig.getInstance().setAdlibKey("52c680f2e4b0f34da50563de"); 
    }
    
 // 동적으로 Container 를 생성하여, 그 객체를 통하여 BIND 하는 경우
 	public void bindAdsContainer(AdlibAdViewContainer a)
 	{
 		_amanager.bindAdsContainer(a);		
 	}
 	
 	// 전면광고 호출
 	public void loadInterstitialAd()
 	{
 		_amanager.loadInterstitialAd(this);
 	}
 			
 	// 전면광고 호출 (광고 수신 성공, 실패 여부를 받고 싶을 때 handler 이용)
 	public void loadInterstitialAd(Handler h)
 	{
 		_amanager.loadInterstitialAd(this, h);
 	}
 	
 	// Full Size 전면광고 호출
 	public void loadFullInterstitialAd()
 	{
 		_amanager.loadFullInterstitialAd(this);
 	}
 		
 	// Full Size 전면광고 호출 (광고 수신 성공, 실패 여부를 받고 싶을 때 handler 이용)
 	public void loadFullInterstitialAd(Handler h)
 	{
 		_amanager.loadFullInterstitialAd(this, h);
 	}
 	
 	// 팝 배너 프레임 컬러 설
 	public void setAdlibPopFrameColor(int color)
 	{
 		_amanager.setAdlibPopFrameColor(color);
 	}
 	
 	// 팝 배너 버튼 컬러 설정 (AdlibPop.BTN_WHITE, AdlibPop.BTN_BLACK)
 	public void setAdlibPopCloseButtonStyle(int style)
 	{
 		_amanager.setAdlibPopCloseButtonStyle(style);
 	}
 	
 	// 팝 배너 in, out 애니메이션 설정(AdlibPop.ANIMATION_SLIDE, AdlibPop.ANIMATION_NONE)
 	public void setAdlibPopAnimationType(int inAnim, int outAnim)
 	{
 		_amanager.setAdlibPopAnimationType(inAnim, outAnim);
 	}
 	
 	// 팝 배너 보이기 (align   : AdlibPop.ALIGN_LEFT, AdlibPop.ALIGN_TOP, AdlibPop.ALIGN_RIGHT, AdlibPop.ALIGN_BOTTOM)
 	//             (padding : dp값)
 	public void showAdlibPop(int align, int padding)
 	{
 		_amanager.showAdlibPop(this, align, padding);
 	}
 	
 	// 팝 배너 숨기기
 	public void hideAdlibPop()
 	{
 		_amanager.hideAdlibPop();
 	}
 	
 	public void setVersionCheckingListner(AdlibVersionCheckingListener l)
 	{
 		_amanager.setVersionCheckingListner(l);		
 	}
 	
 	// AD 영역을 동적으로 삭제할때 호출하는 메소드
 	public void destroyAdsContainer()
 	{
 		_amanager.destroyAdsContainer();
 	}
 	// 애드립 연동에 필요한 구현부 끝    

}
