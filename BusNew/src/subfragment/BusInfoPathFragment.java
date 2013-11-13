package subfragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zoeas.qdeagubus.R;

public class BusInfoPathFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_businfo_path, null);
		TextView tv = (TextView) view.findViewById(R.id.text_path);
		
		Bundle args = getArguments();
		tv.setText(args.getString("station"));
		
		
		
		return view;
	}
}
