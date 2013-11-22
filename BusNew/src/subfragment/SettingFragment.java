package subfragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;

import com.zoeas.qdeagubus.R;


public class SettingFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.temp, null);
	    ExpandableListView elv = (ExpandableListView) view.findViewById(R.id.listtest);
	    elv.setAdapter(new BaseExpandableAdapter(getActivity()));
	    elv.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				getActivity().startActivityForResult(intent, 1001);
				return false;
			}
		});
		return view;
	}
}

class BaseExpandableAdapter extends BaseExpandableListAdapter{
	
	Context context;
	
	public BaseExpandableAdapter(Context context){
		this.context = context;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		TextView tv = new TextView(context);
		tv.setText("챠챠 챠일드!!");
		
		return tv;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 2;
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		return null;
	}

	@Override
	public int getGroupCount() {
		return 10;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		TextView tv = new TextView(context);
		tv.setText("그룹입니다만");
		return tv;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}