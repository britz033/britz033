package com.zoeas.qdeagubus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DbHelper extends SQLiteOpenHelper{
	
	public static final String DB_NAME = "StationDB.png";
	Context mcontext;
	
	public DbHelper(Context context) {
		super(context, DB_NAME, null, 1);
		mcontext = context;
		copyDB();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	private void copyDB() {
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

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
