package subfragment;

import adapter.StationSearchListCursorAdapter;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import com.google.android.gms.maps.model.LatLng;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class StationSearchFragment extends ListFragment implements LoaderCallbacks<Cursor>,OnKeyListener{
	
	private StationSearchListCursorAdapter madapter;
	private EditText et;
	private Context context;
	
//	public interface OnBusStationInfoListener{
//		public void OnBusStationInfo(String station_number, String station_name, LatLng latLng);
//		public void OnBusStationInfo(String station_number, String station_name);
//	}
	
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
		
		//어뎁터 생성등록 커서는 없음.. 로더에서 추가
		madapter = new StationSearchListCursorAdapter(context, null, 0);
		setListAdapter(madapter);
		getLoaderManager().initLoader(0, null, this);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = madapter.getCursor();
		c.moveToPosition(position);
		
		String station_number = c.getString(MyContentProvider.STATION_NUMBER_INDEX); 
		String station_name = c.getString(MyContentProvider.STATION_NAME_INDEX); 
		double station_longitude = c.getDouble(MyContentProvider.STATION_LONGITUDE_INDEX); 
		double station_latitude = c.getDouble(MyContentProvider.STATION_LATITUDE_INDEX); 
		
		OnSaveBusStationInfoListener saver = (OnSaveBusStationInfoListener) context;
		saver.OnSaveBusStationInfo(station_number, station_name, new LatLng(station_latitude, station_longitude));
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
			// 데이터베이스 검색 하여 리스트뷰 새로 뿌림
			Bundle search = new Bundle();
			search.putString("key", s.toString());
			getLoaderManager().restartLoader(0, search, StationSearchFragment.this);
		}
		
	}
	
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri baseUri = MyContentProvider.CONTENT_URI;
		
		// _id 안넣으면 에러 슈바
		String[] projection = {"_id","station_number","station_name","station_longitude","station_latitude","station_favorite"};
		String selection = null;
		if(args != null){
				selection = "station_name like '%" + args.getString("key") +"%'";
		}
		
		return new CursorLoader(getActivity(), baseUri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// 앞서 생성된 커서를 받아옴
		madapter.swapCursor(cursor);
		
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
