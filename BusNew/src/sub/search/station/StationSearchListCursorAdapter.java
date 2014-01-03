package sub.search.station;

import adapter.OnCommunicationActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class StationSearchListCursorAdapter extends CursorAdapter {
	
	private Context mcontext;
	private OnCommunicationActivity communication; 
													
	private int lastAnimatedPosition;
	private int lastPosition;
	private int dummyHeight;

	static class ViewHolder {
		TextView tvNumber;
		TextView tvName;
		View dummy;
		ImageButton ibFavorite;
	}

	public StationSearchListCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		mcontext = context;
		communication = (OnCommunicationActivity) mcontext;
		lastAnimatedPosition = 0;
		dummyHeight = 0;
	}
	
	public void setDummyHeight(int height){
		dummyHeight = height;
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		if(newCursor != null)
			lastPosition = newCursor.getCount()-1;
		return super.swapCursor(newCursor);
	}



	final OnClickListener listener = new OnClickListener() {
		@Override
		public synchronized void onClick(View v) {
			int position = (Integer) v.getTag();
			Cursor mcursor = StationSearchListCursorAdapter.this.getCursor();

			mcursor.moveToPosition(position);

			int favorite = mcursor.getInt(SearchStationFragment.STATION_FAVORITE_INDEX);
			int id = mcursor.getInt(SearchStationFragment.STATION_ID_INDEX);

			updateFavorite(favorite, id);
			communication.OnFavoriteRefresh();
		}
	};
	

	// 즐겨찾기 변경
	private void updateFavorite(int favorite, int id) {
		ContentValues cv = new ContentValues();

		if (favorite == 0) {
			cv.put("station_favorite", 1);
		} else {
			cv.put("station_favorite", 0);
		}
		
		SQLiteDatabase db = SQLiteDatabase.openDatabase(mcontext.getDatabasePath("StationDB.png").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
		db.update(MyContentProvider.TABLE_NAME_STATION, cv, "_id=" + id, null);
		db.close();

	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        
        if(lastAnimatedPosition < position){
        	Animator a = new ObjectAnimator().ofFloat(v,"alpha",0,1);
    		a.setDuration(1000);
    		a.start();
    		lastAnimatedPosition = position;
        }
        if(lastPosition == position){
        	((ViewHolder)v.getTag()).dummy.setVisibility(View.VISIBLE);
        } else {
        	((ViewHolder)v.getTag()).dummy.setVisibility(View.GONE);
        }
        
        return v;
    }

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder holder = (ViewHolder) view.getTag();

		holder.ibFavorite.setOnClickListener(listener);
		holder.tvNumber.setText(cursor.getString(SearchStationFragment.STATION_NUMBER_INDEX));
		holder.tvName.setText(cursor.getString(SearchStationFragment.STATION_NAME_INDEX));
		
		LayoutParams params = holder.dummy.getLayoutParams();
		params.height = dummyHeight;
		holder.dummy.setLayoutParams(params);

		if (cursor.getInt(SearchStationFragment.STATION_FAVORITE_INDEX) == 0) {
			holder.ibFavorite.setImageResource(R.drawable.btn_station_list_item_off_selector);
		} else {
			holder.ibFavorite.setImageResource(R.drawable.btn_station_list_item_on_selector);
		}

		holder.ibFavorite.setTag(Integer.valueOf(cursor.getPosition()));
	}
	
	public void resetAnimatedPosition(){
		lastAnimatedPosition = 0;
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.list_favorite_station_item, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.tvNumber = (TextView) view.findViewById(R.id.text_station_item_number);
		holder.tvName = (TextView) view.findViewById(R.id.text_station_item_name);
		holder.ibFavorite = (ImageButton) view.findViewById((R.id.btn_station_item_favorite));
		holder.dummy = view.findViewById((R.id.dummy_station_item_favorite));
		
		view.setTag(holder);

		return view;
	}
	
}
