package com.example.busnew;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;

class DbHelper extends SQLiteOpenHelper{

	public static final String DB_NAME = "StationDB2";
	Context mcontext;
	
	public DbHelper(Context context) {
		super(context, DB_NAME, null, 1);
		mcontext = context;
		copyDB();
		Log.d("dbhelper", "called");
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("dbhelper", "onCreate called!! 몬가 잘못되었삼");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	private void copyDB() {
		Log.d("copyDB", "start");
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

				Log.d("File dir, File", String.valueOf(dir.exists()) + ","
						+ String.valueOf(db_file.exists()));
			}
		} catch (Exception e) {
			Log.d("copyDB", "error");
			e.printStackTrace();
		}
		Log.d("copyDB", "complete");
	}

}
