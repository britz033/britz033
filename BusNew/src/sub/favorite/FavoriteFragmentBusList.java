package sub.favorite;

import internet.BusInfoNet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import sub.search.bus.SearchBusNumberFragment;
import adapter.OnCommunicationActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import businfo.activity.BusInfoActivity;

import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.R;

/**
 * 즐겨찾기의 정류소별 전광판 정보를 리스트뷰로 뿌림
 * 
 * @author lol
 * 
 */
public class FavoriteFragmentBusList extends ListFragment {

	private Context context;
	private ArrayList<BusInfoNet> netList;
	private ArrayList<BusInfo> busList;
	private ArrayList<BusInfo> busListCopy;
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

		busListCopy = (ArrayList<BusInfo>) busList.clone();

		if (error == null)
			if (busListCopy.size() != 0) {
				if (netList == null) {
					netSize = 0;
					busSize = busListCopy.size();
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
			busSize = busListCopy.size();

			setListAdapter(new BusListAdapter());
		} else {
			setListAdapter(null);
			setEmptyText(error);
		}

	}

	/**
	 * 제거 체크가 되어 있는지 확인하고 체크되어 있으면 체크된 것을 제거하고 남은 것들로 보여주기를 시작한다 이때 전광판에서 정보가 오지
	 * 않은 것들은 회색으로 뿌리고 그외는 정보표시를 한다. 이때 전광판에서 정보와 버스리스트가 매치되지 않으면 전광판에 나타난 번호를
	 * 기준으로 버스리스트를 다 지워버린다 예를들어 300번이 있는데 버스리스트는 300(냥) 300(스파르타)라고 있으면 이 둘은
	 * 통합되어 있다보고 삭제시킨다 만약 300과 300(냥)이 전광판에 뜬다면 300(냥)과 300은 그냥 뜨고 300(스파르타) 가
	 * 삭제된다 혹시 300과 300(냥)과 300(스파르타)가 다 전광판에 나오는데 이번에만 300이 떠서 나머지가 삭제된 경우라고
	 * 치더라도 어쩔수 없다 다만 그렇게 삭제되더라도 다음에 매치시 나올테니 그걸로 위안 삼는다
	 * */
	private void bindInfo() {

		for (int k = 0; k < busListCopy.size(); k++) {
			if (busListCopy.get(k).getBusFavorite() == 0) {
				busListCopy.remove(k);
			}
		}

		HashMap<Integer, Integer> removeIndexHash = new HashMap<Integer, Integer>();

		// 완전일치시 net은 정보저장, busList는 제거
		// 불완전일치시 net은 이동하게, busList는 제거
		// 일치가 없을시 busList는 그냥 뿌림.
		// 리스트체커 제거시 net은 제거, busList는 이미 위에서 제거된 상태
		for (int i = 0; i < netList.size(); i++) {
			for (int j = 0; j < busListCopy.size(); j++) {

				BusInfoNet netInfo = netList.get(i);
				if (netInfo.getBusNum().equals(busListCopy.get(j).getBusNum())) {
					if (netInfo.getRoute().equals(busListCopy.get(j).getBusOption())) {
						// 완전 일치시
						netInfo.setBusId(busListCopy.get(j).getBusId());
						busListCopy.remove(j);
						break;
					} else {
						// 버스 이름만 일치시, net은 이동전용 id를 붙여주고, busList의 인덱스는 저장
						// 최종적으로 그 버스가 이름만 일치한다는게 판명되면 제거
						netInfo.setBusId("0");

					}

				}
			}
			// 번호도 없음 -> 앞서 favorite 리스트에서 제거된 것이므로 여기서도 필요없음
			if (netList.get(i).getBusId() == null) {
				netList.remove(i);
				Log.d("즐겨찾기 버스리스트", "전광판 정보가 하나 제거됨");
			}
		}

		busSize = busListCopy.size();
		netSize = netList.size();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		String busName = null;
		String busId = null;
		if (position < netSize) {
			busId = netList.get(position).getBusId();

			if (busId.equals("0")) {
				OnCommunicationActivity goBusSearch = (OnCommunicationActivity) getActivity();
				Bundle data = new Bundle();
				data.putString(SearchBusNumberFragment.SELECTION_KEY, netList.get(position).getBusNum());
				goBusSearch.OnTabMove(MainActivity.MyTabs.BUS_LISTVIEW, data);
				return;
			}
			busName = netList.get(position).getBusNum() + " " + netList.get(position).getRoute();
		} else {
			busId = busListCopy.get(position - netSize).getBusId();
			busName = busListCopy.get(position - netSize).getBusName();
		}

		Intent intent = new Intent(context, BusInfoActivity.class);
		intent.putExtra(BusInfoActivity.KEY_BUS_ID, busId);
		intent.putExtra(BusInfoActivity.KEY_BUS_NAME, busName);
		intent.putExtra(BusInfoActivity.KEY_CURRENT_STATION_NAME, stationName);
		startActivity(intent);
	}

	public void onDialogOpen() {
		AlertDialog ad = new AlertDialog.Builder(context).setTitle("보여질 버스를 체크하세요").setNeutralButton("확인", null)
				.setAdapter(new BusListCheckDialogAdapter(), null).create();
		ad.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ad.show();
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
				SpannableString sb = new SpannableString(busListCopy.get(position - netSize).getBusName());
				sb.setSpan(new ForegroundColorSpan(Color.argb(100, 0, 0, 0)), 0, sb.length(),
						SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.tv.setText(sb);
			}

			return convertView;
		}

	}

	class BusListCheckDialogAdapter extends BaseAdapter {


		@Override
		public int getCount() {
			return busList.size();
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
			
			RelativeLayout view;
			TextView tv;
			
			if (convertView == null) {
				float density = context.getResources().getDisplayMetrics().density;
				view = new RelativeLayout(context);
				tv = new TextView(context);
				
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				XmlResourceParser xrp = context.getResources().getXml(R.drawable.search_selector);
				Drawable background = null;
				try {
					background = Drawable.createFromXml(getResources(), xrp);
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				view.setBackgroundDrawable(background);
				view.setLayoutParams(params);
				tv.setLayoutParams(params);
				tv.setTextSize(15*density);
				int padding = (int)(12*density);
				tv.setPadding(padding, padding, padding, padding);
				
				view.setTag(tv);
				view.addView(tv);
			} else {
				view = (RelativeLayout) convertView;
			}
			
			tv = (TextView) view.getTag();
			tv.setText(busList.get(position).getBusName());

			return view;
		}

	}
}
