package util;

import android.content.Context;
import android.util.AttributeSet;
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
		setOrientation(LinearLayout.VERTICAL);
		
	}
	
	
	public void setRightPosition(int right){
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(changed){
			
		}
	}

}
