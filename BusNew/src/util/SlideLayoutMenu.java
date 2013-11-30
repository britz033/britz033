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
	
	// 패딩으로 내용을 이동시키는 개념이라 실제 레이아웃은 처음부터 그대로 존재함
	// 고로 여기서 백컬러를 설정하면 전체를 뒤덮게됨 (onMeasure를 오버라이딩해서 크기를 설정안한 커스텀 레이아웃클래스기 땜시)
	private void init(){
		LayoutInflater.from(context).inflate(R.layout.layout_sliding_menu, this);
		setOrientation(LinearLayout.VERTICAL);
		setVisibility(View.GONE);
//		setBackgroundColor(Color.argb(120, 255, 255, 255));
	}
	
	
	public void setRightPosition(int right){
		setPadding((int)(right), 0, 0, 0);
		requestLayout();
	}

}
