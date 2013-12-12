package com.zoeas.qdeagubus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/*
 * 컨텐트 프로바이더용
 * 만약 DB가 없으면 asset에서 가져와 생성한다
 */
class DbHelper extends SQLiteOpenHelper{
	
	private static final String TAG = "DbHelper";

	public static final String DB_NAME = "StationDB.png";
	Context mcontext;
	
	public DbHelper(Context context) {
		super(context, DB_NAME, null, 1);
		mcontext = context;
		copyDB();
		Log.d(TAG, "called");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate called!! 몬가 잘못되었삼");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	private void copyDB() {
		Log.d(TAG, "copyDB start");
		try {
			final String DIR_PATH = "/data/data/" + mcontext.getPackageName()
					+ "/databases/";
			File dir = new File(DIR_PATH);
			File db_file = new File(DIR_PATH + DB_NAME);

			if (!db_file.exists()) {
				if (!dir.exists()) {
					dir.mkdir();
				}

				InputStream is = mcontext.getAssets().open(DB_NAME);
				FileOutputStream fos = new FileOutputStream(db_file);

				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) != -1) {
					fos.write(buffer, 0, length);
				}
				fos.flush();

				is.close();
				fos.close();

				Log.d(TAG, "File dir, File" + String.valueOf(dir.exists()) + ","
						+ String.valueOf(db_file.exists()));
			}
		} catch (Exception e) {
			Log.d(TAG, "copyDB error");
			e.printStackTrace();
		}
		Log.d(TAG, "copyDB complete");
	}

}
