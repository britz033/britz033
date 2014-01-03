package com.zoeas.qdeagubus;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;


public class MyContentProvider extends ContentProvider{
	
	public static final String STATION_NUMBER = "station_number";
	public static final String STATION_NAME = "station_name";
	public static final String STATION_ID = "station_id";
	public static final String STATION_LATITUDE = "station_longitude";
	public static final String STATION_LONGITUDE = "station_latitude";
	public static final String STATION_FAVORITE = "station_favorite";
	public static final String STATION_PASS = "station_pass";
	public static final String PASS_FAVORITE = "pass_favorite";
	
	
	public static final String BUS_NUMBER = "bus_number";
	public static final String BUS_INTERVAL = "bus_interval";
	public static final String BUS_FORWARD = "bus_forward";
	public static final String BUS_BACKWARD = "bus_backward";
	public static final String BUS_ID = "bus_id";
	
	public static final String TABLE_NAME_STATION = "stationInfo";
	public static final String TABLE_NAME_BUS = "busInfo";
	
	public static final Uri	CONTENT_URI_STATION = Uri.parse("content://com.zoeas.qdeagubus/stationInfo");
	public static final Uri	CONTENT_URI_BUS = Uri.parse("content://com.zoeas.qdeagubus/busInfo");
	
	public static final String CONTENT_TYPE_STATION = "vnd.android.curosr.dir/stationInfo";
	public static final String CONTENT_ITEM_TYPE_STATION = "vnd.android.curosr.item/stationInfo";
	public static final int STATION_COLLECTION = 1;
	public static final int SINGLE_STATION = 2;
	
	public static final String CONTENT_TYPE_BUS = "vnd.android.curosr.dir/stationInfo";
	public static final String CONTENT_ITEM_TYPE_BUS = "vnd.android.curosr.item/stationInfo";
	public static final int BUS_COLLECTION = 3;
	public static final int SINGLE_BUS = 4;
	public static final int TABLE_STATION = 1001;
	public static final int TABLE_BUS = 1002;
	
	private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final UriMatcher selectTable = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		matcher.addURI("com.zoeas.qdeagubus", "stationInfo", STATION_COLLECTION);
		matcher.addURI("com.zoeas.qdeagubus", "stationInfo/#", SINGLE_STATION);
		
		matcher.addURI("com.zoeas.qdeagubus", "busInfo", BUS_COLLECTION);
		matcher.addURI("com.zoeas.qdeagubus", "busInfo/#", SINGLE_BUS);
		
		selectTable.addURI("com.zoeas.qdeagubus", "stationInfo/*", TABLE_STATION);
		selectTable.addURI("com.zoeas.qdeagubus", "busInfo/*", TABLE_BUS);
		selectTable.addURI("com.zoeas.qdeagubus", "stationInfo", TABLE_STATION);
		selectTable.addURI("com.zoeas.qdeagubus", "busInfo", TABLE_BUS);
	}
	
	SQLiteDatabase db;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		if(matcher.match(uri) == STATION_COLLECTION){
			return CONTENT_TYPE_STATION;
		}
		if(matcher.match(uri) == SINGLE_STATION){
			return CONTENT_ITEM_TYPE_STATION;
		}
		if(matcher.match(uri) == BUS_COLLECTION){
			return CONTENT_TYPE_BUS;
		}
		if(matcher.match(uri) == SINGLE_BUS){
			return CONTENT_ITEM_TYPE_BUS;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		DbHelper helper = new DbHelper(getContext());
		db = helper.getReadableDatabase();
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		if(selectTable.match(uri) == TABLE_STATION)
			qb.setTables(TABLE_NAME_STATION);
		else if (selectTable.match(uri) == TABLE_BUS)
			qb.setTables(TABLE_NAME_BUS);
		if(matcher.match(uri) == SINGLE_STATION || matcher.match(uri) == SINGLE_BUS){
			qb.appendWhere("_id=" + uri.getLastPathSegment());
		}
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		int rows = 0;
		String id = null;
		
		switch(matcher.match(uri)){
		case STATION_COLLECTION : 
			rows = db.update("stationInfo", values, selection, selectionArgs);
			break;
		case SINGLE_STATION :
			id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection) == true){
				rows = db.update("stationInfo", values, "_id=" + id, null);
			} else {
				rows = db.update("stationInfo", values, selection + " AND " + "_id=" + id, selectionArgs);
			}
			break;
		case BUS_COLLECTION :
			rows = db.update("busInfo", values, selection, selectionArgs);
			break;
		case SINGLE_BUS :
			id = uri.getLastPathSegment();
			if(TextUtils.isEmpty(selection) == true){
				rows = db.update("busInfo", values, "_id=" + id, null);
			} else {
				rows = db.update("busInfo", values, selection + " AND " + "_id=" + id, selectionArgs);
			}
			break;
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}
	
	

}
