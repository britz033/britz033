package subfragment;

import util.SlideLayoutBackGround;
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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.zoeas.qdeagubus.MainActivity;
import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.R;

public class Test extends Fragment implements OnBackAction{
	
	private View view;
	private ImageView iv;
	private SlideLayoutBackGround slideLayout;
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
		slideLayout = new SlideLayoutBackGround(getActivity());
		fl.addView(slideLayout);
		
		final ValueAnimator slideAnim = ObjectAnimator.ofInt(slideLayout, "width", 0, 300);
		
		slideAnim.setDuration(1000);
		slideAnim.setInterpolator(new BounceInterpolator());
		
		Button btn = (Button) view.findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				slideLayout.setVisibility(View.VISIBLE);
				if(first_flag){
					MainActivity.backAction.push();
					first_flag = false;
				}
				Bitmap all = getBitmapFromView(view);
				
				StackBlurManager blurImage = new StackBlurManager(all);
				blurImage.process(50);
				slideLayout.setBlurBitmap(blurImage.returnBlurredImage());
				slideAnim.start();				
				slideLayout.startAnimation(move());
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
		ValueAnimator backSlideAnim = ObjectAnimator.ofInt(slideLayout, "width", 300, 0);
		backSlideAnim.setDuration(500);
		backSlideAnim.setInterpolator(new AccelerateInterpolator());
		backSlideAnim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}
			@Override
			public void onAnimationRepeat(Animator animation) {
			}
			@Override
			public void onAnimationEnd(Animator animation) {
				slideLayout.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});
		backSlideAnim.start();
		
		first_flag = true;
	}
	

	private Animation move(){
		Animation ani = new TranslateAnimation(0.0f, 300.0f, 0.0f, 0.0f);
		ani.setDuration(2000);
		return ani;
	}

	@Override
	public void onClear() {
		slideLayout.setVisibility(View.GONE);
		first_flag = true;
	}
	
}
