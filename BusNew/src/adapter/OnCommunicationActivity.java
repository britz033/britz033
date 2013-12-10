package adapter;

import android.os.Bundle;

import com.zoeas.qdeagubus.MainActivity.MyTabs;

/**
 * 프래그먼트간의 통신을 위해서 메인액티비티 호출
 * @author lol
 *
 */
public interface OnCommunicationActivity {
	/**
	 * 즐겨찾기 미리보기를 갱신하기 위해서 정류소 검색에서 즐겨찾기 프레그먼트를 호출하는 전용 메소드
	 */
	public void OnFavoriteRefresh();
	/**
	 * 다른 프래그먼트를 선택하여 이동하기 위한 메소드
	 * @param myTab	메인액티비티의 MyTabs 상수값, 이동하고픈 탭을 지정
	 * @param data 목적 프래그먼트에 전달할 데이터
	 */
	public void OnTabMove(MyTabs myTab, Bundle data);
}
