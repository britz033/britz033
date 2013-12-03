package adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SlidingMenuAdapter extends BaseAdapter implements OnItemClickListener{

	// 버스이름, id 순서
	private Context context;
	private ArrayList<String[]> data;

	public SlidingMenuAdapter(Context context, ArrayList<String[]> data) {
		this.context = context;
		this.data = data;
		Collections.sort(data, new Comparator<String[]>() {
			@Override
			public int compare(String[] lhs, String[] rhs) {
				return lhs[0].compareTo(rhs[0]);
			}
		});
	}

	@Override
	public int getCount() {
		return data.size();
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
		TextView tv = null;
		
		if (convertView == null) {
			tv = new TextView(context);
		} else {
			tv = (TextView) convertView;
		}
		
		tv.setText(data.get(position)[0]);
		
		return tv;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(context, data.get(position)[1], 0).show();
	}

}
