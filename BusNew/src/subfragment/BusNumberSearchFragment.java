package subfragment;

import com.zoeas.qdeagubus.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/*
 * 20131029     // 월일은 2자리일땐 그대로 

   20130909     // 한자리일땐 0 씩 붙여줌


   http://businfo.daegu.go.kr/ba/route/rtbspos.do?act=findByPos&routeId=2000003100&svcDt=20131029
    순환 3-1의 경우    2000003100
    간선 303-1의 경우 3000303100
    급행 1 의 경우     1000001000
    지선 가창 1         4010001000  
    지선 달서 1 의 경우     4030001000 
     x0t0yyyz00
     
     x가 버스종류구분, t가 특수?, yyy 가 앞번호 z 가 -1 같은 뒷번호 
     
     다른번호도 사용되는데 일단은 그냥 간선, 순환, 급행만 표시
     
     http://www.businfo.go.kr/bp/realInfo.do?act=detailInfo1&sysgbn=&routeId=3000564000&menu=0&routeNo=564&state=forward
     여기 좀 좋은듯?
     
     routeId= 
     routeNo=
     state=
     
     http://businfo.daegu.go.kr/ba/route/rtbspos.do?act=findByPos&routeId=3000564000&svcDt=20131029
     
     이걸해도 둘다 동일한 정보를 보여줌
   
 */

public class BusNumberSearchFragment extends Fragment{
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_bus_layout, null);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
	}
}
