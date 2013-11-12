package adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.zoeas.qdeagubus.R;

/*
 * 프리뷰 미리보기 페이져 어뎁터
 * 현재는 일단 글자만 표현
 */
public class FavoritePreviewPagerAdatper extends PagerAdapter {
	
	private Context context;
	private Cursor cursor;
	private Drawable img;
	private LayoutInflater inflater;
	
	public FavoritePreviewPagerAdatper(Context context, Cursor c){
		this.context = context;
		this.cursor = c;
		cursor.moveToFirst();  // 바깥에서 이미 커서를 움직일지도 모르므로 그냥 무조건 첫번째로
		
		inflater = LayoutInflater.from(context);
		
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == (View)object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		cursor.moveToPosition(position);
		
		RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.viewpager_favorite_preview, null);
		ImageView iv = (ImageView) rl.findViewById(R.id.img_favorite_preview);
		
		StringBuilder sb = new StringBuilder("station");
		sb.append(cursor.getString(0));
		int id = context.getResources().getIdentifier(sb.toString(), "drawable", context.getPackageName());
		if(id == 0)
			id = R.drawable.station00005;
		img = context.getResources().getDrawable(id); 
		
		iv.setImageDrawable(img);
		
		
		container.addView(rl,0);
		
		return rl;
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
		container.removeView((RelativeLayout) object);
	}

}
