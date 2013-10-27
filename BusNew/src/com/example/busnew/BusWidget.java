package com.example.busnew;

import internet.BusInfo;
import internet.ConnectBusTask;

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

public class BusWidget extends AppWidgetProvider {

	public static final String BUS_URL = "http://businfo.daegu.go.kr/ba/arrbus/arrbus.do?act=arrbus&winc_id=";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		SharedPreferences setting = context.getSharedPreferences(MainActivity.PREF_NAME, 0);
		String number = setting.getString("station_number", "error");
		
		Log.d("number", number);
		ConnectBusTask asyncBus = new ConnectBusTask();
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		
		if (asyncBus.isNetworkOn(context) && !number.equals("error")) {
			asyncBus.execute(BUS_URL + number);
			ArrayList<BusInfo> busInfo = null;
			try {
				busInfo = asyncBus.get();
			} catch (Exception e) {
				e.printStackTrace();
			}

			

			// 전 위젯을 강제로 업데이트
			// ComponentName thisWidget = new ComponentName(context,
			// BusWidget.class);
			// int[] allWidgetIds =
			// appWidgetManager.getAppWidgetIds(thisWidget);

			// 업데이트할 필요가 있는 위젯만 업데이트
			// onUpdate 의 appWidgetIds 는 업데이트 할 필요가 있는 위젯의 id만 가져옴
			// 전체가 될 수도 있고 1개가 될 수도 있음.
			// 그리고 처음 배치시에는 마지막으로 배치시키는 위젯 한개의 id만 가져옴. (나머진 방치)

			// for (int widgetId : appWidgetIds) {

			// Log.d("====widgetId===",""+widgetId);

			if (busInfo != null) {
				// 일단 첫번째 버스만 출력, 이후 원하는 버스번호만 골라서 출력하게 할거임.

				BusInfo bus = busInfo.get(busInfo.size() - 1);

				rv.setTextViewText(R.id.station, BusInfo.getStation());
				rv.setTextViewText(R.id.busNum, bus.getBusNum());
				rv.setTextViewText(R.id.time, bus.getTime());
				rv.setTextViewText(R.id.where, bus.getCurrent());
			} else {
				rv.setTextViewText(R.id.busNum, "버스운영시간이 아니거나 홈페이지에 장애가 발생하였습니다");
			}

			// appWidgetManager.updateAppWidget(widgetId, rv);

			// }

			// appWidgetManager.updateAppWidget(appWidgetIds, rv);

			
		} else {
			rv.setTextViewText(R.id.busNum, "인터넷 연결이 되어 있지 않습니다");
		}
		
		Intent refleshIntent = new Intent(context, BusWidget.class);
		refleshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		refleshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, refleshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		rv.setOnClickPendingIntent(R.id.reflesh, pending);
		
		// 그냥 componentName 쓰면 위의 for문따윈 없어도 모조리 업데이트
		appWidgetManager.updateAppWidget(new ComponentName(context, BusWidget.class), rv);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}
}
