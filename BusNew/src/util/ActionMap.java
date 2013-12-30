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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.zoeas.qdeagubus.R;

/**
 * 구글맵에 몇가지 저장기능을 추가하고 
 * 맵의 안정성과 긴 코드를 간결하게 하기위해 만듦
 * @author lol
 *
 * @param <MarkerInfo>	마커에 추가적으로 데이터를 넣을 때 데이터형을 결정한다
 */
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

		density = context.getResources().getDisplayMetrics().density;
		colorIndex = Color.HSVToColor(new float[] { 120, 1, 1 });
		zIndex = 0;
		latLngList = new ArrayList<LatLng>();
		markerAdditionalInfo = new HashMap<Marker, MarkerInfo>();
		defaultAdjust = true;
		lineBoundBuilder = new LatLngBounds.Builder();
	}

	/**
	 * 구글 서비스가 이용 가능한지 검사하고 동시에 설치가 안되어 있으면 에러메세지와
	 * 서비스로의 이동이 가능한 pendingIntent 를 저장한다
	 * 
	 * @return	true 설치되어 있음 / false 설치되어 있지 않음
	 */
	public boolean checkGoogleService() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
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

	/**
	 * 구글 서비스의 이용여부를 검사한 이후 실패시의 메세지 TextView 와 바로가기 Button을 지원한다
	 * 이때 제공하는 view의 TextView 의 id는 text_google_service_error_msg
	 * Button의 id는 btn_google_service_pendingintent 
	 * 으로 정해져 있다. 이후 레이아웃만 제공해주면 addView 형식으로 제공할 계획
	 * <p>
	 * @param msgView	메세지와 버튼을 표시할 Container 레이아웃, <ul><p class="id">
	 * <li>TextView 의 id는 <em>text_google_service_error_msg</em><li>Button의 id는 <em>btn_google_service_pendingintent</em> 으로 미리 정의해줘야한다
	 * </p>
	 */
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

	/**
	 * 지도에 그릴 라인포인트를 설정한다
	 * @param point	LatLng형식의 점을 연결할 포인트
	 */
	public void addLatLngPoint(LatLng point) {
		latLngList.add(point);
		lineBoundBuilder.include(point);
	}
	
	/**
	 * 지도에 라인을 그릴때 라인크기에 맞춰서 경계를 설정한다
	 */
	public void setLineBound(){
		LatLngBounds bounds = lineBoundBuilder.build();
		moveMap(bounds,10);
	}
	
	/**
	 * 경계를 정하고 그 경계의 중앙으로 카메라를 이동한다
	 * @param pointList	이 포인트들을 포함하는 최소한의 사각형 경계를 설정한다
	 */
	public void setBoundAndMoveCenter(ArrayList<LatLng> pointList){
		LatLngBounds.Builder newbuilder = new LatLngBounds.Builder();
		for(int i=0; i<pointList.size(); i++)
			newbuilder.include(pointList.get(i));
		LatLngBounds bounds = newbuilder.build();
		moveMap(bounds,10);
	}

	/**
	 * 현재 버스경로 전용메소드
	 */
	public void drawLine() {
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

	/**
	 * 바로 전의 마커가 있다면 그 마커를 제거한다
	 */
	public void removeMarker() {
		if (preMarker != null) {
			preMarker.remove();
			markerAdditionalInfo.remove(preMarker);
		}
	}

	/**
	 * 지도에 마커를 지정한다. 생성된 마커는 다음 마커 생성 시까지 ActionMap 내부에 일시적으로 보관된다 
	 * @param options	마커옵션
	 * @param additionalInfo	 마커의 추가정보 데이터형은 actionMap생성시 제네릭으로 설정. null 가능
	 * @return	생성된 마커
	 */
	public Marker addMarker(MarkerOptions options, MarkerInfo additionalInfo) {
		if (map != null) {
			preMarker = map.addMarker(options);
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	/**
	 * 지도에 마커를 지정한다. 생성된 마커는 다음 마커 생성 시까지 ActionMap 내부에 일시적으로 보관된다 
	 * @param title	마커제목
	 * @param latLng	마커좌표
	 * @param additionalInfo 마커의 추가정보 데이터형은 actionMap생성시 제네릭으로 설정. null 가능
	 * @return 생성된 마커
	 */
	public Marker addMarker(String title, LatLng latLng, MarkerInfo additionalInfo) {
		if (map != null) {
			markerDefaultOptions.title(title).position(latLng);
			preMarker = map.addMarker(markerDefaultOptions);
			markerAdditionalInfo.put(preMarker, additionalInfo);
			return preMarker;
		}
		return null;
	}

	/**
	 * 지도에 커스텀 아이콘으로 마커를 지정한다. 생성된 마커는 다음 마커 생성 시까지 ActionMap 내부에 일시적으로 보관된다 
	 * @param title	마커제목
	 * @param latLng	마커좌표
	 * @param icon	마커로 사용할 이미지의 리소스ID
	 * @param additionalInfo 마커의 추가정보 데이터형은 actionMap생성시 제네릭으로 설정. null 가능
	 * @return 생성된 마커
	 */
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

	/**
	 * 마커를 추가하고 컨텐츠윈도우를 보여준다. 생성된 마커는 다음 마커 생성 시까지 ActionMap 내부에 일시적으로 보관된다 
	 * @param options 마커옵션
	 * @param additionalInfo 마커의 추가정보 데이터형은 actionMap생성시 제네릭으로 설정. null 가능
	 * @return 생성된 마커
	 */
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

	/**
	 * 맵을 11레벨 줌을 기반으로 이동한다
	 * @param position
	 */
	public void moveMap(LatLng position) {
		if (map != null) {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM_OUT));
		}
	}
	
	/**
	 * 맵을 설정된 경계의 중심으로 이동한다
	 * @param bounds	맵을 이동할 경계영역 
	 * @param padding	경계영역의 여유공간을 설정한다
	 */
	public void moveMap(LatLngBounds bounds, int padding) {
		if (map != null) {
			map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int)(padding*density)));
		}
	}

	/**
	 * 맵을 11레벨 줌을 기반으로 이동한다. 이때 좌표값은 기본 교정 값에 의해 수정된다
	 * @param latLng	이동할 좌표
	 */
	public void aniMap(LatLng latLng) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustDefault(latLng, ZOOM_OUT), ZOOM_OUT));
		}
	}

	/**
	 * 맵을 전달된 zoom 레벨기반으로 이동한다. 이때 좌표값은 기본 교정 값에 의해 수정된다
	 * @param latLng	이동할 좌표
	 * @param zoom	줌 레벨
	 */
	public void aniMap(LatLng latLng, int zoom) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngZoom(adjustDefault(latLng, zoom), zoom));
		}
	}
	
	/**
	 * 맵을 확대, 축소한다
	 * @param zoom
	 */
	public void aniMapZoom(int zoom){
		map.animateCamera(CameraUpdateFactory.zoomTo(zoom));
	}
	
	public void aniMap(LatLngBounds bounds,  int padding) {
		if (map != null) {
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, (int)(padding*density)));
		}
	}
	
	/**
	 * 맵을 기존에 저장된 좌표리스트의 position으로 이동한다. 줌은 14, 좌표값은 디폴트 교정값으로 수정된다
	 * @param position
	 */
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

	/**
	 * 좌표값을 디폴트 교정 값으로 자동 수정할지 설정한다
	 * @param adjust 교정 여부
	 */
	public void setAdjustDefault(boolean adjust) {
		defaultAdjust = adjust;
	}

	/**
	 * 각 줌레벨에 따라 자동으로 좌표를 수정한다
	 * @param latLng 수정할 좌표값
	 * @param zoom	수정할 좌표값의 줌레벨
	 * @return	수정된 좌표값
	 */
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

	/**
	 * 좌표값을 수정한다
	 * @param latLng	수정할 좌표값
	 * @param y	위도 수정치
	 * @param x	경도 수정치
	 * @return	수정된 좌표값
	 */
	public static LatLng adjustLatLng(LatLng latLng, double y, double x) {
		return new LatLng(latLng.latitude + y, latLng.longitude + x);
	}

	/**
	 * 맵의 중심 좌표값을 가져온다
	 * @return	맵의 중심 좌표값
	 */
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

	/**
	 * 주어진 좌표 도수값이 몇 미터인지를 반환한다
	 * @param degree	변환할 좌표값
	 * @return	미터
	 */
	public static double getRadius(double degree) {
		return latLngToMeter(0, 0, 0, degree);
	}

	/**
	 * 원안에 좌표값이 존재하는 지를 검출한다
	 * @param circle	검사할 원
	 * @param latLng	검사할 좌표
	 * @return	존재시 true
	 */
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

	/**
	 * GoogleMap 객체가 존재하는지 검출한다
	 * @return 존재시 true
	 */
	public boolean isMap() {
		return (map == null) ? false : true;
	}

	/**
	 * 객체에 onInfoWindowClick 리스너를 등록한다
	 * @param instance	등록할 객체
	 */
	public void setOnActionInfoWindowClickListener(OnActionInfoWindowClickListener instance) {
		clicker = instance;
		map.setOnInfoWindowClickListener(this);
	}

	/**
	 * 마커클릭시 리스너가 등록된 ActionMap 객체에서 마커의 기본정보와 추가정보를 반환한다
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		MarkerInfo additionalInfo = markerAdditionalInfo.get(marker);
		if (clicker != null)
			clicker.onInfoWindowClick(marker, additionalInfo);
		else
			Log.e("액션맵", "마커클릭 리스너가 지정되어있지 않습니다");
	}

}
