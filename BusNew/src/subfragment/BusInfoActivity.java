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
	private int loopIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_businfo);
		loopIndex = 0;

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

	// 로더 쿼리가 끝난후 호출
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

		while (true) {
			if (!matcher.find())
				break;
			station = matcher.group(1);
			path.add(station);
		}

		PathPagerAdapter<BusInfoPathItemFragment> adapter = new PathPagerAdapter<BusInfoPathItemFragment>(
				getSupportFragmentManager(), path, BusInfoPathItemFragment.class);
		pathPager.setAdapter(adapter);
		pathPager.setOnPageChangeListener(this);

		TextView textBusInterval = (TextView) findViewById(R.id.text_activity_businfo_current);
		textBusInterval.setText(busInterval);
		
		loopQuery();
		
	}
	
	public void loopQuery(){
		Log.d("루프 스테이션 이름",path.get(loopIndex));
		Bundle data = new Bundle();
		data.putString(KEY_PATH_STATION, path.get(loopIndex));
		loopIndex++;
		getSupportLoaderManager().restartLoader(1, data, this);
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {

		Uri uri = null;
		String[] projection = null;
		String selection = null;

		if (id == 0) {
			Log.d("로더 init",data.getString(KEY_BUS_INFO));
			uri = MyContentProvider.CONTENT_URI_BUS;
			projection = new String[] { "_id", "bus_interval", "bus_forward", "bus_backward", "bus_favorite" };
			selection = "bus_number='" + data.getString(KEY_BUS_INFO) + "'";
		} else if (id == 1) {
			uri = MyContentProvider.CONTENT_URI_STATION;
			projection = new String[] { "_id", "station_longitude", "station_latitude" };
			selection = "station_name='" + data.getString(KEY_PATH_STATION) + "'";
		}

		return new CursorLoader(this, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()){
		case 0:
			mcursor = cursor;
			initBusInfo();
			break;
		case 1:
			cursor.moveToNext();
			Log.d("좌표",String.valueOf(cursor.getDouble(1)));
			if(loopIndex < path.size()){
				loopQuery();
			}
			break;
		}
		
		

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		Log.d("로더리셋","불려짐");
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
