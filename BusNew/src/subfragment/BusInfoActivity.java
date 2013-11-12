package subfragment;

import adapter.PathView;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class BusInfoActivity extends FragmentActivity implements LoaderCallbacks<Cursor>{
	
	private Cursor cursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_businfo);
		
		PathView pv = (PathView) findViewById(R.id.horizontalview_activity_businfo_path);
		
		
		getSupportLoaderManager().initLoader(0, getIntent().getExtras(), this);
		String busInterval = cursor.getString(1);
		String busForward = cursor.getString(2);
		String busBackward = cursor.getString(3);
		String busFavorite = cursor.getString(4);
		
		
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle busNum) {
		Uri uri = MyContentProvider.CONTENT_URI_BUS;
		
		String[] projection = {"_id","bus_interval", "bus_forward", "bus_backward", "bus_favorite"};
		String selection = "bus_number='" + busNum.getString("busNum") + "'";
		
		return new CursorLoader(this, uri, projection, selection, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		this.cursor = cursor;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		this.cursor = null;
	}
}
