package sub.search.station;

import adapter.OnCommunicationActivity;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

//리스트뷰에 들어갈 어뎁터
public class StationSearchListCursorAdapter extends CursorAdapter {

	private Context mcontext;
	private OnCommunicationActivity communication; // 즐겨찾기 되면 액티비티에 구현한 리스너가
													// 자동호출
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
		Log.d("아이템클릭 리사이즈","불려짐");
		dummyHeight = height;
//		notifyDataSetChanged();
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
        Log.d("get View 호출됨",""+dummyHeight);
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

		// 현재 뿌려진 Cursor 상태에서 몇번째 위치인가를 반환한다.
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
//		LayoutParams params = holder.dummy.getLayoutParams();
//		params.height = dummyHeight;
//		Log.d("New View 호출됨",""+dummyHeight);
//		holder.dummy.setLayoutParams(params);
		
		view.setTag(holder);

		return view;
	}
	
}
