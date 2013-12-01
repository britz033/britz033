package subfragment;

import internet.BusInfo;
import internet.ConnectTask;
import internet.ResponseTask;

import java.util.ArrayList;

import adapter.FavoriteDummyPagerAdapter;
import adapter.FavoritePreviewPagerAdatper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

/* 뷰페이저에 있는 미리 저장된 즐겨찾기중 하나를 선택하면
 * 그것을 전광판에 뿌림. 시작시는 가장 처음것(이후 다시 수정)을 뿌림
 */

public class FavoriteFragment extends Fragment implements ResponseTask,
		LoaderCallbacks<Cursor>, OnBackAction {

	public static final String KEY_BUSINFO_LIST = "buslist";
	public static final String KEY_STATION_NAME = "station";
	public static final String KEY_STATION_PASS = "pass";
	public static final String KEY_ERROR = "error";

	private Context context;
	private Cursor cursor;
	private String stationNum;
	private String stationName;
	private String stationPass; // 이 정류장을 거치는 버스들 id
	private String[] busNum;
	private Integer[] busFavorite;
	private String[] passBus;
	private int loopIndex;
	private int busCount;
	private View view;
	private FavoritePreviewPagerAdatper adapter;
	private ViewPager pager;
	private RelativeLayout loadingContainer;
	private Bundle data;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		data = new Bundle(); // 걍 데이터 재사용 번들
		view = inflater.inflate(R.layout.fragment_favorite_layout, null);
		getLoaderManager().initLoader(0, null, this);
		loadingContainer = (RelativeLayout) view
				.findViewById(R.id.layout_favorite_buslist_loadingcontainer);
		Button btn = (Button) view.findViewById(R.id.btn_testreflash);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cursor.moveToPosition(pager.getCurrentItem());
				settingInfo();
			}
		});
		loopIndex = 0;

		viewPagerSetting(view);

		return view;
	}

	// 여기서 정류장 번호를 받는다.
	// 즐겨찾기된 그림들을 DB에서 받아온다 그 정보는 어뎁터 내에 집어넣는다.
	private void viewPagerSetting(final View view) {

		float density = context.getResources().getDisplayMetrics().density;
		int dip = (int) (density * 30);

		PagerTitleStrip titlepager = (PagerTitleStrip) view
				.findViewById(R.id.pager_title_strip);
		titlepager.setTextSpacing(dip);

		adapter = new FavoritePreviewPagerAdatper(context, null);
		pager = (ViewPager) view.findViewById(R.id.viewpager_favorite);
		pager.setAdapter(adapter);

		final ViewPager dummy = (ViewPager) view
				.findViewById(R.id.viewpager_favorite_preview_dummy);
		FavoriteDummyPagerAdapter dummyAdapter = new FavoriteDummyPagerAdapter(
				adapter, context);
		dummy.setAdapter(dummyAdapter);
		dummy.setOffscreenPageLimit(7);
		dummy.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// 미리보기에 쓰일 쿼리를 가져온다 and 미리보기를 표시할 아탑터를 세팅한다
		OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

			private int currentPos;

			@Override
			public void onPageSelected(int position) {
				// 페이지가 선택되면 선택된 번호로 커서를 이동시켜 정류소 번호를 가져온다음 showInfo로 보내준다
				cursor.moveToPosition(position);
				settingInfo();
				Log.d("onPageSelected", "갱신");
				dummy.setCurrentItem(position, true);
			}

			public void onPageScrolled(int position, float offset,
					int positionOffsetPixels) {
				// dummy.scrollTo((int)offset, 0);
				// currentPos = arg0;
			}

			public void onPageScrollStateChanged(int arg0) {
				// dummy.setCurrentItem(currentPos,false);
			}
		};

		pager.setOnPageChangeListener(onPageChangeListener);
		pager.setOffscreenPageLimit(5);
		pager.setClipChildren(false);
		pager.setCurrentItem(0);

		adapter.setDummy(dummyAdapter);

		Log.d("즐겨찾기 미리보기", "페이저세팅 끝");
	}

	/*
	 * 받은 정류소 번호를 이용하여 ConnectTask 로 인터넷 연결을 수행하고 그 결과값을 받을 인터페이스를 등록한다
	 */
	private void showInfo(String stationNum) {

		ConnectTask busInfoTask = new ConnectTask(context, stationNum);
		busInfoTask.proxy = this; // 인터페이스를 등록하여 Async의 작업이 끝나면 onTaskFinish 함수를
									// 호출 가능케 한다

		busInfoTask.execute();

	}

	/*
	 * 위에서 인터넷에서 버스정보 가져오기 작업이 끝났을때 호출되는 인터페이스, 최종결과를 뿌린다 주의점 !!!! commit가 아니라
	 * commitAllowingStateLoss를 써야만 에러가 나지 않는다. 이부분은 다시 찾아볼것
	 */
	@Override
	public void onTaskFinish(ArrayList<BusInfo> list, String error) {
		loadingContainer.setVisibility(View.INVISIBLE);
		FavoriteFragmentBusList busListFragment = new FavoriteFragmentBusList();
		Bundle initData = new Bundle();

		// 각종오류 감지 및 정보뿌림
		if (error == null && list != null) {
			// 정상일 경우
			initData.putParcelableArrayList(KEY_BUSINFO_LIST, list);
			initData.putString(KEY_STATION_NAME, stationName);

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
		ft.commitAllowingStateLoss();
	}

	public void refreshPreview() {
		getLoaderManager().restartLoader(0, null, this);
	}

	// 리스트뷰를 보여주기 위한 시작, 루프의 시작
	private void settingInfo() {
		loopIndex = 0;
		loadingContainer.setVisibility(View.VISIBLE);
		if (cursor != null && cursor.getCount() > 0) {
			stationNum = cursor.getString(0);
			stationName = cursor.getString(1);
			stationPass = cursor.getString(2);
		} else {
			new AlertDialog.Builder(context).setTitle("정보 갱신에 문제가 있습니다")
					.setIcon(android.R.drawable.ic_dialog_alert).create()
					.show();
		}
		// 모든 버스 id는 10자리 쉼표포함해서 11자리, 마지막은 쉼표가 없으니 + 1, 해서 모든 버스id에서 버스정보를 추출한다
		if (stationPass.length() != 0) {
			passBus = stationPass.split(",");
			busCount = (stationPass.length() + 1) / 11;
			busNum = new String[busCount];
			busFavorite = new Integer[busCount];

			data.putString(KEY_STATION_PASS, passBus[loopIndex]);
			getLoaderManager().restartLoader(1, data, this);
		} else {
			// 이 역에는 암것도 없음.. 혹시나 에러날 가능성 좀 있으니 후에 체크
			showInfo(stationNum);
		}
	}

	// 즐겨찾기 미리보기를 할 즐겨찾기된 스테이션을 모두 검색, 이름,번호, 그리고 등록된 버스들
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
//		Log.d("즐겨찾기 미리보기", "로더생성");
		Uri uri = null;
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;

		switch (id) {
		case 0:
			uri = MyContentProvider.CONTENT_URI_STATION;
			projection = new String[] { MyContentProvider.STATION_NUMBER,
					MyContentProvider.STATION_NAME,
					MyContentProvider.STATION_PASS };
			selection = MyContentProvider.STATION_FAVORITE + "=?";
			selectionArgs = new String[] { "1" };
			break;
		case 1:
			uri = MyContentProvider.CONTENT_URI_BUS;
			projection = new String[] { MyContentProvider.BUS_NUMBER,
					MyContentProvider.BUS_FAVORITE };
			selection = MyContentProvider.BUS_ID + "="
					+ data.getString(KEY_STATION_PASS);
			break;
		}

		return new CursorLoader(context, uri, projection, selection,
				selectionArgs, null);
	}


	// 검색이 끝나면 settingInfo에서 찾은 정보를 세팅하고 그것들을 showInfo에서 보여줌
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case 0:
			Log.d("즐겨찾기 미리보기", "스왑작동");
			adapter.swapCursor(cursor);
			this.cursor = cursor;
			if (cursor.getCount() == 0)
				showInfo(null);
			else {
				cursor.moveToPosition(pager.getCurrentItem());
				settingInfo();
			}
			break;
		case 1:
			/*
			 * for (int i = 0; i < busCount; i++) {
			 * bus.putString(KEY_STATION_PASS, passBus[i]);
			 * getLoaderManager().restartLoader(1, bus, this); } 이런식으로
			 * restartLoader를 단번에 같은 id로 여러개를 호출하면 무한로딩에 들어감..;;
			 */
			// 결국.. 쿼리가 끝나면 하나 다시 restart 하고 하는 수밖에..ㅠㅠ;;
			if (loopIndex < busCount) {
				cursor.moveToFirst();
				busNum[loopIndex] = cursor.getString(0);
				Log.d("즐겨찾기", "버스id:"+passBus[loopIndex]);
				Log.d("즐겨찾기", "버스번호:"+busNum[loopIndex]);
				busFavorite[loopIndex] = cursor.getInt(1);
				data.putString(KEY_STATION_PASS, passBus[loopIndex]);
				loopIndex++;
				getLoaderManager().restartLoader(1, data, this);
			} else {
				showInfo(stationNum);
			}
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClear() {
		// TODO Auto-generated method stub

	}
}
