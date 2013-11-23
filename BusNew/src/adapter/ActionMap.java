package adapter;

import java.util.ArrayList;

import util.MapAdjust;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActionMap {

	public static final LatLng DEAGU_LATLNG = new LatLng(35.871942,128.601122); 
	
	public static final int ZOOM_NOMAL = 14;
	public static final int ZOOM_IN = 16;
	public static final int ZOOM_OUT = 11;
	
	private GoogleMap map;
	private Context context;
	private int colorIndex;
	private LatLng prePoint;
	private float density;
	private int zIndex;
	private ArrayList<LatLng> latLngList;
	private Marker preMarker;

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
		colorIndex = 120;
		zIndex = 0;
		latLngList = new ArrayList<LatLng>();
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

		int color = Color.HSVToColor(new float[] { colorIndex, 1, 1 });
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
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).position(latLng);
		preMarker = map.addMarker(markerOptions);
	}

	public void addMarkerAndShow(String title, LatLng latLng) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).position(latLng);
		preMarker = map.addMarker(markerOptions);
		preMarker.showInfoWindow();
	}

	public void addMarkerAndShow(int position, String title) {
		try {
			LatLng latLng = latLngList.get(position);
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.title(title).position(latLng);
			preMarker = map.addMarker(markerOptions);
			preMarker.showInfoWindow();
		} catch (Exception e) {
			e.printStackTrace();
			new AlertDialog.Builder(context).setTitle("맵 포인트로 이동중 문제가 발생하였습니다 죄송합니다").create().show();
		}
	}

	public void addMarker(String title, LatLng latLng, int icon) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).position(latLng).icon(BitmapDescriptorFactory.fromResource(icon)).anchor(0.5f, 0.5f);
		preMarker = map.addMarker(markerOptions);
	}

	public void addMarker(String title, String contents, LatLng latLng) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).snippet(contents).position(latLng);
		preMarker = map.addMarker(markerOptions);
	}

	public void addMarker(String title, String contents, LatLng latLng, int icon) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).snippet(contents).position(latLng).icon(BitmapDescriptorFactory.fromResource(icon));
		preMarker = map.addMarker(markerOptions);
	}

	public void moveMap(LatLng position) {
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_OUT));
	}

	public void aniMap(LatLng latLng) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_OUT));
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
	
	public void clearMap(){
		map.clear();
	}

	// 맵객체 존재여부 검사 있으면 true
	public boolean isMap() {
		return (map == null) ? false : true;
	}

}
