package adapter;

import java.util.ArrayList;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActionMap {
	
	private GoogleMap map;
	private int colorIndex;
	private LatLng prePoint;
	private float density;
	
	public ActionMap(float density){
		if(map==null)
			Log.d("맵액션클래스","현재 map이 null 입니다. 주의하세요");
		this.density = density;
		init();
	}
	
	public ActionMap(GoogleMap map, float density){
		if(map != null)
			this.map = map;
		else 
			Log.d("맵액션클래스","map 이 Null 입니다");
		this.density = density;
		init();
	}
	
	private void init(){
		colorIndex = 150;
	}
	
	public void setMap(GoogleMap map){
		this.map = map;
	}
	
	public void drawLine(LatLng point){
		colorIndex += 5;
		colorIndex %= 360;
		int color = Color.HSVToColor(new float[]{colorIndex,1,1});
		if(prePoint != null){
			map.addPolyline((new PolylineOptions()).add(prePoint, point).width((int)(9*density)).color(Color.BLACK));
			map.addPolyline((new PolylineOptions()).add(prePoint, point).width((int)(5*density)).color(color));
			
		}
		prePoint = point;
	}
	
	public void addMarker(String title, String contents, LatLng latLng){
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).snippet(contents).position(latLng);
		map.addMarker(markerOptions);
	}
	
	public void addMarker(String title, LatLng latLng){
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.title(title).position(latLng);
		map.addMarker(markerOptions);
	}
	
	public void moveMap(LatLng position){
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11));
	}
	
	// 맵객체 존재여부 검사 있으면 true
	public boolean isMap(){
		return (map==null)?false:true;
	}

}
