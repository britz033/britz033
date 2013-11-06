package adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.zoeas.qdeagubus.R;

/*
 * 프리뷰 미리보기 페이져 어뎁터
 * 현재는 일단 글자만 표현
 */
public class FavoritePreviewPagerAdatper extends PagerAdapter {
	
	private Context context;
	private Cursor cursor;
	private ArrayList<String> title;
	private int height;
	private int width;
	
	public FavoritePreviewPagerAdatper(Context context, Cursor c){
		this.context = context;
		this.cursor = c;
		cursor.moveToFirst();  // 바깥에서 이미 커서를 움직일지도 모르므로 그냥 무조건 첫번째로
		float density = context.getResources().getDisplayMetrics().density;
		height = (int)(20 * density);
		width = (int)(20*density);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		cursor.moveToPosition(position);
		
		StringBuilder sb = new StringBuilder("station");
		sb.append(cursor.getString(0));
		
		LayoutParams params = new LayoutParams(width, height);
		ImageView iv = new ImageView(context);
		iv.setLayoutParams(params);
		int id = context.getResources().getIdentifier(sb.toString(), "drawable", context.getPackageName());
		if(id == 0)
			id = R.drawable.station00005;
		
		iv.setImageResource(id);
		container.addView(iv);
		
		return iv;
	}
	

	@Override
	public int getCount() {
		return cursor.getCount();
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		cursor.moveToPosition(position);
		return cursor.getString(1);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((ImageView) object);
	}

}
