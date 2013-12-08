package adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zoeas.qdeagubus.R;

public class BusInfoStationSearchListAdapter extends BaseAdapter{
	
	private ArrayList<String> searchedStationList;
	private Context context;
	
	static class ViewHolder{
		TextView name;
	}

	public BusInfoStationSearchListAdapter(ArrayList<String> searchedStationList, Context context){
		this.searchedStationList = searchedStationList;
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return searchedStationList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		
		if(convertView == null){
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.list_favorite_station_item, parent, false);
			holder.name = (TextView)convertView.findViewById(R.id.text_station_item_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.name.setText(searchedStationList.get(position));
		
		return convertView;
	}

}
