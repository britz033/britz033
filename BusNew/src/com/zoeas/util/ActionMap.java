package com.zoeas.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zoeas.qdeagubus.R;

public class ActionMap<MarkerInfo> implements OnInfoWindowClickListener {

	public interface OnActionInfoWindowClickListener<MarkerInfo> {
		public void onInfoWindowClick(Marker marker, MarkerInfo markerAdditionalinfo);
	}

	public static final LatLng DEAGU_LATLNG = new LatLng(35.871942, 128.601122);
	public static final int GOOGLE_SERVICE_REQUEST_CODE = 1001;

	public static final int ZOOM_NOMAL = 14;
	public static final int ZOOM_IN = 16;
	public static final int ZOOM_OUT = 11;
	public static final double ADJUST_INFO_CENTER_NOMAL = 0.0025d;
	public static final double ADJUST_INFO_CENTER_IN = 0.0005d;

	private String errorMsg;
	private PendingIntent pendingIntent;

	private GoogleMap map;
	private Context context;
	private int colorIndex;
	private LatLngBounds.Builder lineBoundBuilder;
	private int zIndex;
	private ArrayList<LatLng> latLngList;
	private Marker preMarker;
	private MarkerOptions markerDefaultOptions;
	private HashMap<Marker, MarkerInfo> markerAdditionalInfo;
	private OnActionInfoWindowClickListener clicker;
	private boolean defaultAdjust;
	private float density;

	public ActionMap(Context context) {
		if (map == null)
		init(context);
	}

	public ActionMap(Context context, GoogleMap map) {
		if (map != null)
			this.map = map;

		init(context);
	}

	private void init(Context context) {
		this.context = context;

		density = context.getResources().getDisplayMetrics().density;
		colorIndex = Color.HSVToColor(new float[] { 120, 1, 1 });
		zIndex = 0;
		latLngList = new ArrayList<LatLng>();
		markerAdditionalInfo = new HashMap<Marker, MarkerInfo>();
		defaultAdjust = true;
		lineBoundBuilder = new LatLngBounds.Builder();
	}

