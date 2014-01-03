package com.zoeas.util;

import java.util.LinkedList;

import android.os.CountDownTimer;


public class BackPressStack {

	public static final int FINISH = -1;
	public static final int FINISH_READY = 1000;
	public static final int DO_SOMETHING = 1001;

	LinkedList<Integer> backId = new LinkedList<Integer>();

	public BackPressStack() {
		onlyFirstPush();
	}

	public void push() {
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
					onlyFirstPush();
				}
			}.start();
		}
		return backId.removeFirst();
	}
	
	public void init(){
		backId.clear(); 
		backId.addFirst(FINISH_READY);
	}
	
	public boolean isAlreadyPushed(){
		if(backId.size() > 1)
			return true;
		return false;
	}

}
