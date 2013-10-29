package com.example.busnew.sub;

import util.MyLocation;
import util.MyLocation.LocationResult;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.busnew.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GMapFragment extends Fragment {

	private SupportMapFragment mapFragment;
	private Context context;
	private GoogleMap map;
	private float density;

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


	@Override
	public void onResume() {
		super.onResume();

		final ProgressDialog wait = ProgressDialog.show(context, null, "위치정보를 받아오는 중입니다. 잠시만 기다려주세요");
		
		// MyLocation 클래스 콜백 리스너. gps나 네트웤 위치 신호가 오기까지 기다리다가 onchange 리스너가 호출되면
		// 그 결과값을 gotLocation 메소드로 리턴해준다. 
		LocationResult locationResult = new LocationResult() {
			@Override
			public void gotLocation(Location location) {
				map = mapFragment.getMap();

				if (map != null) {
					wait.dismiss();
					
					map.setMyLocationEnabled(true);
					LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
				}
			}
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(context, locationResult);

		
	}

	public void setGMap(String station_number, String station_name,
			LatLng latLng) {
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
}
