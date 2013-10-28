package com.example.busnew.sub;

import internet.BusInfo;
import internet.ConnectBusTask;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busnew.MainActivity;
import com.example.busnew.R;

public class FavoriteFragment extends Fragment{

	public static final String BUS_STATION_SEARCH_URL = "http://businfo.daegu.go.kr/ba/arrbus/arrbus.do?act=findByBusStopNo&bsNm=";
	Context context;
	TextView tv;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_favorite_layout, null);
		viewPagerSetting(view);
		
		tv = (TextView) view.findViewById(R.id.text_busdisplay);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		connectAndParseAndShow();
		
	}
	
	
	private void viewPagerSetting(View view){
		ViewPager pager = (ViewPager) view.findViewById(R.id.viewpager_favorite);
		pager.setAdapter(new PagerAdapter() {
			
			private int id[] = {R.drawable.station_00047,R.drawable.station_00117,R.drawable.station_00184,
					R.drawable.station_02001,R.drawable.station_00006};
			
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
	
	// 버스전광판 웹사이트 연결, 파싱, 각각의 버스정보를 배열로 리턴
	public void connectAndParseAndShow() {
		
		SharedPreferences setting = context.getSharedPreferences(MainActivity.PREF_NAME, 0);
		String station_number = setting.getString("station_number", "error");
		
		BusInfoDownloaderTask asyncBus = new BusInfoDownloaderTask(context,BUS_STATION_SEARCH_URL);

		if (asyncBus.isNetworkOn(getActivity())) {
			if (station_number != null) {
				asyncBus.execute(BUS_URL + station_number);
				ArrayList<BusInfo> busInfo = null;
				try {
					busInfo = asyncBus.get();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// ArrayList<BusInfo> busInfo 정보를 뿌림
				if (busInfo != null) {

					SpannableStringBuilder ssb = new SpannableStringBuilder();
//					Log.d("버스역",BusInfo.getStation());

//					ssb.append("버스정류소 : ").append(BusInfo.getStation()).append("\n");
					for (BusInfo bus : busInfo) {
						ssb.append(bus.getInfo());
					}
					tv.setText(ssb);
				} else {
					tv.setText("버스운행시간이 아니거나 홈페이지를 읽어오는데 문제가 발생하였습니다");
				}
			} else {
				Toast.makeText(context, "정류소를 선택해주세요", Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(context, "네트워크에 연결되어 있지 않습니다", Toast.LENGTH_SHORT).show();
		}
	}

}
