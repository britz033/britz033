package com.zoeas.util;

import java.util.zip.Inflater;

import com.zoeas.qdeagubus.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class SlideLayoutBackGround extends LinearLayout{
	
	private int mHeight,mWidth;
	private Bitmap blurBitmap;
	private Context context;

	public SlideLayoutBackGround(Context context) {
		super(context);
		this.context = context;
		
		init();
	}
	
	private void init(){
		mWidth = 0;
		setOrientation(LinearLayout.VERTICAL);
		LayoutInflater.from(context).inflate(R.layout.layout_sliding_background, this, true);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(mWidth, mHeight);
	}
	
	@SuppressLint("NewApi")
	public void setWidth(int width){
		mWidth = (int)(width);
		Bitmap piece = Bitmap.createBitmap(blurBitmap, 0, 0, mWidth, mHeight);
		BitmapDrawable background = new BitmapDrawable(context.getResources(), piece);
		
		if(VERSION.SDK_INT < 16)
			setBackgroundDrawable(background);
		else 
			setBackground(background);
		requestLayout();
	}
	
	public void setBlurBitmap(Bitmap bitmap){
		blurBitmap = bitmap;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
