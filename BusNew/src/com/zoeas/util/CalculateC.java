package com.zoeas.util;

public class CalculateC {

	public native double getData(double p);

	public native double setData(double l);

	static {
		System.loadLibrary("calculate_db");
	}
}
