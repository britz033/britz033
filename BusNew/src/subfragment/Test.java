package subfragment;

import util.SlideLayoutBackGround;
import util.SlideLayoutMenu;
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
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
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
	private LinearLayout slideLayoutMenu;
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
		slideLayoutMenu = new SlideLayoutMenu(getActivity());
		
		fl.addView(slideLayout);
		fl.addView(slideLayoutMenu);
		
		// 참고로 300은 픽셀임
		ValueAnimator slideAnim = ObjectAnimator.ofInt(slideLayout, "width", 0, 300);
		ValueAnimator slideAnim2 = ObjectAnimator.ofInt(slideLayoutMenu, "rightPosition", -300, 0);
		
		final AnimatorSet aniSet = new AnimatorSet();
		aniSet.playTogether(slideAnim, slideAnim2);
		aniSet.setDuration(1000);
		aniSet.setInterpolator(new BounceInterpolator());
		
		Button btn = (Button) view.findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				slideLayout.setVisibility(View.VISIBLE);
				slideLayoutMenu.setVisibility(View.VISIBLE);
				if(first_flag){
					MainActivity.backAction.push();
					first_flag = false;
				}
				Bitmap all = getBitmapFromView(view);
				
				StackBlurManager blurImage = new StackBlurManager(all);
				blurImage.process(50);
				slideLayout.setBlurBitmap(blurImage.returnBlurredImage());
				
				aniSet.start();				
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
		ValueAnimator backSlideBackgroundAnim = ObjectAnimator.ofInt(slideLayout, "width", 300, 0);
		ValueAnimator backSlideMenuAnim2 = ObjectAnimator.ofInt(slideLayoutMenu, "rightPosition", 0, -300);
		
		AnimatorSet backSet = new AnimatorSet();
		
		backSet.playTogether(backSlideBackgroundAnim,backSlideMenuAnim2);
		backSet.setDuration(500);
		backSet.setInterpolator(new AccelerateInterpolator());
		backSet.addListener(new AnimatorListener() {
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
		
		backSet.start();
		
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
		slideLayoutMenu.setVisibility(View.GONE);
		first_flag = true;
	}
	
}
