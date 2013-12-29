package sub.favorite;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.zoeas.qdeagubus.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class AniLayout extends RelativeLayout{

	private ValueAnimator colorAnim;
	
	public AniLayout(Context context) {
		super(context);
		init(context);
		// TODO Auto-generated constructor stub
	}
	public AniLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		// TODO Auto-generated constructor stub
	}
	public AniLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		// TODO Auto-generated constructor stub
	}
	
	private void init(Context context){
		LayoutInflater.from(context).inflate(R.layout.list_favorite_bus_item, this, true);
	}
	
	public void startAni(){
		colorAnim = ObjectAnimator.ofInt(this, "backgroundColor", /* Red */
				0xFFFF8080, /* Blue */
				0xFF8080FF);
		colorAnim.setDuration(2000);
		colorAnim.setEvaluator(new ArgbEvaluator());
		colorAnim.setRepeatCount(ValueAnimator.INFINITE);
		colorAnim.setRepeatMode(ValueAnimator.REVERSE);
		colorAnim.start();
	}
	
	public void stopAni(){
		if(colorAnim != null){
			colorAnim.cancel();
		}
	}
	

}
