package subfragment;

import util.Blur;
import util.SlideLayoutBackGround;
import util.SlideLayoutMenu;
import util.StackBlurManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TimingLogger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
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

/*
 * 후에 지울때 애니메이션 관련은 공부를 위해서 보전
 */
public class Test extends Fragment implements OnBackAction {
	
	private static final String TAG = "Test";

	private View view;
	private SlideLayoutBackGround slideLayout;
	private LinearLayout slideLayoutMenu;
	private boolean first_flag;
	private float density;
	private float height;
	private int moveDistance;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.test, null);
		first_flag = true;
		DisplayMetrics m = getActivity().getResources().getDisplayMetrics();
		density = m.density;
		height = m.heightPixels;
		moveDistance = (int)(300 * density); 

		ValueAnimator colorAnim = ObjectAnimator.ofInt(view, "backgroundColor", /* Red */
				0xFFFF8080, /* Blue */
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

		// 움직일 거리 dp
		
		ValueAnimator slideAnim = ObjectAnimator.ofInt(slideLayout, "width", 0,
				moveDistance);
		ValueAnimator slideAnim2 = ObjectAnimator.ofInt(slideLayoutMenu,
				"rightPosition", -moveDistance, 0);

		final AnimatorSet aniSet = new AnimatorSet();
		
		aniSet.playTogether(slideAnim, slideAnim2);
		aniSet.setDuration(1000);
		aniSet.setInterpolator(new BounceInterpolator());

		Button btn = (Button) view.findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (first_flag) {
					MainActivity.backAction.push();
					first_flag = false;
				}

				CaptureTask capture = new CaptureTask(getActivity(), view, slideLayout, aniSet, moveDistance, slideLayoutMenu);
				capture.execute();
			}
		});

		return view;
	}

	@Override
	public void onBackPressed() {
		ValueAnimator backSlideBackgroundAnim = ObjectAnimator.ofInt(
				slideLayout, "width", moveDistance, 0);
		ValueAnimator backSlideMenuAnim2 = ObjectAnimator.ofInt(
				slideLayoutMenu, "rightPosition", 0, -moveDistance);

		AnimatorSet backSet = new AnimatorSet();

		backSet.playTogether(backSlideBackgroundAnim, backSlideMenuAnim2);
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
				slideLayoutMenu.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}
		});

		backSet.start();

		first_flag = true;
	}

	private Animation move() {
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
	
	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		if(enter){
			return AnimationUtils.loadAnimation(getActivity(), R.animator.slide_open);
		} else {
			return AnimationUtils.loadAnimation(getActivity(), R.animator.slide_close);
		}
	}
}

class CaptureTask extends AsyncTask<Void, Void, Bitmap> {
	
	private View targetView;
	private SlideLayoutBackGround slideLayout;
	private AnimatorSet aniSet;
	private View[] above;
	private int moveDistance;
	private Context context;
	
	public CaptureTask(Context context,View targetView, SlideLayoutBackGround slideLayout, AnimatorSet aniSet, int moveDistance, View ... above){
		this.targetView = targetView;
		this.slideLayout = slideLayout;
		this.aniSet = aniSet;
		this.above = above;
		this.moveDistance = moveDistance;
		this.context = context;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		TimingLogger timing = new TimingLogger("timeChecker", "시간");
		Bitmap all = getBitmapFromView(targetView);
		Bitmap crop = Bitmap.createBitmap(all, 0,0,moveDistance, targetView.getHeight());
		timing.addSplit("캡쳐종료");
		crop = Blur.fastblur(context, crop, 20);
		timing.addSplit("이미지블러처리종료");
		timing.dumpToLog();
		
		return crop;
		
//		StackBlurManager blurImage = new StackBlurManager(crop);
//		blurImage.process(210);
//		return blurImage.returnBlurredImage();
		
	}

	public Bitmap getBitmapFromView(View targetView) {
		Bitmap returnedBitmap = Bitmap.createBitmap(targetView.getWidth(),targetView.getHeight(), Bitmap.Config.RGB_565);
		
		Canvas canvas = new Canvas(returnedBitmap);
		Drawable bgDrawable = targetView.getBackground();
		if (bgDrawable != null)
			bgDrawable.draw(canvas);
		else
			canvas.drawColor(Color.WHITE);
		targetView.draw(canvas);
		return returnedBitmap;
	}

	public Bitmap getBitmapFromView2(View targetView) {
		targetView.buildDrawingCache();
		Bitmap b1 = targetView.getDrawingCache();
		// copy this bitmap otherwise distroying the cache will destroy
		// the bitmap for the referencing drawable and you'll not
		// get the captured view
		Bitmap b = b1.copy(Bitmap.Config.ARGB_8888, false);
		// BitmapDrawable d = new BitmapDrawable(getActivity().getResources(),
		// b);
		targetView.destroyDrawingCache();
		return b;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		slideLayout.setVisibility(View.VISIBLE);
		for(int i=0; i<above.length; i++)
			above[i].setVisibility(View.VISIBLE);
		
		
		slideLayout.setBlurBitmap(result);
		aniSet.start();
	}
	
   	

}
