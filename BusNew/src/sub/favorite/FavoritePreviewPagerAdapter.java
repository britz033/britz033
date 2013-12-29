package sub.favorite;

import java.io.File;

import adapter.OnCommunicationActivity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.R;

/*
 * 프리뷰 미리보기 페이져 어뎁터
 * 현재는 일단 글자만 표현
 */
public class FavoritePreviewPagerAdapter extends PagerAdapter {

	private static final String TAG = "FavoritePreviewPagerAdapter";

	private Context context;
	private Cursor cursor;
	private LayoutInflater inflater;
	private FavoriteDummyPagerAdapter dummy;
	private boolean startSetting;
	private ImageView ivInfo;

	public FavoritePreviewPagerAdapter(Context context, Cursor c) {
		this.context = context;
		this.cursor = c;
		// if (cursor != null)
		// cursor.moveToFirst(); // 바깥에서 이미 커서를 움직일지도 모르므로 그냥 무조건 첫번째로

		inflater = LayoutInflater.from(context);
		startSetting = false;

	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == (View) object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		if (cursor == null)
			return null;

		RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.viewpager_favorite_preview, null);

		if (!startSetting) {
			ImageView iv = (ImageView) rl.findViewById(R.id.img_favorite_preview);
			ivInfo = iv;

			cursor.moveToPosition(position);

			StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().toString()).append("/android/data/").append(context.getPackageName())
					.append("/preview/").append(cursor.getString(0)).append(".png");
			File preImageFile = new File(sb.toString());
			if (preImageFile.exists()) {
				BitmapDrawable bd = new BitmapDrawable(preImageFile.getAbsolutePath());
				iv.setImageDrawable(bd);
			} else {
				iv.setImageDrawable(context.getResources().getDrawable(R.drawable.station00005));
			}

		} else {
			Button btn = (Button) rl.findViewById(R.id.imgbutton_favorite_preview_empty);
			ImageView iv = (ImageView) rl.findViewById(R.id.img_favorite_preview);
			iv.setVisibility(View.INVISIBLE);
			btn.setVisibility(View.VISIBLE);

			btn.setOnClickListener(new OnClickListener() {
				OnCommunicationActivity activityCall;

				@Override
				public void onClick(View v) {
					activityCall = (MainActivity) context;
					activityCall.OnTabMove(MainActivity.MyTabs.STATION_LISTVIEW, null);
				}
			});
		}
		container.addView(rl, 0);
		return rl;
	}
	
	public ImageView getImageView(){
		return ivInfo;
	}
	
	@Override
	public int getCount() {
		if (cursor == null)
			return 0;
		if (cursor.getCount() == 0) {
			startSetting = true;
			return 1;
		} else
			startSetting = false;

		return cursor.getCount();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (cursor == null)
			return null;
		if (startSetting)
			return "정류장 즐겨찾기를 추가하세요";
		cursor.moveToPosition(position);
		return cursor.getString(1);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		View view = (View)object;
		((ViewPager)container).removeView(view);
		view = null;
	}

	public void swapCursor(Cursor cursor) {
		if (this.cursor == cursor)
			return;
		if (cursor != null)
			Log.d(TAG, String.valueOf(cursor.getCount()));
		this.cursor = cursor;
		dummy.swapAdapter(this);
		notifyDataSetChanged();
	}

	public void setDummy(FavoriteDummyPagerAdapter dummy) {
		this.dummy = dummy;
	}

}
