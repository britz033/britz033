package com.zoeas.util;

import com.google.android.gms.maps.model.LatLng;

public abstract class MapAdjust {
	private static final double OFFSET = 268435456;
	private static final double RADIUS = OFFSET / Math.PI;

	public static LatLng xyToLL(int x, int y, LatLng latLng, int z) {
		return xyToLL(x, y, latLng.longitude, latLng.latitude, z);
	}

	public static LatLng xyToLL(int x, int y, double lon, double lat, int z) {
		double resultX = xToLon(lonToX(lon) + (x << (21 - z)));
		double resultY = yToLat(latToY(lat) + (y << (21 - z)));
		return new LatLng(resultY, resultX);
	}

	private static long lonToX(double lon) {
		return Math.round(OFFSET + RADIUS * Math.toRadians(lon));
	}

	private static long latToY(double lat) {
		return Math.round(OFFSET - RADIUS
				* (Math.log((1 + Math.sin(Math.toRadians(lat))) / (1 - Math.sin(Math.toRadians(lat))))) / 2);
	}

	private static double xToLon(double x) {
		return Math.toDegrees((Math.round(x) - OFFSET) / RADIUS);
	}

	private static double yToLat(double y) {
		return Math.toDegrees(Math.PI / 2 - 2 * Math.atan(Math.exp((Math.round(y) - OFFSET) / RADIUS)));
	}
}
