package subfragment;

import internet.BusInfo;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zoeas.qdeagubus.R;

/*
 * 즐겨찾기의 정류소별 전광판 정보를 리스트뷰로 뿌림
 */
public class FavoriteFragmentBusList extends ListFragment {
	
	private Context context;
	private ArrayList<BusInfo> list;
	private String stationName;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		context = getActivity();
		
		Bundle data = getArguments();
		String error = data.getString(FavoriteFragment.KEY_ERROR);
		list = data.getParcelableArrayList(FavoriteFragment.KEY_BUSINFO_LIST);
		stationName = data.getString(FavoriteFragment.KEY_STATION_NAME);
		
		if(error == null)
			setListAdapter(new BusListAdapter());
		else {
			setListAdapter(null);
			setEmptyText(error);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		String busNum =list.get(position).getBusNum();
		Intent intent = new Intent(context, BusInfoActivity.class);
		intent.putExtra(BusInfoActivity.KEY_BUS_INFO, busNum);
		intent.putExtra(BusInfoActivity.KEY_CURRENT_STATION_NAME,stationName);
		startActivity(intent);
	}

	class BusListAdapter extends BaseAdapter {
		
		class ViewHolder {
			TextView tv;
		}

		@Override
		public int getCount() {
			return list.size();
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
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.list_favorite_bus_item, null);
				holder.tv = (TextView) convertView.findViewById(R.id.text_favorite_list_busitem_busnumber);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			SpannableStringBuilder ssb = new SpannableStringBuilder();
			
			float density = context.getResources().getDisplayMetrics().density;
			ssb.append(list.get(position).getSpannableStringBusInfo(density));
			
			holder.tv.setText(ssb);
			
			
			return convertView;
		}

	}
}
