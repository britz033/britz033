package subfragment;

import com.zoeas.qdeagubus.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/*  
     http://www.businfo.go.kr/bp/realInfo.do?act=detailInfo1&sysgbn=&routeId=3000564000&menu=0&routeNo=564&state=forward
     http://businfo.daegu.go.kr/ba/route/rtbspos.do?act=findByPos&routeId=3000564000&svcDt=20131029
 
    
         버스DB 
 _id  버스번호                   기점, 종점, 첫차시간, 막차시간, 배차간격, 즐겨찾기, 정방향역, 역방향역 
       506(테이블 버스DB1)   05:33   23:00   13
       
       전광판 뿌릴땐, 역번호 + 즐겨찾기로 기본틀을 뿌린후. 인터넷에서의 정보로 남은걸 뿌림
 
 	역DB
 _id   역번호, 역이름, 경도, 위도, 즐겨찾기
 
 
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
