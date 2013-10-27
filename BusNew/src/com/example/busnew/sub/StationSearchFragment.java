package com.example.busnew.sub;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import com.example.busnew.MyContentProvider;
import com.example.busnew.R;
import com.google.android.gms.maps.model.LatLng;

public class StationSearchFragment extends ListFragment implements LoaderCallbacks<Cursor>,OnKeyListener{
	
	private SimpleCursorAdapter madapter;
	private EditText et;
	private Context context;
	
	public interface OnBusStationInfoListener{
		public void OnBusStationInfo(String station_number, String station_name, LatLng latLng);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_station_layout, null);
		et = (EditText) view.findViewById(R.id.edit_search_sub2fragment);
		et.addTextChangedListener(new MyWatcher());
		et.setOnKeyListener(this); 
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String[] from = {"station_name","station_number"};
		int[] to = {android.R.id.text1, android.R.id.text2};
		
		madapter = new SimpleCursorAdapter(context, android.R.layout.simple_expandable_list_item_2, null, from, to, 0);
		setListAdapter(madapter);
		
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = madapter.getCursor();
		c.moveToPosition(position);
		
		String station_name = c.getString(1); 
		String station_number = c.getString(2); 
		double station_latitude = c.getDouble(3); 
		double station_longitude = c.getDouble(4); 
		
//		int station_pic = context.getResources().getIdentifier("R.drawable.station_" + station_number, "drawble", context.getPackageName());
//		if(station_pic == 0){
//			station_pic = R.drawable.station_00001;
//		}
//		
//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//		ImageView iv = new ImageView(context);
//		iv.setImageResource(station_pic);
//		builder.setView(iv);
//		builder.create().show();
		
		OnBusStationInfoListener saver = (OnBusStationInfoListener) context;
		saver.OnBusStationInfo(station_number, station_name, new LatLng(station_latitude, station_longitude));
	}
	
	class MyWatcher implements TextWatcher{

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// 데이터베이스 검색
			Bundle search = new Bundle();
			search.putString("key", s.toString());
			Log.d("검색어",s.toString());
			getLoaderManager().restartLoader(0, search, StationSearchFragment.this);
		}
		
	}
	
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri baseUri = MyContentProvider.CONTENT_URI;
		
		// _id 안넣으면 에러 슈바
		String[] projection = {"_id","station_number","station_name","station_latitude","station_longitude"};
		String selection = null;
		if(args != null){
				selection = "station_name like '%" + args.getString("key") +"%'";
		}
		
		return new CursorLoader(getActivity(), baseUri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		madapter.swapCursor(cursor);
		
		// 에러남.. 커스텀 리스트뷰 레이아웃에는 안되나봄.. 
//		if(isResumed()){
//			setListShown(true);
//		} else {
//			setListShownNoAnimation(true);
//		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		madapter.swapCursor(null);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			return true;
		}
			
		return false;
	}

}
