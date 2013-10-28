package internet;

import lombok.Getter;
import lombok.Setter;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

/*
 * 각 버스들 정보
 * 도착까지 가까운지 여부, 번호, 시간 , 현재장소
 */
public @Getter @Setter class BusInfo {
    private String station;
	private boolean soon;
	private String busNum;
	private String time;
	private String current;
	
	public BusInfo(){
		soon=false;
		busNum="버스번호";
		time="버스남은시간";
		current="버스현재장소";
		station="버스역";
	}
	
	public SpannableStringBuilder getSpannableStringBusInfo(){
	    SpannableStringBuilder spb= new SpannableStringBuilder();
	    
	    if(soon){
	    	SpannableString sp = new SpannableString("**다와감**");
	    	sp.setSpan(new ForegroundColorSpan(Color.RED), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	    	spb.append(sp).append(" ");
	    }
	    spb.append(busNum).append(" ");
	    spb.append(time).append(" ");
	    spb.append(current).append(" ");
	    spb.append("\n");
	    
		return spb;
	}
}
