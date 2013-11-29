package subfragment;

import util.BackPressStack;
import util.SlideLayout;
import util.StackBlurManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.R;

public class Test extends Fragment implements OnBackAction{
	
	private View view;
	private ImageView iv;
	private SlideLayout slideLayout;
	private boolean first_flag;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.test, null);
		first_flag = true;
		
		ValueAnimator colorAnim = ObjectAnimator.ofInt(view, "backgroundColor", /* Red */0xFFFF8080, /* Blue */
				0xFF8080FF);
		colorAnim.setDuration(3000);
		colorAnim.setEvaluator(new ArgbEvaluator());
		colorAnim.setRepeatCount(ValueAnimator.INFINITE);
		colorAnim.setRepeatMode(ValueAnimator.REVERSE);
		colorAnim.start();
		
		
		
		FrameLayout fl = (FrameLayout) view.findViewById(R.id.source);
		slideLayout = new SlideLayout(getActivity());
		fl.addView(slideLayout);
		
		final ValueAnimator slideAnim = ObjectAnimator.ofInt(slideLayout, "width", 0, 300);
		slideAnim.setDuration(1000);
		slideAnim.setInterpolator(new BounceInterpolator());
		
		Button btn = (Button) view.findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(first_flag){
					MainActivity.backAction.push(MainActivity.MyTabs.TEST);
					first_flag = false;
				}
				Bitmap all = getBitmapFromView(view);
				
				slideLayout.setVisibility(View.VISIBLE);
				StackBlurManager blurImage = new StackBlurManager(all);
				blurImage.process(50);
				slideLayout.setBlurBitmap(blurImage.returnBlurredImage());
				slideAnim.start();
				
			}
		});

		return view;
	}
	
	public Bitmap getBitmapFromView(View view) {
	    Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(returnedBitmap);
	    Drawable bgDrawable =view.getBackground();
	    if (bgDrawable!=null) 
	        bgDrawable.draw(canvas);
	    else 
	        canvas.drawColor(Color.WHITE);
	    view.draw(canvas);
	    return returnedBitmap;
	}

	@Override
	public void onBackPressed() {
		slideLayout.setVisibility(View.GONE);
		first_flag = true;
	}
	
}
