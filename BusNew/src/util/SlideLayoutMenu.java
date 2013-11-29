package util;

import com.zoeas.qdeagubus.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SlideLayoutMenu extends LinearLayout{
	
	private Context context;

	public SlideLayoutMenu(Context context) {
		super(context);
		this.context = context;
		init();
	}
	public SlideLayoutMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}
	
	private void init(){
		LayoutInflater.from(context).inflate(R.layout.layout_sliding_menu, this);
		setOrientation(LinearLayout.VERTICAL);
		setVisibility(View.GONE);
	}
	
	
	public void setRightPosition(int right){
		setPadding(right, 0, 0, 0);
		requestLayout();
	}

}
