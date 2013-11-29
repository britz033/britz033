package util;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActionMap {

	// 한국좌표는 구글좌표 기준으로 북쪽 +, 오른쪽 +
	public static final LatLng DEAGU_LATLNG = new LatLng(35.871942,128.601122); 
	
	public static final int ZOOM_NOMAL = 14;
	public static final int ZOOM_IN = 16;
	public static final int ZOOM_OUT = 11;
//	private static final double CORRECT = 0.00062799;  // 좌표 -> 미터 변환시 오차수정상수인데.. 왠지 잘못한듯?
	
	private GoogleMap map;
	private Context context;
	// Color.HSVToColor( 알파[0..255], float{ 색상[0-360), 채도[0...1], 명도[0...1] }
	private int colorIndex;
	private LatLng prePoint;
	private float density;
	private int zIndex;
	private ArrayList<LatLng> latLngList;
	private Marker preMarker;
	private MarkerOptions markerDefaultOptions;

	public ActionMap(Context context, float density) {
		if (map == null)
			Log.d("맵액션클래스", "현재 map이 null 입니다. 주의하세요");
		init(context, density);
	}

	public ActionMap(Context context, GoogleMap map, float density) {
		if (map != null)
			this.map = map;
		else
			Log.d("맵액션클래스", "map 이 Null 입니다");

		init(context, density);
	}

	private void init(Context context, float density) {
		this.density = density;
		this.context = context;
		
		colorIndex = Color.HSVToColor(new float[] { 120, 1, 1 });
		zIndex = 0;
		latLngList = new ArrayList<LatLng>();
		markerDefaultOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(220));
	}

	public void setMap(GoogleMap map) {
		this.map = map;
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

	public void drawLine() {
		Log.d("드로우",latLngList.size()+"");
		zIndex++;

		int depth = 1000; // 위선과 아랫선이 겹치지 않게

		int color = colorIndex;
		if (latLngList.size() > 2) {
			map.addPolyline((new PolylineOptions()).addAll(latLngList).width((int) (5 * density)).color(Color.BLACK)
					.zIndex(zIndex));
			map.addPolyline((new PolylineOptions()).addAll(latLngList).width((int) (3 * density)).color(color)
					.zIndex(zIndex + depth));
		} else {
			new AlertDialog.Builder(context).setTitle("경로수가 2개 이하입니다").show();
		}
	}

	public void removeMarker() {
		if (preMarker != null)
			preMarker.remove();
	}

	public void addMarker(String title, LatLng latLng) {
		markerDefaultOptions.title(title).position(latLng);
		preMarker = map.addMarker(markerDefaultOptions);
	}

	public void addMarkerAndShow(String title, LatLng latLng) {
		markerDefaultOptions.title(title).position(latLng);
		preMarker = map.addMarker(markerDefaultOptions);
		preMarker.showInfoWindow();
	}

	public void addMarkerAndShow(int position, String title) {
		try {
			LatLng latLng = latLngList.get(position);
			markerDefaultOptions.title(title).position(latLng);
			preMarker = map.addMarker(markerDefaultOptions);
			preMarker.showInfoWindow();
		} catch (Exception e) {
			e.printStackTrace();
			new AlertDialog.Builder(context).setTitle("맵 포인트로 이동중 문제가 발생하였습니다 죄송합니다").create().show();
		}
	}

	public void addMarker(String title, LatLng latLng, int icon) {
		markerDefaultOptions.title(title).position(latLng).icon(BitmapDescriptorFactory.fromResource(icon)).anchor(0.5f, 0.5f);
		preMarker = map.addMarker(markerDefaultOptions);
	}

	public void addMarker(String title, String contents, LatLng latLng) {
		markerDefaultOptions.title(title).snippet(contents).position(latLng);
		preMarker = map.addMarker(markerDefaultOptions);
	}

	public void addMarker(String title, String contents, LatLng latLng, int icon) {
		markerDefaultOptions.title(title).snippet(contents).position(latLng).icon(BitmapDescriptorFactory.fromResource(icon));
		preMarker = map.addMarker(markerDefaultOptions);
	}

	public void moveMap(LatLng position) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_OUT));
	}

	public void aniMap(LatLng latLng) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_OUT));
	}
	
	public void aniMap(LatLng latLng, int zoom) {
		if(latLng == null)
			map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
		else
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
	}

	public void aniMap(int position) {
		Log.d("사이즈",latLngList.size()+"");
		try {
			LatLng latLng = latLngList.get(position);
			latLng = new LatLng(latLng.latitude +0.0025d,latLng.longitude);
			
			CameraPosition cp =  CameraPosition.builder().target(latLng).zoom(ZOOM_NOMAL).tilt(50).build();
			map.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
		} catch (Exception e) {
			e.printStackTrace();
			new AlertDialog.Builder(context).setTitle("맵 포인트로 이동중 문제가 발생하였습니다 죄송합니다").create().show();
		}
	}
	
	public static double latLngToMeter(double lat1,double lon1,double lat2,double lon2){  // generally used geo measurement function
	    double R = 6378.137; // Radius of earth in KM
	    double dLat = (lat2 - lat1) * Math.PI / 180;
	    double dLon = (lon2 - lon1) * Math.PI / 180;
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = R * c;
	    return d * 1000; // meters
	}
	
	public static double latLngToMeter(LatLng latLng1, LatLng latLng2){  // generally used geo measurement function
	    double R = 6378.137; // Radius of earth in KM
	    double dLat = (latLng2.latitude - latLng1.latitude) * Math.PI / 180;
	    double dLon = (latLng2.longitude - latLng1.longitude) * Math.PI / 180;
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	    Math.cos(latLng1.latitude * Math.PI / 180) * Math.cos(latLng2.latitude * Math.PI / 180) *
	    Math.sin(dLon/2) * Math.sin(dLon/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double d = R * c;
	    return d * 1000; // meters
	}
	
	// 주어진 좌표 도수값이 어느정도의 거리일지를 검출
	public static double getRadius(double degree){
		return latLngToMeter(0, 0, 0, degree);
	}
	
	public static boolean isInsideCircle(Circle circle, LatLng latLng){
		float[] distance = new float[2];	// 0번은 거리, 1번은 시작점이 가르키던 방향, 2번은 끝점의 방향

		Location.distanceBetween( latLng.latitude, latLng.longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);

		if( distance[0] <= circle.getRadius() ){
			return true;
		} else {
			return false;
		}
	}
	
	public void clearMap(){
		map.clear();
	}

	// 맵객체 존재여부 검사 있으면 true
	public boolean isMap() {
		return (map == null) ? false : true;
	}

}
