package subfragment;

import subfragment.CustomMapFragment.OnMapReadyListener;
import util.ActionMap;
import util.MyLocation;
import util.MyLocation.LocationResult;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zoeas.qdeagubus.MainActivity.CallFragmentMethod;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class GMapFragment extends Fragment implements CallFragmentMethod,
		LoaderCallbacks<Cursor>, OnMarkerClickListener, OnMapReadyListener {

	public static final String TAG_MYLOCATION_MAP = "myLocation";
	public static final double DEFAULT_BOUND = 0.005; // 검색범위
	private Context context;
	private GoogleMap map;
	private float density;
	private LatLng myLatLng;
	private LinearLayout loadingLayout;
	private double radius;
	private Circle circle;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		density = context.getResources().getDisplayMetrics().density;

		View view = inflater.inflate(R.layout.fragment_gmap_layout, null);
		loadingLayout = (LinearLayout) view
				.findViewById(R.id.layout_map_loading);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		setupMapIfNeeded();
	}

	// 맵 세팅
	private void setupMapIfNeeded() {
		if (map == null) {
			FragmentManager fm = getChildFragmentManager();
			CustomMapFragment mapFragment = (CustomMapFragment) fm
					.findFragmentByTag(TAG_MYLOCATION_MAP);
			if (mapFragment == null) {
				mapFragment = CustomMapFragment.newInstance();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.layout_gmap, mapFragment, TAG_MYLOCATION_MAP);
				ft.commit();
			}
		}
	}

	// 맵이 준비되면 자동호출
	@Override
	public void OnMapReady(GoogleMap map) {
		this.map = map;
		this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(
				ActionMap.DEAGU_LATLNG, ActionMap.ZOOM_OUT));
		this.map.setMyLocationEnabled(true);
	}

	public void setGMap(String station_number, String station_name,
			LatLng latLng) {
		map.clear();
		BitmapDescriptor icon = BitmapDescriptorFactory
				.fromResource(R.drawable.busicon);

		map.addMarker(new MarkerOptions().title(station_name).position(latLng)
				.snippet(station_number).icon(icon).flat(true).anchor(0, 0)
				.rotation(0));
		map.setPadding(0, 0, 0, (int) (100 * density));
		Log.d("latLng", latLng.toString());
		CameraUpdate c = CameraUpdateFactory.newLatLngZoom(latLng, 14);
		map.animateCamera(c);
	}

	// 생성될때가 아니라 자신이 선택될때 불려진다.
	// 인터페이스로 메인 ViewPager의 OnPageChangeListener 에서 호출한다.
	@Override
	public void OnCalled() {
		Log.d("G맵", "oncalled 인터페이스메소드 호출");
		setupMapIfNeeded();
		loadingLayout.setVisibility(View.VISIBLE);

		// MyLocation 클래스 콜백 리스너. gps나 네트웤 위치 신호가 오기까지 기다리다가 onchange 리스너가 호출되면
		// 그 결과값을 gotLocation 메소드로 리턴해준다.
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {

				if (map != null && location != null) {
					map.clear();
					
					loadingLayout.setVisibility(View.INVISIBLE);
					myLatLng = new LatLng(location.getLatitude(),
							location.getLongitude());
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(
							myLatLng, 15));

					radius = ActionMap.getRadius(DEFAULT_BOUND);
					circle = map.addCircle(new CircleOptions()
							.center(myLatLng)
							.radius(radius)
							.strokeWidth(density * 2)
							.strokeColor(Color.argb(100, 0x4b, 0x4b, 0x4b))
							.fillColor(
									Color.HSVToColor(30, new float[] { 150, 1,
											1 })));

					getLoaderManager().initLoader(0, null, GMapFragment.this);
				}
			}
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(context, locationResult, new Handler());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		Log.d("onCreateLoader", "called");

		Uri uri = MyContentProvider.CONTENT_URI_STATION;

		double maxlat = myLatLng.latitude + DEFAULT_BOUND;
		double minlat = myLatLng.latitude - DEFAULT_BOUND;
		double maxlnt = myLatLng.longitude + DEFAULT_BOUND;
		double minlnt = myLatLng.longitude - DEFAULT_BOUND;

		String[] projection = { "_id", "station_number", "station_name",
				"station_longitude", "station_latitude" };
		String selection = "(station_latitude BETWEEN " + minlat + " AND "
				+ maxlat + ") AND (station_longitude BETWEEN " + minlnt
				+ " AND " + maxlnt + ")";

		return new CursorLoader(context, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		Log.d("onLoadFininshed", "called");

		Handler handler = new Handler();
		// 기존의 cursor를 그대로 불러오기 때문에 시작시 반드시 커서위치를 처음으로 되돌려줘야함
		c.moveToFirst();

		for (int i = 0; i < c.getCount(); i++) {
			String station_number = c.getString(1);
			String station_name = c.getString(2);
			double station_longitude = c.getDouble(3);
			double station_latitude = c.getDouble(4);
			LatLng boundLatLng = new LatLng(station_latitude, station_longitude);
			c.moveToNext();

			if (ActionMap.isInsideCircle(circle, boundLatLng)) {
				final MarkerOptions op = new MarkerOptions();
				op.title(station_name).snippet(station_number)
						.position(boundLatLng);
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						map.addMarker(op);
						map.setOnMarkerClickListener(GMapFragment.this);
					}
				}, 1200);
			}
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("loaderReset", "called");
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		OnSaveBusStationInfoListener saver = (OnSaveBusStationInfoListener) context;
		saver.OnSaveBusStationInfo(marker.getSnippet(), marker.getTitle(),
				new LatLng(0, 0));
		return false;
	}
}
