package internet;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

/*
 * 각 버스들 정보
 * 도착까지 가까운지 여부, 번호, 시간 , 현재장소
 */
public class BusInfo {
//	private static String station;
    private static String station;
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
	
	
	
	public static String getStation() {
		return station;
	}



	public static void setStation(String station) {
		BusInfo.station = station;
	}



	public boolean isSoon() {
		return soon;
	}


	public void setSoon(boolean soon) {
		this.soon = soon;
	}


	public String getBusNum() {
		return busNum;
	}


	public void setBusNum(String busNum) {
		this.busNum = busNum;
	}


	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public String getCurrent() {
		return current;
	}


	public void setCurrent(String current) {
		this.current = current;
	}


	public SpannableStringBuilder getInfo(){
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
