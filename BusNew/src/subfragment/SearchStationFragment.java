package subfragment;

import subfragment.CustomMapFragment.OnMapReadyListener;
import util.ActionMap;
import util.AnimationRelativeLayout;
import adapter.StationSearchListCursorAdapter;
import android.app.Activity;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class SearchStationFragment extends ListFragment implements LoaderCallbacks<Cursor>, OnKeyListener,
		OnMapReadyListener, OnClickListener, OnBackAction {

	public static final String TAG_STATION_MAP = "stationMap";
	public static final String KEY_SERARCH = "station";
	public static final String KEY_WIDE_LATITUDE = "wideLatitude";
	public static final String KEY_WIDE_LONGITUDE = "wideLongitude";
	public static final int SEARCH_STATION = 0;
	public static final int SEARCH_WIDE = 1;

	// "_id", "station_number", "station_name", "station_longitude",
	// "station_latitude", "station_favorite"
	public static final int STATION_ID_INDEX = 0;
	public static final int STATION_NUMBER_INDEX = 1;
	public static final int STATION_NAME_INDEX = 2;
	public static final int STATION_LONGITUDE_INDEX = 3;
	public static final int STATION_LATITUDE_INDEX = 4;
	public static final int STATION_FAVORITE_INDEX = 5;

	private StationSearchListCursorAdapter madapter;
	private EditText et;
	private Context context;
	private AnimationRelativeLayout mapContainer;
	private InputMethodManager imm;
	private GoogleMap map;
	private View view;
	private ActionMap actionMap;
	private boolean isGoogleServiceInstalled;
	private int currentCursorId;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_search_station_layout, null);
		et = (EditText) view.findViewById(R.id.edit_search_sub2fragment);
		et.addTextChangedListener(new MyWatcher());
		et.setOnKeyListener(this);
		et.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mapContainer != null)
					mapContainer.hide();
				if(map != null)
					map.clear();
				Bundle search = new Bundle();
				search.putString(KEY_SERARCH, et.getText().toString());
				getLoaderManager().restartLoader(SEARCH_STATION, search, SearchStationFragment.this);
				
			}
		});
		actionMap = new ActionMap(context);
		mapContainer = (AnimationRelativeLayout) view.findViewById(R.id.layout_search_station_map_container);
		mapContainer.setInAnimation((Animation) AnimationUtils.loadAnimation(context, R.animator.in_ani));
		Button btn = (Button) view.findViewById(R.id.btn_search_station_widesearch);
		btn.setOnClickListener(this);
		
		if (!(isGoogleServiceInstalled = actionMap.checkGoogleService())){
			View msgView = ((ViewStub)view.findViewById(R.id.viewstub_search_station_map_fail)).inflate();
			actionMap.setGoogleFailLayout(msgView);
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// 어뎁터 생성등록 커서는 없음.. 로더에서 추가
		madapter = new StationSearchListCursorAdapter(context, null, 0);
		setListAdapter(madapter);
		getLoaderManager().initLoader(SEARCH_STATION, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isGoogleServiceInstalled)
			setupMapIfNeeded();
	}

	private void setupMapIfNeeded() {
		if (map == null) {
			FragmentManager fm = getChildFragmentManager();
			CustomMapFragment mapFragment = (CustomMapFragment) fm.findFragmentByTag(TAG_STATION_MAP);
			if (mapFragment == null) {
				mapFragment = CustomMapFragment.newInstance();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.layout_search_station_map, mapFragment, TAG_STATION_MAP);
				ft.commit();
			}
		}
	}

	// 맵이 준비되면 자동호출
	@Override
	public void OnMapReady(GoogleMap map) {
		this.map = map;
		this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(ActionMap.DEAGU_LATLNG, ActionMap.ZOOM_OUT));
	}

	// boolean flag = true;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = madapter.getCursor();
		c.moveToPosition(position);

		String stationNumber = c.getString(STATION_NUMBER_INDEX);
		String stationName = c.getString(STATION_NAME_INDEX);
		double stationLongitude = c.getDouble(STATION_LONGITUDE_INDEX);
		double stationLatitude = c.getDouble(STATION_LATITUDE_INDEX);

		LatLng stationPosition = new LatLng(stationLatitude, stationLongitude);
		OnSaveBusStationInfoListener saver = (OnSaveBusStationInfoListener) context;
		saver.OnSaveBusStationInfo(stationNumber, stationName, stationPosition);

		// if (flag) {
		// mapContainer.setVisibility(View.VISIBLE);
		// Animation a = new DropDownAnim(mapContainer,
		// mapContainer.getHeight(), true);
		// a.setDuration(1600);
		// mapContainer.startAnimation(a);
		// // mapContainer.show();
		// flag = false;
		// } else {
		// mapContainer.hide();
		// flag = true;
		// }

		if (map != null) {
			mapContainer.show();

			MarkerOptions options = new MarkerOptions().position(stationPosition).title(stationName)
					.icon(BitmapDescriptorFactory.defaultMarker(120));
			map.addMarker(options).showInfoWindow();
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(stationPosition, ActionMap.ZOOM_IN));
		} 
	}

	public class DropDownAnim extends Animation {
		private final int targetHeight;
		private final View view;
		private final boolean down;

		public DropDownAnim(View view, int targetHeight, boolean down) {
			this.view = view;
			this.targetHeight = targetHeight;
			this.down = down;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			int newHeight;
			if (down) {
				newHeight = (int) (targetHeight * interpolatedTime);
			} else {
				newHeight = (int) (targetHeight * (1 - interpolatedTime));
			}
			view.getLayoutParams().height = newHeight;
			view.requestLayout();
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	}

	// class MyScaler extends ScaleAnimation{
	// private View mView;
	// private LinearLayout.LayoutParams mLayoutParams;
	// private int mMarginBottomFromY;
	// private int mMarginBottomToY;
	// private boolean mVanishAfter;
	//
	// public MyScaler(float fromX, float toX, float fromY, float toY, int
	// duration, View view, boolean vanishAfter) {
	// super(fromX, toX, fromY, toY);
	// setDuration(duration);
	// mView =view;
	// mVanishAfter = vanishAfter;
	// mLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
	// int height = mView.getHeight();
	// mMarginBottomFromY = (int) (height * fromY) + mLayoutParams.bottomMargin
	// - height;
	// mMarginBottomToY = (int) (0 - ((height * toY) +
	// mLayoutParams.bottomMargin)) - height;
	// }
	//
	// @Override
	// protected void applyTransformation(float interpolatedTime,
	// Transformation t) {
	// super.applyTransformation(interpolatedTime, t);
	// if(interpolatedTime < 1.0f){
	// int newMarginBottom = mMarginBottomFromY + (int) ((mMarginBottomToY -
	// mMarginBottomFromY) * interpolatedTime);
	// mLayoutParams.setMargins(mLayoutParams.leftMargin,
	// mLayoutParams.topMargin, mLayoutParams.rightMargin, newMarginBottom);
	// mView.getParent().requestLayout();
	// } else if(mVanishAfter){
	// mView.setVisibility(View.VISIBLE);
	// }
	// }
	// }

	class MyWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// 데이터베이스 검색 하여 리스트뷰 새로 뿌림
			Bundle search = new Bundle();
			search.putString(KEY_SERARCH, s.toString());
			getLoaderManager().restartLoader(SEARCH_STATION, search, SearchStationFragment.this);
		}

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d("정류장검색","로더 생성자 호출됨");
		Uri baseUri = MyContentProvider.CONTENT_URI_STATION;

		currentCursorId = id;
		// 이것의 순서를 바꿔줄시 반드시 위의 상수인덱스 값도 변경해줘야함
		String[] projection = { "_id", "station_number", "station_name", "station_longitude", "station_latitude",
				"station_favorite" };
		String selection = null;

		switch (id) {
		// 커서어뎁터의 경우_id 안넣으면 에러 슈바
		case SEARCH_STATION:
			if (args != null) {
				selection = "station_name like '%" + args.getString(KEY_SERARCH) + "%' OR station_number like '%"
						+ args.getString(KEY_SERARCH) + "%'";
			}
			break;
		case SEARCH_WIDE:
			Log.d("정류소", "주변검색작동");
			double latitude = args.getDouble(KEY_WIDE_LATITUDE);
			double longitude = args.getDouble(KEY_WIDE_LONGITUDE);

			double bound = 0.005;
			double minLatitude = latitude - bound;
			double maxLatitude = latitude + bound;
			double minLongitude = longitude - bound;
			double maxLongitude = longitude + bound;

			if (args != null) {
				selection = "(station_latitude BETWEEN " + minLatitude + " AND " + maxLatitude + ") AND ("
						+ "station_longitude BETWEEN " + minLongitude + " AND " + maxLongitude + ")";
			}
			break;
		}

		return new CursorLoader(getActivity(), baseUri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("정류장검색","로더 finish 호출됨");
		// 앞서 생성된 커서를 받아옴
		if(currentCursorId == loader.getId())
			madapter.swapCursor(cursor);

		if (loader.getId() == SEARCH_WIDE) {
			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {
				LatLng position = new LatLng(cursor.getDouble(4), cursor.getDouble(3));
				MarkerOptions options = new MarkerOptions().position(position).title(cursor.getString(2))
						.snippet(cursor.getString(1));
				map.addMarker(options);
				cursor.moveToNext();
			}
			map.animateCamera(CameraUpdateFactory.zoomTo(14));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		madapter.swapCursor(null);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
			imm.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			return true;
		}

		return false;
	}

	// 주변검색
	@Override
	public void onClick(View v) {
		Bundle mapCenterCoordinate = new Bundle();
		mapCenterCoordinate.putDouble(KEY_WIDE_LATITUDE, map.getCameraPosition().target.latitude);
		mapCenterCoordinate.putDouble(KEY_WIDE_LONGITUDE, map.getCameraPosition().target.longitude);
		map.clear();
		getLoaderManager().restartLoader(SEARCH_WIDE, mapCenterCoordinate, this);
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
