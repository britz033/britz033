package com.zoeas.util;

import java.util.LinkedList;

import android.os.CountDownTimer;

import com.zoeas.qdeagubus.MainActivity;


/**
 * 백스택 자동처리, 아무런 동작용 백스텍이 없을시 첫 백 이후 2초후 자동으로 원상태(ready)로 복구
 * 2초전에 다시 백을 누를시 종료
 * 2초전에 다른 작업이 들어올시 ready를 일단 추가한후 다른 작업을 추가
 * 만약 작업을 추가한 상태에서 다른 서브메뉴로 이동할 경우
 * 다시 모두 초기화
 * 
 * 이 클래스가 구분해주는 것은 종료와 다른 동작이 있었다는 것뿐임. 
 * 그러므로 다른곳에서 3-4개 동작이 들어오면 그만큼 동작이 들어와 있다는 것만 알려주니
 * 순서처리등은 프래그먼트에서 개별적으로 해야함.
 * @author lol
 *
 */
public class BackPressStack {

	public static final int FINISH = -1;
	public static final int FINISH_READY = 1000;
	public static final int DO_SOMETHING = 1001;

	LinkedList<Integer> backId = new LinkedList<Integer>();

	public BackPressStack() {
		onlyFirstPush();
	}

	/**
	 * 이 메소드가 불려지면 뒤로가기 버튼이 OnBackAction 인터페이스의 onBackPressed() 메소드랑 연결됩니다
	 * 한번에 뒤로가기 저장은 2개까지 됩니다. 그 이후 불려지는 것은 무시됩니다.
	 */
	public void push() {
		// 암것도 없으면 finish_ready를 무조건 젤 첨에 넣는다
		if(backId.size() == 0) {
			backId.addFirst(FINISH_READY);
		}
		if(backId.size() < 3){
			backId.addFirst(DO_SOMETHING);
		}
	}
	
	private void onlyFirstPush(){
		if(backId.size() == 0)
			backId.addFirst(FINISH_READY);
	}

	/**
	 * 처음엔 무조건 FINISH_READY 가 들어있습니다. 그 후 do_something 이 추가 가능합니다
	 * 불려질때마다 하나씩 제거가 되는데 마지막에 FINISH_READY 가 불린후 2초후 자동으로
	 * 다시 FINISH_READY 가 채워집니다. 2초 이전에 불려지면 FINISH 를 반환합니다.
	 * DO_SOMETHING 은 두 개까지만 저장이 되어 있으므로 pop은 연속으로 총 4번이 가능합니다 
	 * @return 가장 나중에 넣어진 액션을 반환합니다
	 */
	public int pop() {
		// 암것도 없으면 FINISH 날림, 1개만 있으면 2초후 finish_ready를 맨앞에 추가
		if (backId.size() == 0)
			return FINISH;
		else if(backId.size() == 1){
			new CountDownTimer(2000,1000) {
				@Override
				public void onTick(long millisUntilFinished) {
				}
				
				@Override
				public void onFinish() {
					onlyFirstPush();
				}
			}.start();
		}
		return backId.removeFirst();
	}
	
	// 메뉴를 옮길때 호출
	public void init(){
		backId.clear(); 
		backId.addFirst(FINISH_READY);
	}
	
	// 동작을 한번만으로 제한하고 싶을때 사용
	public boolean isAlreadyPushed(){
		if(backId.size() > 1)
			return true;
		return false;
	}

}
