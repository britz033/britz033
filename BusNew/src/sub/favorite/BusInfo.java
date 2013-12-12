package sub.favorite;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * 다른것을 재검색할 수 있는 각종 키워드가 포함된 버스정보. 경유 정거장 제외
 */
public class BusInfo implements Parcelable{
	
	private String busName;
	private String busId;
	private String busNum;
	private String busOption;
	private int busFavorite;
	
	public BusInfo(){
		busName = null;
		busId = null;
		busNum = null;
		busOption = "";
		busFavorite = 0;
	}
	
	public BusInfo(Parcel in) {
		busName = in.readString();
		busId = in.readString();
		busNum = in.readString();
		busOption = in.readString();
		busFavorite = in.readInt();
	}

	public String getBusName() {
		return busName;
	}
	public void setBusName(String busName) {
		String[] sp = busName.split(" ");
		if(sp.length<2){
			busNum = busName;
		} else {
			busNum = sp[0];
			busOption = sp[1];
		}
		this.busName = busName;
	}
	public String getBusId() {
		return busId;
	}
	public void setBusId(String busId) {
		this.busId = busId;
	}
	public String getBusNum() {
		return busNum;
	}
	public void setBusNum(String busNum) {
		this.busNum = busNum;
	}
	public String getBusOption() {
		return busOption;
	}
	public void setBusOption(String busOption) {
		this.busOption = busOption;
	}
	public int getBusFavorite() {
		return busFavorite;
	}
	public void setBusFavorite(int busFavorite) {
		this.busFavorite = busFavorite;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/*
	  	private String busName;
		private String busId;
		private String busNum;
		private String busOption;
		private int busFavorite;(non-Javadoc)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(busName);
		dest.writeString(busId);
		dest.writeString(busNum);
		dest.writeString(busOption);
		dest.writeInt(busFavorite);
	}

	public static final Parcelable.Creator<BusInfo> CREATOR = new Parcelable.Creator<BusInfo>() {

		@Override
		public BusInfo createFromParcel(Parcel in) {
			return new BusInfo(in);
		}

		@Override
		public BusInfo[] newArray(int size) {
			return new BusInfo[size];
		}
	};
	
	
	
}
