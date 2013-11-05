package subfragment;

import internet.BusInfo;
import internet.ConnectTask;
import internet.ResponseTask;

import java.util.ArrayList;

import adapter.FavoritePreviewPagerAdatper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

/* 뷰페이저에 있는 미리 저장된 즐겨찾기중 하나를 선택하면
 * 그것을 전광판에 뿌림. 시작시는 가장 처음것(이후 다시 수정)을 뿌림
 */

public class FavoriteFragment extends Fragment implements ResponseTask {

	public static final String KEY_LIST = "buslist";
	public static final String KEY_ERROR = "error";
	
	Context context;
	Cursor cursor;
	String stationNum;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_favorite_layout, null);
		viewPagerSetting(view);

		Button btn = (Button) view.findViewById(R.id.btn_refrash_favorite);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPagerSetting(view);
			}
		});
		return view;
	}

	// 여기서 정류장 번호를 받는다.
	// 즐겨찾기된 그림들을 DB에서 받아온다 그 정보는 어뎁터 내에 집어넣는다.

	private void viewPagerSetting(View view) {
		ViewPager pager = (ViewPager) view.findViewById(R.id.viewpager_favorite);

		Uri uri = MyContentProvider.CONTENT_URI;
		String[] projection = { MyContentProvider.STATION_NUMBER, MyContentProvider.STATION_NAME };
		String selection = MyContentProvider.STATION_FAVORITE_STATION + "=?";
		String[] selectionArgs = { "1" };

		// 미리보기에 쓰일 쿼리를 가져온다
		cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
		pager.setAdapter(new FavoritePreviewPagerAdatper(context, cursor));

		pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// 페이지가 선택되면 선택된 번호로 커서를 이동시켜 정류소 번호를 가져온다음 showInfo로 보내준다
				Log.d("postion",String.valueOf(position));
				cursor.moveToPosition(position);
				stationNum = cursor.getString(0);
				showInfo(stationNum);
			}

			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		pager.setOffscreenPageLimit(6);
		pager.setClipChildren(false);
		pager.setPageMargin(0);
	}

	/*
	 * 받은 정류소 번호를 이용하여 
	 * ConnectTask 로 인터넷 연결을 수행하고 그 결과값을 받을 인터페이스를 등록한다
	 */
	private void showInfo(String stationNum) {

		
		ConnectTask busInfoTask = new ConnectTask(context, stationNum);
		busInfoTask.proxy = this;	// 인터페이스를 등록하여 Async의 작업이 끝나면 onTaskFinish 함수를 호출 가능케 한다

		ProgressDialog wait = ProgressDialog.show(context, null, "잠시만 기다려주세요", true);
		busInfoTask.execute();
		wait.dismiss();

	}

	/*
	 * 위에서 인터넷에서 버스정보 가져오기 작업이 끝났을때 호출되는 인터페이스, 최종결과를 뿌린다
	 */
	@Override
	public void onTaskFinish(ArrayList<BusInfo> list, String error) {

		FavoriteFragmentBusList busListFragment = new FavoriteFragmentBusList();
		Bundle initData = new Bundle();

		// 각종오류 감지 및 정보뿌림
		if (error == null && list != null) {
			// 정상일 경우
			initData.putParcelableArrayList(KEY_LIST, list);
			
		} else if (error != null) {
			// error 메세지가 있을 경우
			initData.putString(KEY_ERROR, error);
		} else {
			// 이도 저도 아닐 경우 - 정류장 번호가 없다던지.. 
			initData.putString(KEY_ERROR, "정류소가 설정되어 있지 않습니다");
		}

		busListFragment.setArguments(initData);

		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_favorite_buslist, busListFragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	@Override
	public void onStop() {
		super.onStop();
		cursor.close();
		Log.d("onStop","호출되었습니다");
	}
}
