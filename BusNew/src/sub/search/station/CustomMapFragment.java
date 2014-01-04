package sub.search.station;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

/** 
 * 애니메이션 함 할려고 조잡하게 만든 map프래그 현재 정류소검색 맵에 사용되고 있으며 조만간 삭제예정
 * @author lol
 *
 */
public class CustomMapFragment extends SupportMapFragment{
	
	public CustomMapFragment(){
		super();
	}
	
	public static CustomMapFragment newInstance(){
		CustomMapFragment cmf = new CustomMapFragment();
		return cmf;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		Fragment fragment = getParentFragment();
		
		if(fragment != null && fragment instanceof OnMapReadyListener){
			((OnMapReadyListener)fragment).OnMapReady(getMap());
		}
		
		return view;
	}
	
	public static interface OnMapReadyListener{
		public void OnMapReady(GoogleMap map);
	}
	
}
