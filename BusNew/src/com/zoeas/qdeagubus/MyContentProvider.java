package com.zoeas.qdeagubus;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


/*
 * http://developer.android.com/guide/topics/providers/content-provider-creating.html
 */
public class MyContentProvider extends ContentProvider{
	
	// 테이블의 모든 이름
	
	public static final String STATION_NUMBER = "station_number";
	public static final String STATION_NAME = "station_name";
	public static final String STATION_LATITUDE = "station_longitude";
	public static final String STATION_LONGITUDE = "station_latitude";
	public static final String STATION_FAVORITE = "station_favorite";
	public static final String STATION_PASS = "station_pass";
	
	public static final String BUS_NUMBER = "bus_number";
	public static final String BUS_INTERVAL = "bus_interval";
	public static final String BUS_FORWARD = "bus_forward";
	public static final String BUS_BACKWARD = "bus_backward";
	public static final String BUS_FAVORITE = "bus_favorite";
	public static final String BUS_ID = "bus_id";
	
	// db는 이미 openhelper에서 DB이름으로 열었으니 여기선 테이블 네임을 중심으로 하면됨.
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
		// 각각의 상수와 매치 station_collection = uri, gettype메소드에서 matcher 쉽게 할려고 정의
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
		
		// sql문 조립식 빌더다 
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		// 테이블을 설정한다. 리졸버도 그렇지만 테이블은 uri에 명시했으므로 실제 쿼리문에서는
		// 테이블은 안들어간다. 고로 미리 설정해야된다. 
		if(selectTable.match(uri) == TABLE_STATION)
			qb.setTables(TABLE_NAME_STATION);
		else if (selectTable.match(uri) == TABLE_BUS)
			qb.setTables(TABLE_NAME_BUS);
		// uri 문에서 content://com.example.providertest/stationInfo 여기까진 그대로고 
		// 꽁무니에 붙어 나오는 /# 머시기가 있다면 그걸로 추가검색문을 만들어준다. 
		// #는 그냥 뭔가 온다는 표시고 만약 /#/# 식으로 세분화된다면 그것도 알아서 만들어주거나 무시하면된다.
		// 자신이 원한다면 /#/#/# 이런것도 더욱 세밀하게 커스텀 할 수 있는듯. 어디까지나 원한다면
		// 
	    // *: Matches a string of any valid characters of any length.
	    // #: Matches a string of numeric characters of any length.

		if(matcher.match(uri) == SINGLE_STATION || matcher.match(uri) == SINGLE_BUS){
			qb.appendWhere("_id=" + uri.getLastPathSegment());
		}
		
		// 리졸버랑 매우 닮았다. 그대로 넣어주고 나머지 group by니 having 이니는 입맛대로. 여기선 null
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		//데이터 변경시 컨텐트리졸버의 리스너가 통보받을 수 있도록 해준다
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
		
		// 데이터가 바뀌었다고 부른곳에 통지한다. Loader 가 있다면 onLoaderFinished가 불려짐
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}
	
	

}
