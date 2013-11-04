package subfragment;

import util.StationTableConstants;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

//리스트뷰에 들어갈 어뎁터
class MyCursorAdapter extends CursorAdapter {

	Context mcontext;

	static class ViewHolder {
		TextView tvNumber;
		TextView tvName;
		ImageButton ibFavorite;
	}

	public MyCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mcontext = context;
	}

	final OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// 클릭된 아이템의 위치를 반환한다
			int position = (Integer) v.getTag();
			Cursor mcursor = MyCursorAdapter.this.getCursor();
			
			// 현재 뿌려진(존재하는.. 예를들어 검색해서 20개의 결과물이 나왔으면 20개만 있음) 커서의 위치로 이동한다.
			mcursor.moveToPosition(position);
			
			// 그 위치의 아이템이 즐겨찿기가 되어 있는 여부를 저장한다
			int favorite = mcursor
					.getInt(StationTableConstants.STATION_FAVORITE_STATION);
			int id = mcursor.getInt(StationTableConstants.STATION_ID);
			
			Log.d("이름",mcursor.getString(StationTableConstants.STATION_NAME));
			Log.d("id",String.valueOf(mcursor.getInt(0)));

			updateFavorite(favorite, id);
		}
	};

	// 즐겨찾기 변경
	private void updateFavorite(int favorite, int id) {
		ContentValues value = new ContentValues();

		if (favorite == 0) {
			value.put("station_favorite_station", String.valueOf(1));
		} else {
			value.put("station_favorite_station", String.valueOf(0));
		}

		// 아이디 값을 기준으로 업데이트
		Uri singleUri = ContentUris.withAppendedId(
				MyContentProvider.CONTENT_URI, id);

		mcontext.getContentResolver().update(singleUri, value, null, null);
		
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder holder = (ViewHolder) view.getTag();

		holder.ibFavorite.setOnClickListener(listener);
		holder.tvNumber.setText(cursor
				.getString(StationTableConstants.STATION_NUMBER));
		holder.tvName.setText(cursor
				.getString(StationTableConstants.STATION_NAME));

		if (cursor.getInt(StationTableConstants.STATION_FAVORITE_STATION) == 0) {
			holder.ibFavorite
					.setImageResource(R.drawable.btn_station_list_item_off_selector);
		} else {
			holder.ibFavorite
					.setImageResource(R.drawable.btn_station_list_item_on_selector);
		}

		// 현재 뿌려진 Cursor 상태에서 몇번째 위치인가를 반환한다. 
		holder.ibFavorite.setTag(Integer.valueOf(cursor.getPosition()));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.list_item_layout, null);

		ViewHolder holder = new ViewHolder();
		holder.tvNumber = (TextView) view
				.findViewById(R.id.text_station_item_number);
		holder.tvName = (TextView) view
				.findViewById(R.id.text_station_item_name);
		holder.ibFavorite = (ImageButton) view
				.findViewById((R.id.btn_station_item_favorite));

		view.setTag(holder);

		return view;
	}
}
