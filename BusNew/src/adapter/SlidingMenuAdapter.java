package adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import businfo.activity.BusInfoActivity;

import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.R;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
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
		
		RelativeLayout rl = null;
		TextView tv = null;
		
		if (convertView == null) {
			rl = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.sliding_item,null);
		} else {
			rl = (RelativeLayout) convertView;
		}
		
		
		tv = (TextView) rl.findViewById(R.id.text_sliding_menu);
		tv.setText(data.get(position)[0]);
		
		if(position%2==0){
			rl.setBackgroundColor(context.getResources().getColor(R.color.color_blueWhite));
		} else {
			rl.setBackgroundColor(context.getResources().getColor(R.color.color_blueDark));
		}
		
		return rl;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MainActivity.backAction.init();
		Intent intent = new Intent(context, BusInfoActivity.class);
		intent.putExtra(BusInfoActivity.KEY_BUS_ID, data.get(position)[1]);
		intent.putExtra(BusInfoActivity.KEY_BUS_NAME, data.get(position)[0]);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		context.startActivity(intent);
	}

}
