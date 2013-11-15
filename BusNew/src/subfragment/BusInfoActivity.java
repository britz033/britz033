package subfragment;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.PathPagerAdapter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.TextView;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class BusInfoActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, OnPageChangeListener {

	public static final String KEY_BUS_INFO = "BUSNUM";
	public static final String KEY_CURRENT_STATION_NAME = "STATION";
	public static final String KEY_PATH_STATION = "PATH";
	private ViewPager pathPager;
	private ArrayList<String> path;
	private Cursor mcursor;
	private boolean flag = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_businfo);

		pathPager = (ViewPager) findViewById(R.id.viewpager_activity_businfo_path);

		String busNum = getIntent().getExtras().getString(KEY_BUS_INFO);
		String stationName = getIntent().getExtras().getString(KEY_CURRENT_STATION_NAME);
		TextView textBusNumber = (TextView) findViewById(R.id.text_activity_businfo_number);
		TextView textStationName = (TextView) findViewById(R.id.text_activity_businfo_stationname);
		textBusNumber.setText(busNum);
		if (stationName != null)
			textStationName.setText(stationName);

		getSupportLoaderManager().initLoader(0, getIntent().getExtras(), this);

		Log.d("버스인포", "onCreate 호출");

	}

	private void initBusInfo() {
		mcursor.moveToFirst();

		String busInterval = mcursor.getString(1);
		String busForward = mcursor.getString(2);
		String busBackward = mcursor.getString(3);
		String busFavorite = mcursor.getString(4);

		Pattern pattern = Pattern.compile("([^,]+),");
		Matcher matcher = pattern.matcher(busForward);

		path = new ArrayList<String>();
		String station = null;
		Bundle data = new Bundle();
		
		flag = false;
		
		while (true) {
			if (!matcher.find())
				break;
			station = matcher.group(1);
			path.add(station);
			data.putString(KEY_PATH_STATION, station);
			LoaderManager lm = getSupportLoaderManager();
			lm.restartLoader(1, data, this);
		}

		PathPagerAdapter<BusInfoPathItemFragment> adapter = new PathPagerAdapter<BusInfoPathItemFragment>(
				getSupportFragmentManager(), path, BusInfoPathItemFragment.class);
		pathPager.setAdapter(adapter);
		pathPager.setOnPageChangeListener(this);

		TextView textBusInterval = (TextView) findViewById(R.id.text_activity_businfo_current);
		textBusInterval.setText(busInterval);
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
		
		Uri uri = null;
		String[] projection = null;
		String selection = null;
				
		if (id == 0) {
			uri = MyContentProvider.CONTENT_URI_BUS;
			projection = new String[]{ "_id", "bus_interval", "bus_forward", "bus_backward", "bus_favorite" };
			selection = "bus_number='" + data.getString(KEY_BUS_INFO) + "'";
		} else if (id == 1) {
			
			uri = MyContentProvider.CONTENT_URI_STATION;
			projection = new String[]{ "_id", "station_longitude" , "station_latitude" };
			selection = "station_name='" + data.getString(KEY_PATH_STATION) + "'";
			
		}
		
		return new CursorLoader(this, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if(flag){
		mcursor = cursor;
		initBusInfo();
		} else {
			cursor.moveToNext();
//			Log.d("좌표",String.valueOf(cursor.getDouble(1)));
		}
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	
}
