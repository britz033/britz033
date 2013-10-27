package com.example.busnew.sub;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
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
		
		if(mapFragment == null){
			FragmentTransaction ft = fm.beginTransaction();
			mapFragment = SupportMapFragment.newInstance();
			ft.replace(R.id.map, mapFragment);
			ft.commit();
		}
		
		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		map = mapFragment.getMap();
		
		if(map != null){
		map.setMyLocationEnabled(true);
		}
	}

	public void setGMap(String station_number, String station_name,
			LatLng latLng) {
		BitmapDescriptor icon = BitmapDescriptorFactory
				.fromResource(R.drawable.busicon);

		map.addMarker(new MarkerOptions().title(station_name).position(latLng)
				.snippet(station_number).icon(icon).flat(true)
				.anchor(0, 0).rotation(0));
		map.setPadding(0, 0, 0, (int)(100*density));
		Log.d("latLng",latLng.toString());
		CameraUpdate c = CameraUpdateFactory.newLatLngZoom(latLng, 18);
		map.animateCamera(c);
		
		
	}
}
