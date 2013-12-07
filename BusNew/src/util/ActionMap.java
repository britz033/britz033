package util;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zoeas.qdeagubus.R;

public class ActionMap<MarkerInfo> implements OnInfoWindowClickListener {

	public interface OnActionInfoWindowClickListener<MarkerInfo> {
		public void onInfoWindowClick(Marker marker, MarkerInfo markerAdditionalinfo);
	}

	// 한국좌표는 구글좌표 기준으로 북쪽 +, 오른쪽 +
	public static final LatLng DEAGU_LATLNG = new LatLng(35.871942, 128.601122);
	public static final int GOOGLE_SERVICE_REQUEST_CODE = 1001;

	public static final int ZOOM_NOMAL = 14;
	public static final int ZOOM_IN = 16;
	public static final int ZOOM_OUT = 11;
	public static final double ADJUST_INFO_CENTER_NOMAL = 0.0025d;
	public static final double ADJUST_INFO_CENTER_IN = 0.0005d;

	// 에러처리 관련
	private String errorMsg;
	private PendingIntent pendingIntent;
	// private static final double CORRECT = 0.00062799; // 좌표 -> 미터 변환시
	// 오차수정상수인데.. 왠지 잘못한듯?

	private GoogleMap map;
	private Context context;
	// Color.HSVToColor( 알파[0..255], float{ 색상[0-360), 채도[0...1], 명도[0...1] }
	private int colorIndex;
	private LatLng prePoint;
	private int zIndex;
	private ArrayList<LatLng> latLngList;
	private Marker preMarker;
	private MarkerOptions markerDefaultOptions;
	private HashMap<Marker, MarkerInfo> markerAdditionalInfo;
	private OnActionInfoWindowClickListener clicker;
	private boolean defaultAdjust;

	public ActionMap(Context context) {
		if (map == null)
			Log.d("맵액션클래스", "현재 map이 null 입니다. 주의하세요");
		init(context);
	}

	public ActionMap(Context context, GoogleMap map) {
		if (map != null)
			this.map = map;
		else
			Log.d("맵액션클래스", "map 이 Null 입니다");

		init(context);
	}

	private void init(Context context) {
		this.context = context;

		colorIndex = Color.HSVToColor(new float[] { 120, 1, 1 });
		zIndex = 0;
		latLngList = new ArrayList<LatLng>();
		markerAdditionalInfo = new HashMap<Marker, MarkerInfo>();
		defaultAdjust = true;
	}

	public boolean checkGoogleService() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
		Log.d("code", String.valueOf(resultCode));
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				Toast.makeText(context, "현재 구글맵 서비스가 설치되어 있지 않습니다", Toast.LENGTH_LONG).show();
				errorMsg = GooglePlayServicesUtil.getErrorString(resultCode);
				pendingIntent = GooglePlayServicesUtil.getErrorPendingIntent(resultCode, context,
						GOOGLE_SERVICE_REQUEST_CODE);
			} else {
				((Activity) context).finish();
			}
			return false;
		}

		// 초기화중에 map관련 변수가 있으면 Serivce가 없을땐 에러뜸 그리고 여기서도 뜨네..;;

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
		markerDefaultOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(220));
	}

	// 색이 다르게 할 수는 있지만 라인이 가닥가닥 따로 그리기에 끊김 현상이 발생
	// public void drawLine(LatLng point){
	// colorIndex -= 2;
	// colorIndex %= 360;
	//
	// zIndex++;
	//
	// int depth = 1000; // 위선과 아랫선이 겹치지 않게
	//
	// int color = Color.HSVToColor(new float[]{colorIndex,1,1});
	// if(prePoint != null){
	// map.addPolyline((new PolylineOptions()).add(prePoint,
	// point).width((int)(5*density)).color(Color.BLACK).zIndex(zIndex));
	// map.addPolyline((new PolylineOptions()).add(prePoint,
	// point).width((int)(3*density)).color(color).zIndex(zIndex+depth));
	// }
	// prePoint = point;
	// }

	public void addLinePoint(LatLng point) {
		latLngList.add(point);
	}

	public void drawLine(float density) {
		if (map != null) {
			Log.d("드로우", latLngList.size() + "");
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

	public void aniMap(LatLng latLng) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustDefault(latLng, ZOOM_OUT), ZOOM_OUT));
		}
	}

	public void aniMap(LatLng latLng, int zoom) {
		if (map != null) {
			if (latLng == null)
				map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
			else
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustDefault(latLng, zoom), zoom));
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

	public void aniMap(int position) {
		if (map != null) {
			Log.d("사이즈", latLngList.size() + "");
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

	// 주어진 좌표 도수값이 어느정도의 거리일지를 검출
	public static double getRadius(double degree) {
		return latLngToMeter(0, 0, 0, degree);
	}

	public static boolean isInsideCircle(Circle circle, LatLng latLng) {
		float[] distance = new float[2]; // 0번은 거리, 1번은 시작점이 가르키던 방향, 2번은 끝점의 방향

		Location.distanceBetween(latLng.latitude, latLng.longitude, circle.getCenter().latitude,
				circle.getCenter().longitude, distance);

		if (distance[0] <= circle.getRadius()) {
			return true;
		} else {
			return false;
		}
	}

	public void clearMap() {
		if (map != null) {
			map.clear();
		}
	}

	// 맵객체 존재여부 검사 있으면 true
	public boolean isMap() {
		return (map == null) ? false : true;
	}

	public void setOnActionInfoWindowClickListener(OnActionInfoWindowClickListener instance) {
		clicker = instance;
		map.setOnInfoWindowClickListener(this);
	}

	// fragment가 있으면 그것을 기준으로 하고 없으면 activity라고 가정한다
	@Override
	public void onInfoWindowClick(Marker marker) {
		MarkerInfo additionalInfo = markerAdditionalInfo.get(marker);
		if (clicker != null)
			clicker.onInfoWindowClick(marker, additionalInfo);
		else
			Log.e("액션맵", "마커클릭 리스너가 지정되어있지 않습니다");
	}

}
