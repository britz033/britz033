package com.example.busnew;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MyContentProvider extends ContentProvider{
	
	// db는 이미 openhelper에서 DB이름으로 열었으니 여기선 테이블 네임을 중심으로 하면됨.
	public static final String TABLE_NAME = "stationInfo";
	public static final Uri	CONTENT_URI = Uri.parse("content://com.example.providertest/stationInfo");
	
	public static final String CONTENT_TYPE = "vnd.android.curosr.dir/stationInfo";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.curosr.item/stationInfo";
	public static final int STATION_COLLECTION = 1;
	public static final int SINGLE_STATION = 2;
	
	private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
	static{
		// 각각의 상수와 매치 station_collection = uri, gettype메소드에서 matcher 쉽게 할려고 정의
		matcher.addURI("com.example.providertest", "stationInfo", STATION_COLLECTION);
		matcher.addURI("com.example.providertest", "stationInfo/#", SINGLE_STATION);
	}
	
	SQLiteDatabase db;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		if(matcher.match(uri) == STATION_COLLECTION){
			return CONTENT_TYPE;
		}
		if(matcher.match(uri) == SINGLE_STATION){
			return CONTENT_ITEM_TYPE;
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
		qb.setTables("stationInfo");
		// uri 문에서 content://com.example.providertest/stationInfo 여기까진 그대로고 
		// 꽁무니에 붙어 나오는 /# 머시기가 있다면 그걸로 추가검색문을 만들어준다. 
		// #는 그냥 뭔가 온다는 표시고 만약 /#/# 식으로 세분화된다면 그것도 알아서 만들어주거나 무시하면된다.
		// 자신이 원한다면 /#/#/# 이런것도 더욱 세밀하게 커스텀 할 수 있는듯. 어디까지나 원한다면
		// 뭐.. 이 쿼리 메소드자체가 이미 커스텀이지만.
		if(matcher.match(uri) == SINGLE_STATION){
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
		return 0;
	}
	
	

}
