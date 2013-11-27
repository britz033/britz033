package subfragment;

import util.StackBlurManager;
import android.app.AlertDialog;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.zoeas.qdeagubus.R;

public class Test extends Fragment {
	
	private View view;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.test, null);

		ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", /* Red */0xFFFF8080, /* Blue */
				0xFF8080FF);
		colorAnim.setDuration(3000);
		colorAnim.setEvaluator(new ArgbEvaluator());
		colorAnim.setRepeatCount(ValueAnimator.INFINITE);
		colorAnim.setRepeatMode(ValueAnimator.REVERSE);
		colorAnim.start();


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
				ImageView iv = new ImageView(getActivity());
//				iv.setLayoutParams(new LayoutParams(300, 300));
				iv.setImageBitmap(blurImage.returnBlurredImage());
				
				new AlertDialog.Builder(getActivity()).setView(iv).create().show();
				
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
