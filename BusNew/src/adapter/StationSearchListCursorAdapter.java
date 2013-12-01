package adapter;

import subfragment.SearchStationFragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

//리스트뷰에 들어갈 어뎁터
public class StationSearchListCursorAdapter extends CursorAdapter implements OnTouchListener {

	private Context mcontext;
	private OnCommunicationActivity communication; // 즐겨찾기 되면 액티비티에 구현한 리스너가
													// 자동호출

	static class ViewHolder {
		TextView tvNumber;
		TextView tvName;
		ImageButton ibFavorite;
	}

	public StationSearchListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mcontext = context;
		communication = (OnCommunicationActivity) mcontext;
	}

	final OnClickListener listener = new OnClickListener() {
		@Override
		public synchronized void onClick(View v) {
			// 클릭된 아이템의 위치를 반환한다
			int position = (Integer) v.getTag();
			Cursor mcursor = StationSearchListCursorAdapter.this.getCursor();

			// 현재 뿌려진(존재하는.. 예를들어 검색해서 20개의 결과물이 나왔으면 20개만 있음) 커서의 위치로 이동한다.
			mcursor.moveToPosition(position);

			// 그 위치의 아이템이 즐겨찿기가 되어 있는 여부를 저장한다
			int favorite = mcursor.getInt(SearchStationFragment.STATION_FAVORITE_INDEX);
			int id = mcursor.getInt(SearchStationFragment.STATION_ID_INDEX);

			updateFavorite(favorite, id);
			communication.OnFavoriteRefresh();
		}
	};

	// 즐겨찾기 변경
	private void updateFavorite(int favorite, int id) {
		ContentValues value = new ContentValues();

		if (favorite == 0) {
			value.put("station_favorite", 1);
		} else {
			value.put("station_favorite", 0);
		}

		// 아이디 값을 기준으로 업데이트
		Uri singleUri = ContentUris.withAppendedId(MyContentProvider.CONTENT_URI_STATION, id);

		Log.d("정류장검색 커서어뎁터","즐겨찾기 업데이트 호출됨");
		mcontext.getContentResolver().update(singleUri, value, null, null);

	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder holder = (ViewHolder) view.getTag();

		holder.ibFavorite.setOnClickListener(listener);
		holder.tvNumber.setText(cursor.getString(SearchStationFragment.STATION_NUMBER_INDEX));
		holder.tvName.setText(cursor.getString(SearchStationFragment.STATION_NAME_INDEX));

		if (cursor.getInt(SearchStationFragment.STATION_FAVORITE_INDEX) == 0) {
			holder.ibFavorite.setImageResource(R.drawable.btn_station_list_item_off_selector);
		} else {
			holder.ibFavorite.setImageResource(R.drawable.btn_station_list_item_on_selector);
		}

		// 현재 뿌려진 Cursor 상태에서 몇번째 위치인가를 반환한다.
		holder.ibFavorite.setTag(Integer.valueOf(cursor.getPosition()));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.list_favorite_station_item, null);

		ViewHolder holder = new ViewHolder();
		holder.tvNumber = (TextView) view.findViewById(R.id.text_station_item_number);
		holder.tvName = (TextView) view.findViewById(R.id.text_station_item_name);
		holder.ibFavorite = (ImageButton) view.findViewById((R.id.btn_station_item_favorite));

		view.setTag(holder);

		return view;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("정류장리스트터치","호출되었습니다");
		InputMethodManager imm = (InputMethodManager) mcontext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return false;
	}
}
