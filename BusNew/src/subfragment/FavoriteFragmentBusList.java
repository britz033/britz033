package subfragment;

import internet.BusInfoNet;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils.StringSplitter;
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
	private ArrayList<BusInfoNet> netList;
	private ArrayList<BusInfo> busList;
	private String stationName;
	private float density;
	private int netSize;
	private int busSize;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		context = getActivity();
		density = context.getResources().getDisplayMetrics().density;

		Bundle data = getArguments();
		String error = data.getString(FavoriteFragment.KEY_ERROR);
		netList = data.getParcelableArrayList(FavoriteFragment.KEY_BUS_NET_INFO_LIST);
		busList = data.getParcelableArrayList(FavoriteFragment.KEY_BUS_INFO_LIST);
		stationName = data.getString(FavoriteFragment.KEY_STATION_NAME);

		if (error == null)
			if (busList.size() != 0) {
				if (netList == null) {
					netSize = 0;
					busSize = busList.size();
				} else {
					netSize = netList.size();
					bindInfo();
				}

				setListAdapter(new BusListAdapter());
			} else {
				setListAdapter(null);
				setEmptyText("현재 버스정보가 없는 정류소입니다");
			}
		else if(error.equals("0")){ // 버스가 끊김. 버스목록만 보여줌
			netSize = 0;
			busSize = busList.size();
			
			setListAdapter(new BusListAdapter());
		} else {
			setListAdapter(null);
			setEmptyText(error);
		}

	}

	/*
	 * <임시> 인터넷에서 가져온 정보에 id와 favorite 추가 추가시마다 본래 리스트껀 제거 리스트뷰엔 net에서 가져온거 상단에 다 뿌린후.. 본래꺼에서 남는것들 뿌림
	 */
	private void bindInfo() {
		for (int i = 0; i < netList.size(); i++) {
			for (int j = 0; j < busList.size(); j++) {
				// 완전일치시
				BusInfoNet netInfo = netList.get(i);
				if (netInfo.getBusNum().equals(busList.get(j).getBusName())) {
					// id부여
					netInfo.setBusId(busList.get(j).getBusId());
					netInfo.setFavorite(busList.get(j).getBusFavorite());
					busList.remove(j);
					break;
				}
			}
		}

		busSize = busList.size();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String busName = null;
		String busId = null;
		// 클릭시 보내는 정보를 부정확한 이름조합에서 정확한 버스Id로 교체할 예정
		if (position < netSize) {
			busId = netList.get(position).getBusId();
			busName = netList.get(position).getBusNum();
		} else {
			busId = busList.get(position - netSize).getBusId();
			busName = busList.get(position - netSize).getBusName();
		}

		Intent intent = new Intent(context, BusInfoActivity.class);
		intent.putExtra(BusInfoActivity.KEY_BUS_ID, busId);
		intent.putExtra(BusInfoActivity.KEY_BUS_NAME, busName);
		intent.putExtra(BusInfoActivity.KEY_CURRENT_STATION_NAME, stationName);
		startActivity(intent);
	}

	class BusListAdapter extends BaseAdapter {

		class ViewHolder {
			TextView tv;
		}

		@Override
		public int getCount() {
			return netSize + busSize;
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

			if (position < netSize) {
				SpannableStringBuilder ssb = netList.get(position).getSpannableStringBusInfo(density);
				holder.tv.setText(ssb);
			} else {
				holder.tv.setText(busList.get(position - netSize).getBusName());
			}

			return convertView;
		}

	}
}
