package adapter;

import com.zoeas.qdeagubus.R;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class BusSearchListCursorAdapter extends CursorAdapter implements OnTouchListener{
	
	Context context;

	public BusSearchListCursorAdapter(Context context, Cursor c, int flag) {
		super(context, c, flag);
		this.context = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tv = (TextView) view.findViewById(R.id.text_bus_search_list_item);
		tv.setText(cursor.getString(1));
		view.setOnTouchListener(this);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.list_bus_search_item, null);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return false;
	}


}
