package subfragment;

import com.google.android.gms.maps.model.LatLng;

public interface OnSaveBusStationInfoListener {
	public void OnSaveBusStationInfo(String station_number, String station_name,
			LatLng latLng);

	public void OnSaveBusStationInfo(String station_number, String station_name);
}