	public boolean checkGoogleService() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				Toast.makeText(context, "현재 구글맵 서비스가 설치되어 있지 않습니다", Toast.LENGTH_LONG).show();
				errorMsg = GooglePlayServicesUtil.getErrorString(resultCode);
				if(resultCode == 9)
					resultCode = 2;
				pendingIntent = GooglePlayServicesUtil.getErrorPendingIntent(resultCode, context,
						GOOGLE_SERVICE_REQUEST_CODE);
			} else {
				((Activity) context).finish();
			}
			return false;
		}

		return true;
	}

	public void setGoogleFailLayout(View msgView) {
		((TextView) msgView.findViewById(R.id.text_google_service_error_msg)).setText(errorMsg);
		((Button) msgView.findViewById(R.id.btn_google_service_pendingintent))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							pendingIntent.send(ActionMap.GOOGLE_SERVICE_REQUEST_CODE);
						} catch (CanceledException e) {
							e.printStackTrace();
						}
					}
				});
	}

	public void setMap(GoogleMap map) {
		this.map = map;
		markerDefaultOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bus2));
	}

	public void addLatLngPoint(LatLng point) {
		latLngList.add(point);
		lineBoundBuilder.include(point);
	}
	
	public void setLineBound(){
		LatLngBounds bounds = lineBoundBuilder.build();
		moveMap(bounds,10);
	}
	
	public void setBoundAndMoveCenter(ArrayList<LatLng> pointList){
		LatLngBounds.Builder newbuilder = new LatLngBounds.Builder();
		for(int i=0; i<pointList.size(); i++)
			newbuilder.include(pointList.get(i));
		LatLngBounds bounds = newbuilder.build();
		moveMap(bounds,10);
	}

	public void drawLine() {
		if (map != null) {
			zIndex++;

			int depth = 1000; // 위선과 아랫선이 겹치지 않게

			int color = colorIndex;
			if (latLngList.size() > 2) {
				map.addPolyline((new PolylineOptions()).addAll(latLngList).width((int) (5 * density))
						.color(Color.BLACK).zIndex(zIndex));
				map.addPolyline((new PolylineOptions()).addAll(latLngList).width((int) (3 * density)).color(color)
						.zIndex(zIndex + depth));
			} else {
				new AlertDialog.Builder(context).setTitle("경로수가 2개 이하입니다").show();
			}
		}
	}

	public void removeMarker() {
		if (preMarker != null) {
			preMarker.remove();
			markerAdditionalInfo.remove(preMarker);
		}
	}

	public Marker addMarker(MarkerOptions options, MarkerInfo additionalInfo) {
		if (map != null) {
			options.icon(markerDefaultOptions.getIcon());
			preMarker = map.addMarker(options);
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	public Marker addMarker(String title, LatLng latLng, MarkerInfo additionalInfo) {
		if (map != null) {
			markerDefaultOptions.title(title).position(latLng);
			preMarker = map.addMarker(markerDefaultOptions);
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	public Marker addMarker(String title, LatLng latLng, int icon, MarkerInfo additionalInfo) {
		if (map != null) {
			markerDefaultOptions.title(title).position(latLng).icon(BitmapDescriptorFactory.fromResource(icon))
					.anchor(0.5f, 0.5f);
			preMarker = map.addMarker(markerDefaultOptions);
			return preMarker;
		}
		return null;
	}

	public Marker addMarker(String title, String contents, LatLng latLng, MarkerInfo additionalInfo) {
		if (map != null) {
			preMarker = map.addMarker(markerDefaultOptions);
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	public Marker addMarker(String title, String contents, LatLng latLng, int icon, MarkerInfo additionalInfo) {
		if (map != null) {
			markerDefaultOptions.title(title).snippet(contents).position(latLng)
					.icon(BitmapDescriptorFactory.fromResource(icon));
			preMarker = map.addMarker(markerDefaultOptions);
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	public Marker addMarkerAndShow(MarkerOptions options, MarkerInfo additionalInfo) {
		if (map != null) {
			options.icon(markerDefaultOptions.getIcon());
			preMarker = map.addMarker(options);
			preMarker.showInfoWindow();
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	public Marker addMarkerAndShow(String title, LatLng latLng, MarkerInfo additionalInfo) {
		if (map != null) {
			markerDefaultOptions.title(title).position(latLng);
			preMarker = map.addMarker(markerDefaultOptions);
			preMarker.showInfoWindow();
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	public Marker addMarkerAndShow(int position, String title, MarkerInfo additionalInfo) {
		if (map != null) {
			try {
				LatLng latLng = latLngList.get(position);
				markerDefaultOptions.title(title).position(latLng);
				preMarker = map.addMarker(markerDefaultOptions);
				preMarker.showInfoWindow();
				markerAdditionalInfo.put(preMarker, additionalInfo);
			} catch (Exception e) {
				e.printStackTrace();
				new AlertDialog.Builder(context).setTitle("맵 포인트로 이동중 문제가 발생하였습니다 죄송합니다").create().show();
			}

			return preMarker;
		}
		return null;
	}
	public void moveMap(LatLng position) {
		if (map != null) {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_OUT));
		}
	}
	
	public void moveMap(LatLngBounds bounds, int padding) {
		if (map != null) {
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int)(padding*density)));
		}
	}

	public void aniMap(LatLng latLng) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustDefault(latLng, ZOOM_OUT), ZOOM_OUT));
		}
	}

	public void aniMap(LatLng latLng, int zoom) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustDefault(latLng, zoom), zoom));
		}
	}
	
	public void aniMapZoom(int zoom){
		map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
	}
	
	public void aniMap(LatLngBounds bounds,  int padding) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int)(padding*density)));
		}
	}
	
	public void aniMap(int position) {
		if (map != null) {
			try {
				LatLng latLng = latLngList.get(position);

				CameraPosition cp = CameraPosition.builder().target(adjustDefault(latLng, ZOOM_NOMAL)).zoom(ZOOM_NOMAL)
						.tilt(50).build();
				map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
			} catch (Exception e) {
				e.printStackTrace();
				new AlertDialog.Builder(context).setTitle("맵 포인트로 이동중 문제가 발생하였습니다 죄송합니다").create().show();
			}
		}
	}

	public void setAdjustDefault(boolean adjust) {
		defaultAdjust = adjust;
	}

	private LatLng adjustDefault(LatLng latLng, int zoom) {
		if (defaultAdjust) {
			switch(zoom){
			case ZOOM_IN :
				return new LatLng(latLng.latitude + ADJUST_INFO_CENTER_IN, latLng.longitude);
			case ZOOM_NOMAL :
				return new LatLng(latLng.latitude + ADJUST_INFO_CENTER_NOMAL, latLng.longitude);
			case ZOOM_OUT :
				return new LatLng(latLng.latitude + ADJUST_INFO_CENTER_NOMAL, latLng.longitude);
			}
			
		}
		return latLng;
	}

	public static LatLng adjustLatLng(LatLng latLng, double y, double x) {
		return new LatLng(latLng.latitude + y, latLng.longitude + x);
	}

	public LatLng getCenterOfMap() {
		return map.getCameraPosition().target;
	}

	public static double latLngToMeter(double lat1, double lon1, double lat2, double lon2) { // generally
																								// used
																								// geo
																								// measurement
																								// function
		double R = 6378.137; // Radius of earth in KM
		double dLat = (lat2 - lat1) * Math.PI / 180;
		double dLon = (lon2 - lon1) * Math.PI / 180;
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180)
				* Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;
		return d * 1000; // meters
	}

	public static double latLngToMeter(LatLng latLng1, LatLng latLng2) { // generally
																			// used
																			// geo
																			// measurement
																			// function
		double R = 6378.137; // Radius of earth in KM
		double dLat = (latLng2.latitude - latLng1.latitude) * Math.PI / 180;
		double dLon = (latLng2.longitude - latLng1.longitude) * Math.PI / 180;
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(latLng1.latitude * Math.PI / 180)
				* Math.cos(latLng2.latitude * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;
		return d * 1000; // meters
	}

	public static double getRadius(double degree) {
		return latLngToMeter(0, 0, 0, degree);
	}

	public static boolean isInsideCircle(Circle circle, LatLng latLng) {
		float[] distance = new float[2]; // 0번은 거리, 1번은 시작점이 가르키던 방향, 2번은 끝점의 방향

		Location.distanceBetween(latLng.latitude, latLng.longitude, circle.getCenter().latitude,
				circle.getCenter().longitude, distance);

		return (distance[0] <= circle.getRadius()) ? true : false;
	}

	public void clearMap() {
		if (map != null) {
			map.clear();
		}
	}

	public boolean isMap() {
		return (map == null) ? false : true;
	}

	public void setOnActionInfoWindowClickListener(OnActionInfoWindowClickListener instance) {
		clicker = instance;
		map.setOnInfoWindowClickListener(this);
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		MarkerInfo additionalInfo = markerAdditionalInfo.get(marker);
		if (clicker != null)
			clicker.onInfoWindowClick(marker, additionalInfo);
	}

}
