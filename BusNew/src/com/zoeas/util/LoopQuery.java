package com.zoeas.util;

import java.io.Serializable;
import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

public class LoopQuery<T> {

	public static final int DEFAULT_LOOP_QUERY_ID = 25429;
	public static final int LOOP_QUERY_ID_2 = 34329;
	public static final int LOOP_QUERY_ID_3 = 57649;
	public static final String KEY = "loopQuery";

	private LoaderManager loaderManager;
	private LoaderCallbacks<Cursor> callback;
	private T[] sourceArray;
	private ArrayList<String[]> dataSet;
	private int length;
	private int count;
	private int id;
	private Bundle bundle;
	private boolean isUpdate;

	public LoopQuery(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> callback) {
		this.id = DEFAULT_LOOP_QUERY_ID;
		init(lm,sourceArray,callback);
	}

	public LoopQuery(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> callback, int id) {
		this.id = id;
		init(lm,sourceArray,callback);
		
	}
	
	private void init(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> callback){
		this.loaderManager = lm;
		this.callback = callback;
		this.bundle = new Bundle();
		this.length = sourceArray.length;
		this.sourceArray = sourceArray;
		this.dataSet = new ArrayList<String[]>();
		this.isUpdate = false;
		if (sourceArray != null && length == 0) {
		}
	}

	public void start() {
		count = 0;
		restart();
	}

	public void start(T[] changeResource) {
		count = 0;
		length = changeResource.length;
		sourceArray = changeResource;
		restart();
	}

	public void restart() {
		bundle.putSerializable(KEY, (Serializable) sourceArray[count++]);
		loaderManager.restartLoader(id, bundle, callback);
	}

	public boolean isEnd() {
		return (count < length) ? false : true;
	}

	public void addResultData(String... data) {
		dataSet.add(data);
	}

	public ArrayList<String[]> getResultData() {
		return dataSet;
	}

	public int getCount() {
		return count;
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

}
