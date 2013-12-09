package businfo.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zoeas.qdeagubus.R;

public class BusInfoPathItemFragment extends Fragment{
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_businfo_path, null);
		TextView tv = (TextView) view.findViewById(R.id.text_path);
		ImageView iv = (ImageView) view.findViewById(R.id.img_path);
		
		Bundle args = getArguments();
		
		if(args != null){
			int startEnd = args.getInt(PathPagerAdapter.PATH_START_END);
			tv.setText(args.getString(PathPagerAdapter.PATH_STATION_NAME));
			switch(startEnd){
			case 0: 
				iv.setImageResource(R.drawable.path_start);
				break;
			case 1: 
				iv.setImageResource(R.drawable.path_end);
				break;
			case 2: break;
			}
		}
		else
			view.setVisibility(View.INVISIBLE);
		
		return view;
	}
	
}
