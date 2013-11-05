package adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zoeas.qdeagubus.MyContentProvider;

/*
 * 프리뷰 미리보기 페이져 어뎁터
 * 현재는 일단 글자만 표현
 */
public class FavoritePreviewPagerAdatper extends PagerAdapter {
	
	private Context context;
	private Cursor cursor;
	
	public FavoritePreviewPagerAdatper(Context context, Cursor c){
		this.context = context;
		this.cursor = c;
		cursor.moveToFirst();  // 바깥에서 이미 커서를 움직일지도 모르므로 그냥 무조건 첫번째로
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		cursor.moveToPosition(position);
		TextView tv = new TextView(context);
		tv.setText(cursor.getString(1));
		container.addView(tv);
		Log.d("moveToNext 후","ok");
		return tv;
	}

	@Override
	public int getCount() {
		return cursor.getCount();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((TextView) object);
	}

}
