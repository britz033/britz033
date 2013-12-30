package com.zoeas.util;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;


public class AnimationRelativeLayout extends RelativeLayout {

	private Context context;
	private Animation inAni;
	private Animation outAni;
	
	
	public AnimationRelativeLayout(Context context) {
		super(context);
		this.context = context;
		initAnimations();
	}

	public AnimationRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initAnimations();
	}

	public void initAnimations(){
		inAni = (Animation) AnimationUtils.loadAnimation(context, R.anim.fade_in);
		outAni = (Animation) AnimationUtils.loadAnimation(context, R.anim.fade_out);
	}
	
	public void show() {
		if(isVisible()) return;
		show(true);
	}
	
	public void show(boolean withAnimation){
		if(withAnimation)
			startAnimation(inAni);
		this.setVisibility(View.VISIBLE);
	}
	
	public void hide() {
		if(!isVisible()) return;
		hide(true);
	}
	
	public void hide(boolean withAnimation){
		if(withAnimation)
			startAnimation(outAni);
		this.setVisibility(View.GONE);
	}
	
	public boolean isVisible(){
		return (this.getVisibility() == View.VISIBLE);
	}
	
	public void setInAnimation(Animation ani){
		inAni = ani;
	}
	public void setOutAnimation(Animation ani){
		outAni = ani;
	}

}
