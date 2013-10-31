package subfragment;

import internet.BusInfo;
import internet.ConnectTask;
import internet.ResponseTask;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.R;

public class FavoriteFragment extends Fragment implements ResponseTask {

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
		
		
		Button btn = (Button) view.findViewById(R.id.btn_refrash_favorite);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showInfo();
			}
		});
		tv = (TextView) view.findViewById(R.id.text_busdisplay);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		showInfo();
	}
	
	private void showInfo(){
		SharedPreferences sp = context.getSharedPreferences(
				MainActivity.PREF_NAME, 0);
		String stationNum = sp.getString("station_number", "xxx");
		if (stationNum.equals("xxx")) {
			Toast.makeText(context, "정류소 번호를 가져오는데 실패했습니다", 0).show();
		}
		
		Log.d("버스정류소 번호",String.valueOf(stationNum));

		ConnectTask busInfoTask = new ConnectTask(context,
				stationNum);
		busInfoTask.proxy = this;
		
		ProgressDialog wait = ProgressDialog.show(context, null, "잠시만 기다려주세요", true);
		busInfoTask.execute();
		wait.dismiss();

		
		
	}

	private void viewPagerSetting(View view) {
		ViewPager pager = (ViewPager) view
				.findViewById(R.id.viewpager_favorite);
		pager.setAdapter(new PagerAdapter() {

			private int id[] = { R.drawable.station_00047,
					R.drawable.station_00117, R.drawable.station_00184,
					R.drawable.station_02001, R.drawable.station_00006 };

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

	// 인터넷에서 버스정보 가져오기 작업이 끝났을대 호출되는 인터페이스
	@Override
	public void onTaskFinish(ArrayList<BusInfo> list, String error) {
		SpannableStringBuilder ssb = new SpannableStringBuilder();

		if (error == null && list != null) {
			ssb.append("버스정류소 : ").append(list.get(0).getStation())
					.append("\n");
			for (BusInfo bus : list) {
				ssb.append(bus.getSpannableStringBusInfo());
			}
			tv.setText(ssb);
		} else if(error != null){
			tv.setText(error);
		} else {
			tv.setText("정류장이나 버스가 설정되어 있지 않습니다");
		}
	}

}
