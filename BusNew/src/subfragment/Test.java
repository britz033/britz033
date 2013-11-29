package subfragment;

import util.StackBlurManager;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zoeas.qdeagubus.R;

public class Test extends Fragment {
	
	private View view;
	private ImageView iv;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.test, null);
		iv = (ImageView) view.findViewById(R.id.img_test);
		
//		ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", /* Red */0xFFFF8080, /* Blue */
//				0xFF8080FF);
//		colorAnim.setDuration(3000);
//		colorAnim.setEvaluator(new ArgbEvaluator());
//		colorAnim.setRepeatCount(ValueAnimator.INFINITE);
//		colorAnim.setRepeatMode(ValueAnimator.REVERSE);
//		colorAnim.start();
		
		LayoutTransition layoutTransition = new LayoutTransition();
		
		ObjectAnimator transX = ObjectAnimator.ofInt(iv, "left", 0, 200).setDuration(5000);
		layoutTransition.setAnimator(LayoutTransition.APPEARING, transX);
		((LinearLayout)view).setLayoutTransition(layoutTransition);
		
		
		


//		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();  
//		lp.dimAmount=0.0f;  
//		dialog.getWindow().setAttributes(lp);  
//		dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);  
		
		
		
		// BitmapFactory.Options options = new BitmapFactory.Options();
		// options.inSampleSize = 1;
		//
		// Bitmap image = BitmapFactory.decodeResource(getResources(),
		// R.drawable.station00005, options);
		// StackBlurManager blurImage = new StackBlurManager(image);
		// blurImage.process(20);
		//
		//
		// ImageView iv = (ImageView) view.findViewById(R.id.test1);
		// iv.setImageBitmap(blurImage.returnBlurredImage());
		Button btn = (Button) view.findViewById(R.id.btn_test);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bitmap all = getBitmapFromView(view);
				
				StackBlurManager blurImage = new StackBlurManager(all);
				blurImage.process(20);
				iv.setImageBitmap(blurImage.returnBlurredImage());
				
				iv.setVisibility(View.VISIBLE);
				
				
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
}
