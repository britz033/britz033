package com.zoeas.qdeagubus;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

class MainViewPager extends ViewPager {

	public MainViewPager(Context context) {
		super(context);
	}
	public MainViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View view, boolean checkV, int arg2, int arg3,
			int arg4) {
		if(view != this && view instanceof ViewPager){
			return true;
		}
		
		return super.canScroll(view, checkV, arg2, arg3, arg4);
	}
}
