package internet;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class BusInfoNet implements Parcelable {
	private String station;
	private boolean soon;
	private String busNum;
	private String time;
	private String current;
	private String route;
	private String busId;

	public BusInfoNet() {
		soon = false;
		busNum = "버스번호";
		time = "버스남은시간";
		current = "버스현재장소";
		station = "버스역";
		route = "";
		busId = null;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public BusInfoNet(Parcel in) {
		readFromParcel(in);
	}

	public SpannableStringBuilder getSpannableStringBusInfo(float density) {
		StringBuilder sb = new StringBuilder(busNum).append(" ").append(route);
		SpannableStringBuilder spb = new SpannableStringBuilder();

		if (soon && !time.equals("전")) {
			SpannableString sp = new SpannableString("**다와감**");
			sp.setSpan(new ForegroundColorSpan(Color.RED), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spb.append(sp).append(" ");
		}

		int textSize = (int) (30 * density);

		SpannableString num = new SpannableString(sb.toString());
		num.setSpan(new AbsoluteSizeSpan(textSize), 0, num.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		num.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, num.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		
		if (!time.equals("전")) {
			spb.append(num).append(" ");
			spb.append("[");
			spb.append(time).append("] ");
			spb.append(current).append(" ");
		} else {
			num.setSpan(new ForegroundColorSpan(Color.WHITE), 0, num.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			SpannableString s = new SpannableString("[전 출발] 혹은 [현재 이미 역을 지나침]");
			s.setSpan(new AbsoluteSizeSpan((int)(textSize/2)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spb.append(num).append(s);
			s = new SpannableString("\n대구버스 홈페이지 정보는 1분정도의 오차가 있을 수 있습니다");
			s.setSpan(new AbsoluteSizeSpan((int)(textSize/3)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spb.append(s);
		}

		return spb;
	}

	public String getStation() {
		return station;
	}

	public void setStation(String station) {
		this.station = station;
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

	public String getRoute() {
		return route;
	}

	public String getBusId() {
		return busId;
	}

	public void setBusId(String busId) {
		this.busId = busId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int) parcel 소포로 보내기 위해 쓰는 필수적 함수
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(station);
		dest.writeByte((byte) (soon ? 1 : 0));
		dest.writeString(busNum);
		dest.writeString(time);
		dest.writeString(current);
		dest.writeString(route);
		dest.writeString(busId);
	}

	public void readFromParcel(Parcel in) {
		station = in.readString();
		soon = in.readByte() != 0;
		busNum = in.readString();
		time = in.readString();
		current = in.readString();
		route = in.readString();
		busId = in.readString();

	}

	public static final Parcelable.Creator<BusInfoNet> CREATOR = new Creator<BusInfoNet>() {

		@Override
		public BusInfoNet createFromParcel(Parcel source) {
			return new BusInfoNet(source);
		}

		@Override
		public BusInfoNet[] newArray(int size) {
			return new BusInfoNet[size];
		}

	};
}
