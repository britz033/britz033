package com.zoeas.util;

public class CalculateC {

	public native double getData1(double lat, int id);

	public native double getData2(double lon, int id);

	static {
		System.loadLibrary("calculate_db");
	}
}
