package util;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.zoeas.qdeagubus.R;

public class SlideLayoutMenu extends LinearLayout{
	
	private Context context;
	private int mright;

	public SlideLayoutMenu(Context context) {
		super(context);
		this.context = context;
		init();
	}
	
	private void init(){
		LayoutInflater.from(context).inflate(R.layout.layout_sliding_menu, this);
		setOrientation(LinearLayout.VERTICAL);
		setVisibility(View.GONE);
		setBackgroundColor(Color.argb(120, 255, 255, 255));
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		setMeasuredDimension(mright, MeasureSpec.getSize(heightMeasureSpec));
	}
	
	
	public void setRightPosition(int right){
		mright = -right;
		setPadding((int)(right), 0, 0, 0);
		requestLayout();
	}

}
