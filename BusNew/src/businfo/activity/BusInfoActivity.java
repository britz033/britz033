package businfo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.ActionMap;
import util.ActionMap.OnActionInfoWindowClickListener;
import util.BackPressStack;
import util.LoopQuery;
import util.Switch;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

/*
 * 일반 버스 리스트에서 검색말고
 * 전광판에서 가져오는 것은 그냥 텍스트를 매치 시키는 것이기때문에 에러날 소지가 큼
 * 쿼리시 에러가 나면 try문으로 캐취해서 경고문 띄움
 */

public class BusInfoActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, OnPageChangeListener, OnClickListener,
		OnActionInfoWindowClickListener<Integer> {

	public static final String KEY_BUS_ID = "BUSID";
	public static final String KEY_BUS_NAME = "BUSNAME";
	public static final String KEY_CURRENT_STATION_NAME = "STATION";
	public static final String KEY_PATH_STATION = "PATH";
	public static final LatLng DEAGU = new LatLng(35.8719607, 128.5910759);
	private static final int LOADER_ID_INIT = 0;

	private static final int FORWARD = 10;
	private static final int BACKWARD = 20;
	private ViewPager pathPager;
	private ArrayList<String> pathDirection;
	private ArrayList<String> pathForward;
	private ArrayList<String> pathBackward;
	private int[] stationIdDirection;
	private int[] stationIdForward;
	private int[] stationIdBackward;
	private HashMap<Integer, String> passBusHash;
	private LoopQuery<String> loopQueryStation;
	private Cursor mcursor;
	private int busDirection;
	private int currentDirection;
	private int saveIndex;
	private int prePosition;
	private ActionMap<Integer> actionMapDirection;
	private ActionMap<Integer> actionMapForward;
	private ActionMap<Integer> actionMapBackward;
	private String currentStationName;
	private boolean userControlAllowed;
	private Switch pathSwitchWidget;
	private ImageButton favoriteAddBtn;
	private Drawable[] drawables;
	private int busFavorite;
	private int busId;
	private BackPressStack backPressStack;
	private FrameLayout stationListViewContainer;
	private ListView stationListView;
	private boolean searchAgain;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_businfo);
		actionMapForward = new ActionMap<Integer>(this);
		actionMapBackward = new ActionMap<Integer>(this);
		passBusHash = new HashMap<Integer, String>();
		pathForward = new ArrayList<String>();
		pathBackward = new ArrayList<String>();
		backPressStack = new BackPressStack();
		saveIndex = 0;
		prePosition = 0;
		busDirection = currentDirection = FORWARD;
		userControlAllowed = false;
		searchAgain = false;
		drawables = new Drawable[] { getResources().getDrawable(R.drawable.path), getResources().getDrawable(R.drawable.path_selected),
				getResources().getDrawable(R.drawable.path_end), getResources().getDrawable(R.drawable.path_end_selected),
				getResources().getDrawable(R.drawable.path_start), getResources().getDrawable(R.drawable.path_start_selected) };

		// 각정보를 넣을 곳을 기본 세팅하고 쿼리를 스타트
		pathPager = (ViewPager) findViewById(R.id.viewpager_activity_businfo_path);

		String busId = getIntent().getExtras().getString(KEY_BUS_ID);
		String busName = getIntent().getExtras().getString(KEY_BUS_NAME);
		currentStationName = getIntent().getExtras().getString(KEY_CURRENT_STATION_NAME);
		TextView textBusNumber = (TextView) findViewById(R.id.text_activity_businfo_number);
		TextView textStationName = (TextView) findViewById(R.id.text_activity_businfo_stationname);
		TextView textOption = (TextView) findViewById(R.id.text_activity_businfo_option);
		Button searchBtn = (Button) findViewById(R.id.btn_activity_businfo_pathsearch);
		favoriteAddBtn = (ImageButton) findViewById(R.id.btn_activity_businfo_addfavorite);
		stationListViewContainer = (FrameLayout) findViewById(R.id.layout_activity_businfo_path_container);
		stationListView = new ListView(this);
		searchBtn.setOnClickListener(this);
		favoriteAddBtn.setOnClickListener(this);

		String busOption = "";
		Pattern pattern = Pattern.compile("^(.+) \\((.+)\\)$");
		Matcher matcher = pattern.matcher(busName);
		if (matcher.find()) {
			busName = matcher.group(1);
			busOption = matcher.group(2);
		}

		textBusNumber.setText(busName);
		textOption.setText(busOption);

		if (currentStationName != null)
			textStationName.setText(currentStationName);

		pathSwitchWidget = (Switch) findViewById(R.id.switch_path);
		pathSwitchWidget.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (userControlAllowed) {
					if (searchAgain) {
						backPressStack.pop();
						pathListClose();
						searchAgain = false;
					}

					if (isChecked) {
						currentDirection = FORWARD;
					} else {
						currentDirection = BACKWARD;
					}

					actionMapDirection.clearMap();
					prePosition = 0;
					settingMapPath();
					onPageSelected(0);
				}
			}
		});

		// 정방향 역방향의 모든 버스정류장 이름을 추출
		getSupportLoaderManager().initLoader(LOADER_ID_INIT, getIntent().getExtras(), this);

		Log.d("버스인포", "onCreate 호출");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (actionMapForward.checkGoogleService())
			mapSetIfNeeded();
		else {

		}

	}

	// 액티비티니까 null 걱정없이 바로 xml에서 map 가져옴
	private void mapSetIfNeeded() {
		if (!actionMapForward.isMap() || !actionMapBackward.isMap()) {
			actionMapForward.setMap(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.busmap)).getMap());
			actionMapBackward.setMap(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.busmap)).getMap());
		}
		actionMapForward.moveMap(DEAGU); // 처음로딩때 대구시
		actionMapBackward.moveMap(DEAGU); // 처음로딩때 대구시
	}

	// 모든 버스 정류장 이름이 추출된 후 호출됨, 여기서 스위치에 따라 정방향,역방향을 구분, 처음 시작시 한번만 호출
	private void initBusInfo() {
		Log.d("버스인포", "busInfo 초기화불려짐 이것은 한번만 불려져야함");
		try {
			mcursor.moveToFirst();

			String busInterval = mcursor.getString(1);
			String busForward = mcursor.getString(2);
			String busBackward = mcursor.getString(3);
			busFavorite = mcursor.getInt(4);
			busId = mcursor.getInt(0);

			Log.d("즐겨찾기여부", "" + busFavorite);
			if (busFavorite == 0) {
				favoriteAddBtn.setImageResource(R.drawable.btn_station_list_item_off_selector);
			} else {
				favoriteAddBtn.setImageResource(R.drawable.btn_station_list_item_on_selector);
			}

			Pattern pattern = Pattern.compile("([^;]+);");
			Matcher matcherForward = pattern.matcher(busForward);
			Matcher matcherBackward = pattern.matcher(busBackward);

			while (true) {
				if (!matcherForward.find())
					break;
				pathForward.add(matcherForward.group(1));
			}
			while (true) {
				if (!matcherBackward.find())
					break;
				pathBackward.add(matcherBackward.group(1));
			}

			stationIdForward = new int[pathForward.size()];
			stationIdBackward = new int[pathBackward.size()];

			TextView textBusInterval = (TextView) findViewById(R.id.text_activity_businfo_current);
			textBusInterval.setText(busInterval);
		} catch (Exception e) {
			e.printStackTrace();
			new AlertDialog.Builder(this).setMessage("현재 이 버스는 업데이트되어 있지 않습니다").show().setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
		}
		settingSwitch(FORWARD);
		String[] startPath = pathDirection.toArray(new String[pathDirection.size()]);
		loopQueryStation = new LoopQuery<String>(getSupportLoaderManager(), startPath, this);
		loopQueryStation.start();
	}


	public void settingSwitch(int pathSwitch) {
		PathPagerAdapter<BusInfoPathItemFragment> adapter = null;
		switch (pathSwitch) {
		case FORWARD:
			adapter = new PathPagerAdapter<BusInfoPathItemFragment>(getSupportFragmentManager(), pathForward, BusInfoPathItemFragment.class);
			pathDirection = pathForward;
			actionMapDirection = actionMapForward;
			stationIdDirection = stationIdForward;
			break;

		case BACKWARD:
			adapter = new PathPagerAdapter<BusInfoPathItemFragment>(getSupportFragmentManager(), pathBackward, BusInfoPathItemFragment.class);
			pathDirection = pathBackward;
			actionMapDirection = actionMapBackward;
			stationIdDirection = stationIdBackward;
			break;
		}
		pathPager.setAdapter(adapter);
		pathPager.setOnPageChangeListener(this);
	}

	// 이 쿼리문 자체는 정방향, 역방향의 영향을 받지 않는 순수한 쿼리
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {

		Uri uri = null;
		String[] projection = null;
		String selection = null;

		// 0 번은 스위치에 관계없이 무조건 불려져야하는 버스자체의 정보
		// 1번은 들어오는 정류장이름으로 좌표 추출, 2번은 즐겨찾기추가 업데이트
		switch (id) {
		case LOADER_ID_INIT:
			Log.d("버스인포", "로더 초기화 생성");
			uri = MyContentProvider.CONTENT_URI_BUS;
			projection = new String[] { "_id", "bus_interval", "bus_forward", "bus_backward", "bus_favorite" };
			selection = "bus_id='" + data.getString(KEY_BUS_ID) + "'";
			break;
		case LoopQuery.DEFAULT_LOOP_QUERY_ID:
			uri = MyContentProvider.CONTENT_URI_STATION;
			projection = new String[] { "_id", "station_name", "station_latitude", "station_longitude", "station_pass" };
			selection = "station_name='" + loopQueryStation.getBundleData() + "'";
			break;
		}

		return new CursorLoader(this, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch (loader.getId()) {
		case LOADER_ID_INIT: // 무조건 한번만 불려짐, 즐겨찾기 추가등으로 업데이트 되었을시 불려지는걸 방지 단, 즐겨찾기정보만은 업데이트
			if (!userControlAllowed) {
				mcursor = cursor;
				initBusInfo();
			} else {
				cursor.moveToFirst();
				busFavorite = cursor.getInt(4);
			}
			break;

		// 역의 좌표,경로(두방향모두),경유버스 저장
		case LoopQuery.DEFAULT_LOOP_QUERY_ID:
			try {
				cursor.moveToNext();
				Log.d("버스인포",cursor.getString(1));
				LatLng latLng = new LatLng(cursor.getDouble(2), cursor.getDouble(3));
				actionMapDirection.addLinePoint(latLng);
				passBusHash.put(cursor.getInt(0), cursor.getString(4)); // 나중에 꺼낼 pass 저장, id별로 저장
				stationIdDirection[loopQueryStation.getCount() - 1] = cursor.getInt(0); // 각 마커마다 순서대로 id를 전달하기 위해 저장중

				// 일단 양쪽 모두 돌리는데 정류장이 발견되면 현재 돌고 있는 방향을 저장
				if (cursor.getString(1).equals(currentStationName)) {
					Log.d("버스인포액티비티", "정류장 존재");
					actionMapDirection.aniMap(latLng);
					saveIndex = loopQueryStation.getCount() - 1;
					busDirection = currentDirection;
				}
				if (!loopQueryStation.isEnd()) {
					loopQueryStation.restart();
				} else if (currentDirection != BACKWARD && pathBackward.size() != 0) {
					Log.d("버스인포액티비티", "역방향으로 체인지");
					currentDirection = BACKWARD;
					settingSwitch(BACKWARD);
					String[] newPath = pathDirection.toArray(new String[pathDirection.size()]);
					loopQueryStation.start(newPath);
				} else {
					finishLoading();
				}
			} catch (Exception e) {
				e.printStackTrace();
				new AlertDialog.Builder(this).setTitle("경로데이터가 부족합니다").create().show();
			}
			break;
		}
	}

	// 모든 로딩이 끝나고 저장된 마커정보를 바탕으로 마커를 찍고 경로를 그림
	private void finishLoading() {
		currentDirection = busDirection;
		settingMapPath();
		actionMapDirection.addMarkerAndShow(saveIndex, pathDirection.get(saveIndex), stationIdDirection[saveIndex]);

		if (saveIndex != 0) {
			prePosition = saveIndex;
			pathPager.setCurrentItem(saveIndex);
		} else {
			actionMapDirection.setLineBound();
		}

		if (pathBackward.size() != 0) {
			pathSwitchWidget.setStartWork();
			if (busDirection == FORWARD) {
				pathSwitchWidget.setChecked(true);
			} else if (busDirection == BACKWARD) {
				pathSwitchWidget.setChecked(false);
			}
		} else {
			pathSwitchWidget.setOnePath();
		}

		userControlAllowed = true;

	}

	private void settingMapPath() {
		settingSwitch(currentDirection);
		actionMapDirection.setOnActionInfoWindowClickListener(this);
		actionMapDirection.drawLine();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d("로더리셋", "불려짐");
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {

		final int START = 4;
		final int START_SELECTED = 5;
		final int END = 2;
		final int END_SELECTED = 3;
		final int NOMAL = 0;
		final int NOMAL_SELECTED = 1;
		int positionEnd = pathDirection.size() - 1;

		// 주의점 : 종점에 갔을때 다시한번 밀면 position이 보이지 않는 놈의 번호로 바뀌는 경우가 있음. 이때 인덱스 오버해서
		// 에러가 남 그것을 방지
		position = (position >= pathDirection.size() ? position - 1 : position);
		if (userControlAllowed) {
			actionMapDirection.removeMarker();
			actionMapDirection.aniMap(position);
			actionMapDirection.addMarkerAndShow(position, pathDirection.get(position), stationIdDirection[position]);
		}

		// 첫로딩시 가져오면 에러가 남.. instantiateItem 쪽 개념때문인듯. 이거 어떻게든 해야되는데..
		if (position != prePosition) {
			ImageView preImg = (ImageView) ((BusInfoPathItemFragment) pathPager.getAdapter().instantiateItem(pathPager, prePosition + 2)).getView()
					.findViewById(R.id.img_path);
			ImageView curImg = (ImageView) ((BusInfoPathItemFragment) pathPager.getAdapter().instantiateItem(pathPager, position + 2)).getView()
					.findViewById(R.id.img_path);

			if (position == 0) {
				curImg.setImageDrawable(drawables[START_SELECTED]);
				preImg.setImageDrawable(drawables[NOMAL]);
			} else if (position == positionEnd) {
				curImg.setImageDrawable(drawables[END_SELECTED]);
				preImg.setImageDrawable(drawables[NOMAL]);
			} else {
				curImg.setImageDrawable(drawables[NOMAL_SELECTED]);
				if (prePosition == 0) {
					preImg.setImageDrawable(drawables[START]);
				} else if (prePosition == positionEnd) {
					preImg.setImageDrawable(drawables[END]);
				} else
					preImg.setImageDrawable(drawables[NOMAL]);
			}
		}

		prePosition = position;
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub

	}

	// 검색창을 보여주거나 즐겨찾기를 추가
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_activity_businfo_addfavorite:
			Log.d("즐겨찾기추가", "됨");
			Uri uri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI_BUS, busId);
			ContentValues value = new ContentValues();
			if (busFavorite == 0) {
				value.put("bus_favorite", 1);
				((ImageButton) v).setImageResource(R.drawable.btn_station_list_item_on_selector);
			} else {
				value.put("bus_favorite", 0);
				((ImageButton) v).setImageResource(R.drawable.btn_station_list_item_off_selector);
			}
			getContentResolver().update(uri, value, null, null);
			break;
		case R.id.btn_activity_businfo_pathsearch:
			LayoutInflater inflater = LayoutInflater.from(this);
			LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.edittext_businfo_search, null);
			final AlertDialog searchDialog = new AlertDialog.Builder(this).setView(ll).create();
			searchDialog.show();

			EditText et = (EditText) ll.findViewById(R.id.edittext_activity_businfo_search);

			et.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

						String s = ((EditText) v).getText().toString();
						ArrayList<Integer> saveSearchPoint = new ArrayList<Integer>();
						ArrayList<String> searchedStationList = new ArrayList<String>();

						for (int i = 0; i < pathDirection.size(); i++) {
							if (pathDirection.get(i).contains(s)) {
								saveSearchPoint.add(i);
								searchedStationList.add(pathDirection.get(i));
							}
						}

						if (searchedStationList.size() != 0) {
							actionMapDirection.clearMap();
							actionMapDirection.drawLine();

							for (int i = 0; i < saveSearchPoint.size(); i++) {
								int point = saveSearchPoint.get(i);
								actionMapDirection.addMarkerAndShow(point, pathDirection.get(point), saveSearchPoint.get(i));
							}

							BusInfoStationSearchListAdapter searchAdapter = new BusInfoStationSearchListAdapter(searchedStationList, getBaseContext());

							if (!searchAgain) {
								searchAgain = true;
								backPressStack.push();
								pathListOpen();
							}

							stationListView.setAdapter(searchAdapter);

							actionMapDirection.setLineBound();
						} else {
							Toast.makeText(getBaseContext(), "버스 경로상에 <" + s + "> 단어가 포함된 \n정류소가 없습니다", 0).show();
						}
						searchDialog.dismiss();
					}
					return false;
				}

			});
			break;
		}
	}

	private void pathListOpen() {
		Animator downAni = ObjectAnimator.ofFloat(pathPager, "translationY", 0, 200);
		Animator alphaAni = ObjectAnimator.ofFloat(pathPager, "alpha", 1, 0);

		AnimatorSet aniSet = new AnimatorSet();
		aniSet.playTogether(downAni, alphaAni);
		aniSet.setDuration(1000);
		aniSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				stationListViewContainer.addView(stationListView);
				pathPager.setVisibility(View.GONE);
			}
		});
		aniSet.start();
	}

	private void pathListClose() {
		stationListViewContainer.removeViewAt(1);
		pathPager.setVisibility(View.VISIBLE);

		Animator downAni = ObjectAnimator.ofFloat(pathPager, "translationY", 200, 0);
		Animator alphaAni = ObjectAnimator.ofFloat(pathPager, "alpha", 0, 1);

		AnimatorSet aniSet = new AnimatorSet();
		aniSet.playTogether(downAni, alphaAni);
		aniSet.setDuration(1000);
		aniSet.start();
	}

	@Override
	public void onInfoWindowClick(Marker marker, Integer markerAdditionalinfo) {
		Toast.makeText(this, passBusHash.get(markerAdditionalinfo), 0).show();
	}

	// 경유버스 목록 닫기, 검색된 버스 목록 닫고 경로 목록 열기, 각각은 겹치지 않게 경유버스가 안닫혔으면 검색 버스가 안열리는
	// 식으로.. 이거 잘못하면 에러 박살남
	@Override
	public void onBackPressed() {
		switch (backPressStack.pop()) {
		case BackPressStack.FINISH:
		case BackPressStack.FINISH_READY:
			finish();
			break;
		case BackPressStack.DO_SOMETHING:
			pathListClose();
			searchAgain = false;
			break;
		}
	}

}
