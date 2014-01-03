package sub.search.bus;

import adapter.OnCommunicationActivity;
import adapter.OnCommunicationReceive;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import businfo.activity.BusInfoActivity;

import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;

public class SearchBusNumberFragment extends ListFragment implements LoaderCallbacks<Cursor>, TextWatcher,
		OnKeyListener, OnBackAction, OnCommunicationReceive{

	private Context context;
	private BusSearchListCursorAdapter busAdapter;
	private EditText et;
	private InputMethodManager imm;
	public static final String SELECTION_KEY = "selection";
	public static final String DEFAULT_STATION = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();
		View view = inflater.inflate(R.layout.fragment_search_bus_layout, null);

		et = (EditText) view.findViewById(R.id.edit_fragment_search_bus);
		et.addTextChangedListener(this);
		et.setOnKeyListener(this);

		imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

		busAdapter = new BusSearchListCursorAdapter(context, null, 0);
		setListAdapter(busAdapter);

		getLoaderManager().initLoader(0, null, this);
		return view;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {

		Uri uri = MyContentProvider.CONTENT_URI_BUS;
		String[] projection = { "_id", "bus_number", "bus_id" };
		String selection = null;
		String sortOrder = null;

		if (data != null) {
			selection = data.getString(SELECTION_KEY);
		}

		return new CursorLoader(context, uri, projection, selection, null, sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		busAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		busAdapter.swapCursor(null);
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Bundle data = new Bundle();
		data.putString(SELECTION_KEY, "bus_number like '%" + s.toString() + "%'");

		getLoaderManager().restartLoader(0, data, this);
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
			imm.hideSoftInputFromWindow(et.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			return true;
		}

		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor cursor = busAdapter.getCursor();
		cursor.moveToPosition(position);
		String busId = cursor.getString(2);
		String busNum = cursor.getString(1);

		Intent intent = new Intent(context, BusInfoActivity.class);
		intent.putExtra(BusInfoActivity.KEY_BUS_ID, busId);
		intent.putExtra(BusInfoActivity.KEY_BUS_NAME, busNum);
		intent.putExtra(BusInfoActivity.KEY_CURRENT_STATION_NAME, DEFAULT_STATION);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		OnCommunicationActivity backtotheFavorite = (OnCommunicationActivity) getActivity();
		backtotheFavorite.OnTabMove(MainActivity.MyTabs.FAVORITE, null);
	}

	@Override
	public void onClear() {

	}

	@Override
	public void OnReceive(Bundle data) {
		MainActivity.backAction.push();
		et.setText(data.getString(SELECTION_KEY));
		Toast.makeText(context, "뒤로가기를 누르시면 즐겨찾기로 되돌아갑니다", Toast.LENGTH_SHORT).show();
	}

}
