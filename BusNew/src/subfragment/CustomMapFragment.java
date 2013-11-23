package subfragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public class CustomMapFragment extends SupportMapFragment{
	
	public CustomMapFragment(){
		super();
	}
	
	// 싱글톤패턴... 은 아니고 걍 편하게..
	public static CustomMapFragment newInstance(){
		CustomMapFragment cmf = new CustomMapFragment();
		return cmf;
	}

	// onCreateView가 호출되었을때 MapView가 존재한다. 그러므로 이것이 호출되었다는 말은 준비가 되었다는 말
	// 그래서 바깥의 메소드를 호출하기 위해 인터페이스를 정의하고 불러들인다. 
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
