package subfragment;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.ActionMap;
import adapter.PathPagerAdapter;
import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

/*
 * 일반 버스 리스트에서 검색말고
 * 전광판에서 가져오는 것은 그냥 텍스트를 매치 시키는 것이기때문에 에러날 소지가 큼
 * 쿼리시 에러가 나면 try문으로 캐취해서 경고문 띄움
 */

public class BusInfoActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, OnPageChangeListener {

	public static final String KEY_BUS_INFO = "BUSNUM";
	public static final String KEY_CURRENT_STATION_NAME = "STATION";
	public static final String KEY_PATH_STATION = "PATH";
	public static final LatLng DEAGU = new LatLng(35.8719607, 128.5910759);
	private static final int FORWARD = 10;
	private static final int BACKWARD = 20;
	private ViewPager pathPager;
	private ArrayList<String> pathDirection;
	private ArrayList<String> pathForward;
	private ArrayList<String> pathBackward;
	private Cursor mcursor;
	private int loopIndex;
	private int loopErrorCheck;
	private ActionMap actionMapDirection;
	private ActionMap actionMapForward;
	private ActionMap actionMapBackward;
	private String currentStationName;
	private boolean directionFlag;
	private TextView textDirection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_businfo);
		actionMapForward = new ActionMap(this, getResources().getDisplayMetrics().density);
		actionMapBackward = new ActionMap(this, getResources().getDisplayMetrics().density);
		pathForward = new ArrayList<String>();
		pathBackward = new ArrayList<String>();
		loopIndex = 0;
		loopErrorCheck = 0;
		directionFlag = false;

		// 각정보를 넣을 곳을 기본 세팅하고 쿼리를 스타트
		pathPager = (ViewPager) findViewById(R.id.viewpager_activity_businfo_path);

		String busNum = getIntent().getExtras().getString(KEY_BUS_INFO);
		currentStationName = getIntent().getExtras().getString(KEY_CURRENT_STATION_NAME);
		TextView textBusNumber = (TextView) findViewById(R.id.text_activity_businfo_number);
		TextView textStationName = (TextView) findViewById(R.id.text_activity_businfo_stationname);
		textDirection = (TextView) findViewById(R.id.text_activity_businfo_direction);
		textBusNumber.setText(busNum);
		if (currentStationName != null)
			textStationName.setText(currentStationName);
		if (currentStationName.equals(SearchBusNumberFragment.DEFAULT_STATION))
			directionFlag = true;

		// 정방향 역방향의 모든 버스정류장 이름을 추출
		getSupportLoaderManager().initLoader(0, getIntent().getExtras(), this);

		Log.d("버스인포", "onCreate 호출");
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapSetIfNeeded();

	}

	private void mapSetIfNeeded() {
		if (!actionMapForward.isMap() || !actionMapBackward.isMap()) {
			actionMapForward.setMap(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.busmap))
					.getMap());
			actionMapBackward.setMap(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.busmap))
					.getMap());
		}
		actionMapForward.moveMap(DEAGU); // 처음로딩때 대구시
		actionMapBackward.moveMap(DEAGU); // 처음로딩때 대구시
	}

	// 모든 버스 정류장 이름이 추출된 후 호출됨, 여기서 스위치에 따라 정방향,역방향을 구분, 처음 시작시 한번만 호출
	private void initBusInfo() {
		try {
			mcursor.moveToFirst();

			String busInterval = mcursor.getString(1);
			String busForward = mcursor.getString(2);
			String busBackward = mcursor.getString(3);
			int busFavorite = mcursor.getInt(4);

			Pattern pattern = Pattern.compile("([^,]+),");
			Matcher matcherForward = pattern.matcher(busForward);
			Matcher matcherBackward = pattern.matcher(busBackward);

			String station = null;

			while (true) {
				if (!matcherForward.find())
					break;
				// 간혹 어디어디 () 이런 식의 골때리는 역명이 있는데 괄호 제거안하면 에러
				station = matcherForward.group(1).replace(" ()", "");
				pathForward.add(station);
			}
			while (true) {
				if (!matcherBackward.find())
					break;
				// 간혹 어디어디 () 이런 식의 골때리는 역명이 있는데 괄호 제거안하면 에러
				station = matcherBackward.group(1).replace(" ()", "");
				pathBackward.add(station);
			}

			TextView textBusInterval = (TextView) findViewById(R.id.text_activity_businfo_current);
			textBusInterval.setText(busInterval);
		} catch (Exception e) {
			new AlertDialog.Builder(this).setMessage("현재 이 버스는 업데이트되어 있지 않습니다").show();
			// 전광판에서 가져온 버스번호가 제대로 매치가 안되거나 없을때 발생
			e.printStackTrace();
		}
		settingSwitch(FORWARD);
		loopQuery();
	}

	public void settingSwitch(int pathSwitch) {
		PathPagerAdapter<BusInfoPathItemFragment> adapter = null;
		switch (pathSwitch) {
		case FORWARD:
			adapter = new PathPagerAdapter<BusInfoPathItemFragment>(getSupportFragmentManager(), pathForward,
					BusInfoPathItemFragment.class);
			pathDirection = pathForward;
			actionMapDirection = actionMapForward;
			break;

		case BACKWARD:
			adapter = new PathPagerAdapter<BusInfoPathItemFragment>(getSupportFragmentManager(), pathBackward,
					BusInfoPathItemFragment.class);
			pathDirection = pathBackward;
			actionMapDirection = actionMapBackward;
			break;
		}

		pathPager.setAdapter(adapter);
		pathPager.setOnPageChangeListener(this);
	}

	// 받은 버스번호를 토대로 경로를 뽑은데서 다시 각 경로마다의 버스정류장을 반복쿼리함, 반복 index는 loopIndex
	public void loopQuery() {
		loopErrorCheck++;
		if (loopErrorCheck < 300) {
			try {
				Log.d("루프 스테이션 이름", pathDirection.get(loopIndex));
				Bundle data = new Bundle();
				data.putString(KEY_PATH_STATION, pathDirection.get(loopIndex));
				loopIndex++;
				getSupportLoaderManager().restartLoader(1, data, this);
			} catch (Exception e) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("이 버스는 경로를 불러올 수 없습니다 : " + currentStationName);
				builder.create().show();
				// 버스 경로에서 불러온 이름으로 버스정류장을 다시 검색하였으나 존재치 않음
				e.printStackTrace();
			}
		} else
			Log.d("버스인포액티비티", "무한루프에러:" + currentStationName);
	}

	// 이 쿼리문 자체는 정방향, 역방향의 영향을 받지 않는 순수한 쿼리
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {

		Uri uri = null;
		String[] projection = null;
		String selection = null;

		// 0 번은 스위치에 관계없이 무조건 불려져야하는 버스자체의 정보
		// 1번은 들어오는 정류장이름으로 좌표 추출
		if (id == 0) {
			Log.d("로더 init", data.getString(KEY_BUS_INFO));
			uri = MyContentProvider.CONTENT_URI_BUS;
			projection = new String[] { "_id", "bus_interval", "bus_forward", "bus_backward", "bus_favorite" };
			selection = "bus_number='" + data.getString(KEY_BUS_INFO) + "'";
		} else if (id == 1) {
			uri = MyContentProvider.CONTENT_URI_STATION;
			projection = new String[] { "_id", "station_name", "station_latitude", "station_longitude" };
			selection = "station_name='" + data.getString(KEY_PATH_STATION) + "'";
		}

		return new CursorLoader(this, uri, projection, selection, null, null);
	}

	// init ID 0 , restart ID 1, 쿼리 받은게 끝난후 받은 데이터를 사용하기 시작, initBusInfo와
	// loopQuery 둘다 에러가능성 높음
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case 0:
			mcursor = cursor;
			initBusInfo();
			break;
		case 1:
			try {
				// 처음이것이 불려질시 앞에 ininbusInfo에서 loopQuery를 호출했으므로 loopIndex 는 1인
				// 상태
				cursor.moveToNext();
				LatLng latLng = new LatLng(cursor.getDouble(2), cursor.getDouble(3));
				actionMapDirection.addLinePoint(latLng);
				// actionMap.addMarker(cursor.getString(1), latLng,
				// R.drawable.station_point);

				if (cursor.getString(1).equals(currentStationName)) {
					Log.d("버스인포액티비티", "방향이 맞음");
					actionMapDirection.moveMap(latLng);
					actionMapDirection.addMarker(pathDirection.get(loopIndex - 1), latLng);
					Log.d("버스인포액티비티", pathDirection.get(loopIndex - 1) + latLng.toString());
					directionFlag = true;
				}
				if (loopIndex < pathDirection.size()) {
					loopQuery();
				} else {
					loopIndex = 0;
					if (!directionFlag) {
						Log.d("버스인포액티비티", "역방향으로 체인지");
						textDirection.setText("역방향");
						settingSwitch(BACKWARD);
						loopQuery();
					} else {
						actionMapDirection.drawLine();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				new AlertDialog.Builder(this).setTitle("경로데이터가 부족합니다").create().show();
			} finally {
				break;
			}
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d("로더리셋", "불려짐");
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub

	}

}
