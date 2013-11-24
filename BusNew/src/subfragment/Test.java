package subfragment;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.zoeas.qdeagubus.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Test extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ValueAnimator colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", /*Red*/0xFFFF8080, /*Blue*/0xFF8080FF);
		colorAnim.setDuration(3000);
		colorAnim.setEvaluator(new ArgbEvaluator());
		colorAnim.setRepeatCount(ValueAnimator.INFINITE);
		colorAnim.setRepeatMode(ValueAnimator.REVERSE);
		colorAnim.start();
		
		return inflater.inflate(R.layout.test, null);
	}
}
