package subfragment;

import internet.BusInfo;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zoeas.qdeagubus.R;

public class FavoriteFragmentBusList extends ListFragment {
	
	Context context;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		context = getActivity();
		
		Bundle data = getArguments();
		String error = data.getString(FavoriteFragment.KEY_ERROR);
		ArrayList<BusInfo> list = data.getParcelableArrayList(FavoriteFragment.KEY_LIST);
		
		if(error == null)
			setListAdapter(new BusListAdapter(list));
		else 
			setEmptyText(error);
	}

	class BusListAdapter extends BaseAdapter {
		ArrayList<BusInfo> list;
		
		public BusListAdapter(ArrayList<BusInfo> list){
			this.list = list;
		}
		
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
			View view;
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(context);
				view = inflater.inflate(R.layout.list_favorite_bus_item, null);
				holder.tv = (TextView) view.findViewById(R.id.text_favorite_list_busitem_busnumber);
				view.setTag(holder);
			} else {
				view = convertView;
				holder = (ViewHolder) view.getTag();
			}
			
			SpannableStringBuilder ssb = new SpannableStringBuilder();
			for (BusInfo bus : list) {
				ssb.append(bus.getSpannableStringBusInfo());
			}
			
			holder.tv.setText(ssb);
			
			
			return view;
		}

	}
}
