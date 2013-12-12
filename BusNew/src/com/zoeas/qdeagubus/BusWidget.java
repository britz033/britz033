package com.zoeas.qdeagubus;

import internet.BusInfoNet;
import internet.ConnectTask;
import internet.ResponseTask;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class BusWidget extends AppWidgetProvider implements ResponseTask{

	private static final String TAG = "BusWidget";
	
	private Context context;
	private AppWidgetManager appw;
	private int[] ids;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		Toast.makeText(context, "위젯이 업데이트 되었습니다", 0).show();
		
		SharedPreferences setting = context.getSharedPreferences(MainActivity.PREF_NAME, 0);
		String stationNumber = setting.getString("station_number", "error");
		
		Log.d(TAG,"NUMBER " + stationNumber);
		ConnectTask busInfoTask = new ConnectTask(context, stationNumber); // 정보얻기 위한 task 실행
		// 테스크에서 소환한 인터페이스에 본클래스를 등록시켜서 저쪽이 이쪽을 호출 가능하도록 함.. 아래 onTaskFinish 가 그거
		busInfoTask.proxy = this;
		busInfoTask.execute();
		
		// 이후의 처리문은 혹시 싱크가 안맞을 것을 대비해서 onTaskfinish가 호출되고 난뒤부터 처리되게 함.. 그 함수에 넣는 다는 소리
		// 그걸 위해서 일단 변수들을 다 옮김
		this.context = context;
		appw = appWidgetManager;
		ids = appWidgetIds;
		
	}


	// 여기가 주력, 결과값과 함께 혹시나의 에러값
	@Override
	public void onTaskFinish(ArrayList<BusInfoNet> buslist, String error) {
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		if(error == null && buslist != null){
			
			// 일단 0, 후에 아예 즐겨찾기된것만 되게 바꿀거임
			BusInfoNet bus = buslist.get(0);
			rv.setTextViewText(R.id.widget_busNum, bus.getBusNum());
			rv.setTextViewText(R.id.widget_time, bus.getTime());
			rv.setTextViewText(R.id.widget_where, bus.getCurrent());
			rv.setTextViewText(R.id.widget_station, bus.getStation());
			
			Intent intent = new Intent(context, BusWidget.class);
			intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
			PendingIntent pd = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			rv.setOnClickPendingIntent(R.id.widget_reflesh, pd);
			
		} else if(error != null){
			rv.setTextViewText(R.id.widget_busNum, error);
		} else {
			rv.setTextViewText(R.id.widget_busNum, "정류장이나 버스가 설정되어 있지 않습니다");
		}
		
		appw.updateAppWidget(new ComponentName(context, BusWidget.class), rv);
	}
}
