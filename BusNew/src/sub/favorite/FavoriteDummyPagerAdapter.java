package sub.favorite;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FavoriteDummyPagerAdapter extends PagerAdapter{
	
	private FavoritePreviewPagerAdatper adapter;
	private Context context;
	
	public FavoriteDummyPagerAdapter(FavoritePreviewPagerAdatper adapter, Context context){
		this.adapter = adapter;
		this.context = context;
	}

	@Override
	public int getCount() {
		return adapter.getCount();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view==object;
	}
	
	
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		TextView tv = new TextView(context);
		container.addView(tv);
		return tv;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return adapter.getPageTitle(position);
	}
	
	public void swapAdapter(FavoritePreviewPagerAdatper adapter){
		this.adapter = adapter;
		notifyDataSetChanged();
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((TextView)object);
	}

}
