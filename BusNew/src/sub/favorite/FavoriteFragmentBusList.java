package sub.favorite;

import internet.BusInfoNet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParserException;

import sub.search.bus.SearchBusNumberFragment;
import adapter.OnCommunicationActivity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import businfo.activity.BusInfoActivity;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

/**
 * 즐겨찾기의 정류소별 전광판 정보를 리스트뷰로 뿌림 busList 가 null 로 오는 것때문에 골치.. error 값부터 새로 만들어야할듯
 * 
 * @author lol
 * 
 */
public class FavoriteFragmentBusList extends ListFragment {

	private static final String TAG = "FavoriteFragmentBusList";

	private Context context;
	private ArrayList<BusInfoNet> netList;
	private ArrayList<BusInfo> busList;
	private ArrayList<BusInfo> busListCopy;
	private String stationName;
	private String stationID;
	private String passFavorite;
	private StringBuilder setFavorite;
	private float density;
	private int netSize;
	private int busSize;
	private SQLiteDatabase db;

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
		stationID = data.getString(FavoriteFragment.KEY_STATION_ID);
		passFavorite = data.getString(FavoriteFragment.KEY_PASS_FAVORITE);

		if (error == null) {
			busListCopy = (ArrayList<BusInfo>) busList.clone();

			if (busListCopy.size() != 0) {
				bindInfo();
				setFavorite = new StringBuilder(passFavorite);
				setListAdapter(new BusListAdapter());
			} else {
				setListAdapter(null);
				setEmptyText("현재 버스정보가 없는 정류소입니다");
			}
		} else {
			setListAdapter(null);
			setEmptyText(error);
		}
	}

	/**
	 * 제거 체크가 되어 있는지 확인하고 체크되어 있으면 체크된 것을 제거하고 남은 것들로 보여주기를 시작한다 이때 전광판에서 정보가 오지 않은 것들은 회색으로 뿌리고 그외는 정보표시를 한다. 이때 전광판에서 정보와 버스리스트가 매치되지 않으면 전광판에 나타난 번호를 기준으로 버스리스트를 다 지워버린다 예를들어
	 * 300번이 있는데 버스리스트는 300(냥) 300(스파르타)라고 있으면 이 둘은 통합되어 있다보고 삭제시킨다 만약 300과 300(냥)이 전광판에 뜬다면 300(냥)과 300은 그냥 뜨고 300(스파르타) 가 삭제된다 혹시 300과 300(냥)과 300(스파르타)가 다 전광판에 나오는데 이번에만 300이 떠서
	 * 나머지가 삭제된 경우라고 치더라도 어쩔수 없다 다만 그렇게 삭제되더라도 다음에 매치시 나올테니 그걸로 위안 삼는다
	 * */
	private void bindInfo() {

		// for (int k = 0; k < busListCopy.size(); k++) {
		// if (busListCopy.get(k).getBusFavorite() == 0) {
		// busListCopy.remove(k);
		// k--;
		// }
		// }

		if (passFavorite != null && passFavorite.length() == busListCopy.size()) {
			int index = 0;
			for (int k = 0; k < busListCopy.size(); k++) {
				if (passFavorite.charAt(index++) == '0') {
					busListCopy.remove(k);
					k--;
				}
			}
		} else {
			StringBuilder sb = new StringBuilder();
			for (int k = 0; k < busListCopy.size(); k++) {
				sb.append("1");
			}
			passFavorite = sb.toString();
		}

		if (netList == null) {
			netSize = 0;
			busSize = busListCopy.size();
			return;
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
						j--;
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
				netList.remove(i--);
				Log.d(TAG, "전광판 정보가 하나 제거됨");
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
		db = SQLiteDatabase.openDatabase(getActivity().getDatabasePath("StationDB.png").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

		View customTitleView = LayoutInflater.from(context).inflate(R.layout.layout_favorite_dialog_title, null);
		AlertDialog ad = new AlertDialog.Builder(context).setCustomTitle(customTitleView).setAdapter(new BusListCheckDialogAdapter(), null)
				.setNeutralButton("확인", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						((FavoriteFragment) getParentFragment()).refreshPreview();
						if (db != null && db.isOpen()) {
							db.close();
						}
					}
				}).create();
		final ListView checkListView = ad.getListView();
		checkListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				char check = (passFavorite.charAt(position) == '0' ? '1' : '0');
				setFavorite.setCharAt(position, check);

				dbUpdate();
			}

		});

		((Button) customTitleView.findViewById(R.id.imgbtn_dialog_title_busfavorite)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StringBuilder sb = new StringBuilder();
				for (int k = 0; k < passFavorite.length(); k++) {
					sb.append("0");
					checkListView.setItemChecked(k, false);
				}
				setFavorite = sb;
				dbUpdate();
				((FavoriteFragment) getParentFragment()).refreshPreview();
			}
		});
		checkListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		ad.show();
		for (int i = 0; i < checkListView.getCount(); i++) {
			checkListView.setItemChecked(i, passFavorite.charAt(i) == '0' ? false : true);
		}
	}

	private void dbUpdate() {
		StringBuilder sb = new StringBuilder(MyContentProvider.STATION_ID);
		sb.append("='").append(stationID).append("'");
		ContentValues cv = new ContentValues();
		cv.put(MyContentProvider.PASS_FAVORITE, setFavorite.toString());
		db.update("stationInfo", cv, sb.toString(), null);
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
				convertView = new AniLayout(context);
				holder.tv = (TextView) convertView.findViewById(R.id.text_favorite_list_busitem_busnumber);
				convertView.setTag(holder);
			} else {
				((AniLayout) convertView).stopAni();
				holder = (ViewHolder) convertView.getTag();
			}

			convertView.setBackgroundColor(Color.argb(position%2 * 30, 48, 99, 208));

			// 넷에서 가져온게 일치하는 경우는 넷정보를 뿌리고 그렇지 않은 것들은 그냥 이름만 뿌림
			if (position < netSize) {
				SpannableStringBuilder ssb = netList.get(position).getSpannableStringBusInfo(density);
				if (netList.get(position).isSoon()) {
					if (netList.get(position).getTime().equals("전")) {
						((AniLayout) convertView).startAniSoon();
					} else {
						convertView.setBackgroundColor(Color.argb(200, 48, 99, 208));
					}
				}
				holder.tv.setText(ssb);
			} else {
				SpannableString sb = new SpannableString(busListCopy.get(position - netSize).getBusName());
				sb.setSpan(new ForegroundColorSpan(Color.argb(100, 0, 0, 0)), 0, sb.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
				holder.tv.setText(sb);
			}

			return convertView;
		}

	}

	private class BusListCheckDialogAdapter extends BaseAdapter {

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
				tv.setTextSize(15 * density);
				int padding = (int) (12 * density);
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

	private class BusUpdateLoader extends SQLiteCursorLoader {

		public BusUpdateLoader(Context context) {
			super(context);
		}

		@Override
		protected Cursor loadCursor() {
			return null;
		}

	}

}
