package businfo.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class BusInfoStationSearchListAdapter extends BaseAdapter implements OnClickListener{

	private ArrayList<String> searchedStationList;
	private Context context;
	private HashMap<Integer, Integer> pathFavorite;
	private ArrayList<Integer> searchedStationId;

	static class ViewHolder {
		TextView name;
		ImageButton ibFavorite;
	}

	public BusInfoStationSearchListAdapter(ArrayList<String> searchedStationList, ArrayList<Integer> searchedStationId, HashMap<Integer, Integer> pathFavorite, Context context) {
		this.searchedStationList = searchedStationList;
		this.context = context;
		this.pathFavorite = pathFavorite;
		this.searchedStationId = searchedStationId;
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

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.list_favorite_station_item, parent, false);
			holder.name = (TextView) convertView.findViewById(R.id.text_station_item_name);
			holder.ibFavorite = (ImageButton) convertView.findViewById(R.id.btn_station_item_favorite);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText(searchedStationList.get(position));
		if (pathFavorite.get(searchedStationId.get(position)) == 0) {
			holder.ibFavorite.setImageResource(R.drawable.btn_station_list_item_off_selector);
		} else {
			holder.ibFavorite.setImageResource(R.drawable.btn_station_list_item_on_selector);
		}

		holder.ibFavorite.setTag(new Integer(position));
		holder.ibFavorite.setOnClickListener(this);
		return convertView;
	}

	@Override
	public void onClick(View v) {
		int position = (Integer)v.getTag();
		
		SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath("StationDB.png").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
		ContentValues cv = new ContentValues();
		if (pathFavorite.get(searchedStationId.get(position)) == 0) {
			cv.put("station_favorite", 1);
			((ImageButton)v).setImageResource(R.drawable.btn_station_list_item_on_selector);
		} else {
			cv.put("station_favorite", 0);
			((ImageButton)v).setImageResource(R.drawable.btn_station_list_item_off_selector);
		}
		
		db.update(MyContentProvider.TABLE_NAME_STATION, cv, "_id=" + searchedStationId.get(position), null);
		db.close();
		
	}

}
