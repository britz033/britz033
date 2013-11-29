package util;

import java.util.LinkedList;

import android.os.CountDownTimer;

import com.zoeas.qdeagubus.MainActivity;


/*
 * 백스택 자동처리, 아무런 동작용 백스텍이 없을시 첫 백 이후 2초후 자동으로 원상태(ready)로 복구
 * 2초전에 다시 백을 누를시 종료
 * 2초전에 다른 작업이 들어올시 ready를 일단 추가한후 다른 작업을 추가
 * 만약 작업을 추가한 상태에서 다른 서브메뉴로 이동할 경우
 * 다시 모두 초기화
 * 
 * 이 클래스가 구분해주는 것은 종료와 다른 동작이 있었다는 것뿐임. 
 * 그러므로 다른곳에서 3-4개 동작이 들어오면 그만큼 동작이 들어와 있다는 것만 알려주니
 * 순서처리등은 프래그먼트에서 개별적으로 해야함.
 */
public class BackPressStack {

	public static final int FINISH = -1;
	public static final int FINISH_READY = 1000;
	public static final int DO_SOMETHING = 1001;

	private MainActivity.MyTabs sub;

	LinkedList<Integer> backId = new LinkedList<Integer>();

	public BackPressStack() {
		push(null);
	}

	// 암것도 없으면 finish_ready를 무조건 젤 첨에 넣는다
	public void push(MainActivity.MyTabs tab) {
		if(backId.size() == 0) {
			backId.addFirst(FINISH_READY);
		}
		
		if(tab != null){
			backId.addFirst(DO_SOMETHING);
			sub = tab;
		}
	}

	// 암것도 없으면 FINISH 날림, 1개만 있으면 2초후 finish_ready를 맨앞에 추가
	public int pop() {
		if (backId.size() == 0)
			return FINISH;
		else if(backId.size() == 1){
			new CountDownTimer(2000,1000) {
				@Override
				public void onTick(long millisUntilFinished) {
				}
				
				@Override
				public void onFinish() {
					push(null);
				}
			}.start();
		}
		return backId.removeFirst();
	}

	public MainActivity.MyTabs getMyTab() {
		return sub;
	}
	
	// 메뉴를 옮길때 호출
	public void init(){
		backId.clear(); 
		backId.addFirst(FINISH_READY);
	}

}
