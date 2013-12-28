package subfragment;

import util.LoopQuery;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class Test2 extends ListFragment implements OnBackAction, LoaderCallbacks<Cursor> {
	
	private static final String TAG = "Test2";

	LoopQuery<String> lq;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		String[] a = {"3000653000"};
		lq = new LoopQuery<String>(getLoaderManager(), a, this);
		lq.start();
		setListAdapter(new MyAdapter(getActivity()));
		return inflater.inflate(R.layout.test2, container, false);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getListView().setItemChecked(3, true);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClear() {
		// TODO Auto-generated method stub

	}

	// @Override
	// public Animation onCreateAnimation(int transit, boolean enter, int
	// nextAnim) {
	// if(enter){
	// return AnimationUtils.loadAnimation(getActivity(),
	// R.animator.slide_open);
	// } else {
	// return AnimationUtils.loadAnimation(getActivity(),
	// R.animator.slide_close);
	// }
	// }

	class MyAdapter extends BaseAdapter {

		private Context context;
		private float density;

		public MyAdapter(Context context) {
			this.context = context;
			density = context.getResources().getDisplayMetrics().density;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 7;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Animation ani = AnimationUtils.loadAnimation(getActivity(), R.animator.slide_open);
			ani.setStartOffset(50 * position);
			
			View view = LayoutInflater.from(context).inflate(R.layout.test2_list_item, null);
			if(position%2==0){
				view.setBackgroundColor(getResources().getColor(R.color.color_test));
			} else {
				view.setBackgroundColor(getResources().getColor(R.color.color_test2));
			}
			
			TextView tv = (TextView) view.findViewById(R.id.text_listitem_test2);
			tv.setText("아야이야오ㅑㅐㅣㅇ");
			tv.setTextSize(12 * density);
			tv.startAnimation(ani);
			return view;
		}

	}


	@Override
	public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
		data.moveToFirst();
		Log.d(TAG, data.getString(0));
		if(!lq.isEnd())
			lq.restart();
	}

	@Override
	public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = MyContentProvider.CONTENT_URI_BUS;
		String[] projection = {"_id","bus_number","bus_id"};
		String selection =  "bus_id=" + args.getString(LoopQuery.KEY);
		return new CursorLoader(getActivity(), uri, projection, selection, null, null);
	}
}
