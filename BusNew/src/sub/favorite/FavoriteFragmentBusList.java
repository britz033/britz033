package sub.favorite;

import internet.BusInfoNet;

import java.util.ArrayList;

import sub.search.bus.SearchBusNumberFragment;

import adapter.OnCommunicationActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import businfo.activity.BusInfoActivity;

import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.R;

/**
 * 즐겨찾기의 정류소별 전광판 정보를 리스트뷰로 뿌림
 * @author lol
 *
 */
public class FavoriteFragmentBusList extends ListFragment{

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
					bindInfo();
				}

				setListAdapter(new BusListAdapter());
			} else {
				setListAdapter(null);
				setEmptyText("현재 버스정보가 없는 정류소입니다");
			}
		else if (error.equals("0")) { // 버스가 끊김. 버스목록만 보여줌
			netSize = 0;
			busSize = busList.size();

			setListAdapter(new BusListAdapter());
		} else {
			setListAdapter(null);
			setEmptyText(error);
		}

	}

	/**
	 * 바인드 aa
	 */
	private void bindInfo() {

		for (int k = 0; k < busList.size(); k++) {
			if (busList.get(k).getBusFavorite() == 0) {
				busList.remove(k);
			}
		}

		for (int i = 0; i < netList.size(); i++) {
			for (int j = 0; j < busList.size(); j++) {
				
				BusInfoNet netInfo = netList.get(i);
				if (netInfo.getBusNum().equals(busList.get(j).getBusNum())) {
					if (netInfo.getRoute().equals(busList.get(j).getBusOption())) {
						//완전 일치시
						netInfo.setBusId(busList.get(j).getBusId());
						busList.remove(j);
						break;
					}
					// 버스 이름만 일치시
					netInfo.setBusId("0");
				}
			}
			// 번호도 없음 -> 앞서 favorite 리스트에서 제거된 것이므로 여기서도 필요없음
			if(netList.get(i).getBusId() == null){
				netList.remove(i);
				Log.d("즐겨찾기 버스리스트", "전광판 정보가 하나 제거됨");
			}
		}

		busSize = busList.size();
		netSize = netList.size();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String busName = null;
		String busId = null;
		if (position < netSize) {
			busId = netList.get(position).getBusId();
			
			if(busId.equals("0")){
				OnCommunicationActivity goBusSearch = (OnCommunicationActivity) getActivity();
				Bundle data = new Bundle();
				data.putString(SearchBusNumberFragment.SELECTION_KEY, netList.get(position).getBusNum());
				goBusSearch.OnTabMove(MainActivity.MyTabs.BUS_LISTVIEW, data);
				return;
			}
			busName = netList.get(position).getBusNum() + " " + netList.get(position).getRoute();
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
				SpannableString sb = new SpannableString(busList.get(position - netSize).getBusName());
				sb.setSpan(new ForegroundColorSpan(Color.argb(100, 0, 0, 0)), 0, sb.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.tv.setText(sb);
			}

			return convertView;
		}

	}

}
