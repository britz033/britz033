package subfragment;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import adapter.PathPagerAdapter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class BusInfoActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor> {

	public static final String KEY_BUS_INFO = "BUSNUM";
	public static final String KEY_STATION_NAME = "STATION";
	private ViewPager pathPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_businfo);

		pathPager = (ViewPager) findViewById(R.id.viewpager_activity_businfo_path);

		String busNum = getIntent().getExtras().getString(KEY_BUS_INFO);
		String stationName = getIntent().getExtras().getString(KEY_STATION_NAME);
		TextView textBusNumber = (TextView) findViewById(R.id.text_activity_businfo_number);
		TextView textStationName = (TextView) findViewById(R.id.text_activity_businfo_stationname);
		textBusNumber.setText(busNum);
		if(stationName != null)
			textStationName.setText(stationName);

		getSupportLoaderManager().initLoader(0, getIntent().getExtras(), this);

		Log.d("버스인포", "curosr 쓰기직전");

	}

	private void initBusInfo(Cursor cursor) {
		cursor.moveToFirst();

		String busInterval = cursor.getString(1);
		String busForward = cursor.getString(2);
		String busBackward = cursor.getString(3);
		String busFavorite = cursor.getString(4);

		Pattern pattern = Pattern.compile("([^,]+),");
		Matcher matcher = pattern.matcher(busForward);

		ArrayList<String> path = new ArrayList<String>();
		while (true) {
			if (!matcher.find())
				break;
			path.add(matcher.group(1));
		}

		PathPagerAdapter<BusInfoPathFragment> adapter = new PathPagerAdapter<BusInfoPathFragment>(
				getSupportFragmentManager(), path, BusInfoPathFragment.class);
		pathPager.setAdapter(adapter);

		TextView textBusInterval = (TextView) findViewById(R.id.text_activity_businfo_current);
		textBusInterval.setText(busInterval);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle busNum) {
		Uri uri = MyContentProvider.CONTENT_URI_BUS;

		String[] projection = { "_id", "bus_interval", "bus_forward",
				"bus_backward", "bus_favorite" };
		String selection = "bus_number='" + busNum.getString(KEY_BUS_INFO) + "'";

		Log.d("버스인포", selection);

		return new CursorLoader(this, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		Log.d("버스인포", "커서finish");

		initBusInfo(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}
