package sub.gmap;

import java.util.ArrayList;

import sub.search.station.CustomMapFragment;
import sub.search.station.CustomMapFragment.OnMapReadyListener;
import sub.search.station.SearchStationFragment;
import adapter.OnCommunicationActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.MainActivity.CallFragmentMethod;
import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;
import com.zoeas.util.ActionMap;
import com.zoeas.util.CalculateC;
import com.zoeas.util.MyLocation;
import com.zoeas.util.MyLocation.LocationResult;

public class GMapFragment extends Fragment implements CallFragmentMethod, LoaderCallbacks<Cursor>, OnInfoWindowClickListener, OnMapReadyListener,
		OnBackAction, OnCameraChangeListener {

	public static final String TAG_MYLOCATION_MAP = "myLocation";
	public static final double DEFAULT_BOUND = 0.005; // 검색범위
	public static final String PREFS_NAME ="CHECKBOX_pref";
	public static final String PREFS_CHECK ="CHECKBOX_ONOFF";
	
	private Context context;
	private GoogleMap map;
	private ActionMap actionMap;
	private Bundle searchBundle;
	private ArrayList<Marker> markerList;
	private float density;
	private LatLng myLatLng;
	private LinearLayout loadingLayout;
	private TextView loadingText;
	private double radius;
	private Circle circle;
	private MyLocation myLocation;
	private boolean isGoogleServiceInstalled;
	private CalculateC cc;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		cc = new CalculateC();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		actionMap = new ActionMap(context);
		density = context.getResources().getDisplayMetrics().density;
		searchBundle = new Bundle();
		
		View view = inflater.inflate(R.layout.fragment_gmap_layout, null);

		loadingLayout = (LinearLayout) view.findViewById(R.id.layout_map_loading);
		loadingText = (TextView) view.findViewById(R.id.text_map_loading);

		if (!(isGoogleServiceInstalled = actionMap.checkGoogleService())) {
			View googleFail = ((ViewStub) view.findViewById(R.id.viewstub_gmap_google_fail)).inflate();
			actionMap.setGoogleFailLayout(googleFail);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isGoogleServiceInstalled) {
			setupMapIfNeeded();
		}
	}

	private void setupMapIfNeeded() {
		if (map == null) {
			FragmentManager fm = getChildFragmentManager();
			CustomMapFragment mapFragment = (CustomMapFragment) fm.findFragmentByTag(TAG_MYLOCATION_MAP);
			if (mapFragment == null) {
				mapFragment = CustomMapFragment.newInstance();
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(R.id.layout_gmap, mapFragment, TAG_MYLOCATION_MAP);
				ft.commit();
			}
		}
	}

	@Override
	public void OnMapReady(GoogleMap map) {
		this.map = map;
		this.map.moveCamera(CameraUpdateFactory.newLatLngZoom(ActionMap.DEAGU_LATLNG, ActionMap.ZOOM_OUT));
//		this.map.setMyLocationEnabled(true);
		this.map.setOnCameraChangeListener(this);
		this.map.setOnInfoWindowClickListener(this);
	}

	@Override
	public void OnCalled() {
		final boolean isConfirmed = context.getSharedPreferences(PREFS_NAME, 0).getBoolean(PREFS_CHECK, false);
		
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_title, null);
		final CheckBox check = (CheckBox) view.findViewById(R.id.check_dialog_confirm_title);
		
		if(!isConfirmed){
		new AlertDialog.Builder(context).setTitle("위치정보사용승인").setView(view).setNegativeButton("아니요", 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						loadingText.setText("위치정보확인을 할 수 없습니다");
					}
				})
				.setPositiveButton("예", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(check.isChecked()){
							SharedPreferences setting = context.getSharedPreferences(PREFS_NAME, 0);
							SharedPreferences.Editor editor = setting.edit();
							editor.putBoolean(PREFS_CHECK, true);
							editor.commit();
						}
						loadingText.setText("위치정보를 가져오고 있습니다");
						startLoadLocation();
					}
				}).create().show();
		} else {
			startLoadLocation();
		}
	}
	
	private void startLoadLocation(){
		if (isGoogleServiceInstalled) {
			loadingText.setText("위치정보를 가져오고 있습니다");
			loadingLayout.setVisibility(View.VISIBLE);

			LocationResult locationResult = new LocationResult() {
				@Override
				public void gotLocation(Location location) {

					if (map != null && location != null) {
						map.clear();

						loadingLayout.setVisibility(View.INVISIBLE);
						myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
						map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));

						radius = ActionMap.getRadius(DEFAULT_BOUND);
						circle = map.addCircle(new CircleOptions().center(myLatLng).radius(radius).strokeWidth(density * 2)
								.strokeColor(Color.argb(100, 0x4b, 0x4b, 0x4b)).fillColor(Color.HSVToColor(30, new float[] { 150, 1, 1 })));

						getLoaderManager().initLoader(0, null, GMapFragment.this);
					}
				}
			};
			myLocation = new MyLocation();
			myLocation.getLocation(context, locationResult, new Handler());
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {

		Uri uri = MyContentProvider.CONTENT_URI_STATION;

		double bound = cc.setData(DEFAULT_BOUND);
		double maxlat = cc.setData(myLatLng.latitude) + bound;
		double minlat = cc.setData(myLatLng.latitude) - bound;
		double maxlnt = cc.setData(myLatLng.longitude) + bound;
		double minlnt = cc.setData(myLatLng.longitude) - bound;

		String[] projection = { "_id", "station_number", "station_name", "station_longitude", "station_latitude" };
		String selection = "(station_latitude BETWEEN " + minlat + " AND " + maxlat + ") AND (station_longitude BETWEEN " + minlnt + " AND " + maxlnt
				+ ")";

		return new CursorLoader(context, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

		Handler handler = new Handler();
		c.moveToFirst();
		markerList = new ArrayList<Marker>();

		for (int i = 0; i < c.getCount(); i++) {
			String station_number = c.getString(1);
			String station_name = c.getString(2);
			double station_longitude = cc.getData(c.getDouble(3));
			double station_latitude = cc.getData(c.getDouble(4));
			LatLng boundLatLng = new LatLng(station_latitude, station_longitude);
			c.moveToNext();

			if (ActionMap.isInsideCircle(circle, boundLatLng)) {
				final MarkerOptions op = new MarkerOptions();
				op.title(station_name).snippet(station_number).position(boundLatLng)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_bus));
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						markerList.add(map.addMarker(op));
					}
				}, 1200);
			}
		}

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClear() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPause() {
		super.onPause();
		if (myLocation != null)
			myLocation.cancle();

	}

	@Override
	public void onCameraChange(CameraPosition cPosition) {
		if (markerList != null && markerList.size() != 0) {
			LatLng centerLatLng = cPosition.target;
			LatLng markerLatLng = null;
			double minDistance = 1000;
			Marker nearMarker = null;
			for (int i = 0; i < markerList.size(); i++) {
				markerLatLng = markerList.get(i).getPosition();

				double latgap = Math.abs(centerLatLng.latitude - markerLatLng.latitude);
				double longap = Math.abs(centerLatLng.longitude - markerLatLng.longitude);

				double distance = latgap + longap;

				if (minDistance > distance) {
					minDistance = distance;
					nearMarker = markerList.get(i);
				}
			}

			searchBundle.putString(SearchStationFragment.KEY_TAB_SELECTION, nearMarker.getTitle());
			nearMarker.showInfoWindow();
		}

	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				OnCommunicationActivity communication = (OnCommunicationActivity) getActivity();
				communication.OnTabMove(MainActivity.MyTabs.STATION_LISTVIEW, searchBundle);
			}

		}.execute();
	}
}
