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

/*
 * 각 버스들 정보
 * 도착까지 가까운지 여부, 번호, 시간 , 현재장소
 */
public class BusInfo implements Parcelable {
	private String station;
	private boolean soon;
	private String busNum;
	private String time;
	private String current;

	public BusInfo() {
		soon = false;
		busNum = "버스번호";
		time = "버스남은시간";
		current = "버스현재장소";
		station = "버스역";
	}

	public BusInfo(Parcel in) {
		readFromParcel(in);
	}

	public SpannableStringBuilder getSpannableStringBusInfo() {
		SpannableStringBuilder spb = new SpannableStringBuilder();

		if (soon) {
			SpannableString sp = new SpannableString("**다와감**");
			sp.setSpan(new ForegroundColorSpan(Color.RED), 0, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			spb.append(sp).append(" ");
		}
		
		//그냥 픽셀값이라 나중에 수정해야함
		SpannableString num = new SpannableString(busNum);
		num.setSpan(new AbsoluteSizeSpan(130), 0, num.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		num.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, num.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		spb.append(num).append(" ").append("[");
		spb.append(time).append("] ");
		spb.append(current).append(" ");
		spb.append("\n");

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 * 
	 * Parcel 하려는 오브젝트의 종류를 정의한다. 어떤 특별한 객체를 포함하고 있는지에 대한 설명을 리턴값으로 표현 하는 것이라고
	 * 보면된다. bit mask 형식의 integer를 리턴 하며,값을 체크 할 때 bit mask 체크를 해서 어떤 것들이 들어 있는지
	 * 알 수 있다.
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int) parcel
	 * 소포로 보내기 위해 쓰는 필수적 함수
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// private String station;
		// private boolean soon;
		// private String busNum;
		// private String time;
		// private String current;

		dest.writeString(station);
		dest.writeByte((byte) (soon ? 1 : 0));
		dest.writeString(busNum);
		dest.writeString(time);
		dest.writeString(current);
	}

	// 이건 필수가 아닌데 저 다음것에서 read 할려면 getter 써야하는게 귀찮아서..
	public void readFromParcel(Parcel in) {
		station = in.readString();
		soon = in.readByte() != 0;
		busNum = in.readString();
		time = in.readString();
		current = in.readString();
	}

	/*
	 * Parcel 에서 Parcelable 클래스의 인스턴스를 만들 때 CREATOR라는 static field를 찾아서 실행 합니다.
	 * CREATOR는 Parcelable.Creator<T> type 으로 만들어져야 하는데 이건 선언과 동시에 반드시
	 * initialize 되어야 합니다.
	 * 
	 * 클래스 따로 만들어서 initialize 하기도 쉽지 않습니다. 그냥 익명 클래스로 만들어 버립시다.
	 */
	public static final Parcelable.Creator<BusInfo> CREATOR = new Creator<BusInfo>() {

		// 읽기함수 원래 여기에 source.readString 이런식으로 써야한다. 근데 귀찮아서..
		@Override
		public BusInfo createFromParcel(Parcel source) {
			return new BusInfo(source);
		}

		// array로 만들때 반환되는 듯
		@Override
		public BusInfo[] newArray(int size) {
			return new BusInfo[size];
		}

	};
}
