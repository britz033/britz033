package adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BusSearchListCursorAdapter extends CursorAdapter{

	public BusSearchListCursorAdapter(Context context, Cursor c, int flag) {
		super(context, c, flag);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		((TextView)view).setText(cursor.getString(1));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new TextView(context);
	}


}
