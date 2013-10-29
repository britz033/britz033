package com.example.busnew.sub;

import util.MyLocation;
import util.MyLocation.LocationResult;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
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

import com.example.busnew.MainActivity.CallFragmentMethod;
import com.example.busnew.MyContentProvider;
import com.example.busnew.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GMapFragment extends Fragment implements CallFragmentMethod,LoaderCallbacks<Cursor> {

	private SupportMapFragment mapFragment;
	private Context context;
	private GoogleMap map;
	private float density;
	private LatLng myLatLng;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_gmap_layout, null);
		return view;
	}

	// 맵 생성
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		density = context.getResources().getDisplayMetrics().density;

		FragmentManager fm = getChildFragmentManager();
		mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

		if (mapFragment == null) {
			FragmentTransaction ft = fm.beginTransaction();
			mapFragment = SupportMapFragment.newInstance();
			ft.replace(R.id.map, mapFragment);
			ft.commit();
		}
	}

	// 맵 설정시작
	@Override
	public void onResume() {
		super.onResume();

		map = mapFragment.getMap();
		map.setMyLocationEnabled(true);

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
		CameraUpdate c = CameraUpdateFactory.newLatLngZoom(latLng, 15);
		map.animateCamera(c);

	}

	// 생성될때가 아니라 자신이 선택될때 불려진다. 
	// 인터페이스로 메인 ViewPager의 OnPageChangeListener 에서 호출한다.
	@Override
	public void OnCalled() {

		final ProgressDialog wait = ProgressDialog.show(context, null,
				"위치정보를 받아오는 중입니다. 잠시만 기다려주세요");

		// MyLocation 클래스 콜백 리스너. gps나 네트웤 위치 신호가 오기까지 기다리다가 onchange 리스너가 호출되면
		// 그 결과값을 gotLocation 메소드로 리턴해준다.
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {

				if (map != null && location != null) {
					wait.dismiss();
					myLatLng = new LatLng(location.getLatitude(),
							location.getLongitude());
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng,
							17));
					
					getLoaderManager().initLoader(0, null, GMapFragment.this);
				}
			}
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(context, locationResult, new Handler());
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		Log.d("onCreateLoader","called");
		
		Uri uri = MyContentProvider.CONTENT_URI;
		
		final double bound = 0.005; 
		double maxlat = myLatLng.latitude + bound;
		double minlat = myLatLng.latitude - bound;
		double maxlnt = myLatLng.longitude + bound;
		double minlnt = myLatLng.longitude - bound;
		
		String[] projection = {"_id","station_number","station_name", "station_latitude", "station_longitude"};
		String selection = "(station_latitude BETWEEN " + minlat + " AND " + maxlat +
				") AND (station_longitude BETWEEN " + minlnt + " AND " + maxlnt +")";
		
		return new CursorLoader(context, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
		Log.d("onLoadFininshed","called");
		
		// 기존의 cursor를 그대로 불러오기 때문에 시작시 반드시 커서위치를 처음으로 되돌려줘야함
		c.moveToFirst();
		Log.d("counter", String.valueOf(c.getCount()));
		for(int i=0; i<c.getCount(); i++){
			String station_number = c.getString(1); 
			String station_name = c.getString(2); 
			double station_latitude = c.getDouble(3); 
			double station_longitude = c.getDouble(4); 
			LatLng boundLatLng = new LatLng(station_latitude, station_longitude); 
			c.moveToNext();
			map.addMarker(new MarkerOptions().title(station_name).snippet(station_number).position(boundLatLng));
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d("loaderReset","called");
	}
}
