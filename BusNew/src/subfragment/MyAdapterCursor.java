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
			int position = (Integer) v.getTag();
			Cursor mcursor = MyCursorAdapter.this.getCursor();
			mcursor.moveToPosition(position);
			int favorite = mcursor
					.getInt(StationTableConstants.STATION_FAVORITE_STATION);

			updateFavorite(favorite, position + 1);
		}
	};

	private void updateFavorite(int favorite, int position) {
		ContentValues value = new ContentValues();

		if (favorite == 0) {
			value.put("station_favorite_station", String.valueOf(1));
		} else {
			value.put("station_favorite_station", String.valueOf(0));
		}

		Log.d("update comple", value.toString());
		Log.d("update comple", String.valueOf(position));
		
		Uri singleUri = ContentUris.withAppendedId(
				MyContentProvider.CONTENT_URI, position);

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
