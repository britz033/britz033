package subfragment;

import com.google.android.gms.maps.model.LatLng;

public interface OnBusStationInfoListener {
	public void OnBusStationInfo(String station_number, String station_name,
			LatLng latLng);

	public void OnBusStationInfo(String station_number, String station_name);
}