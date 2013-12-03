package util;

import java.io.Serializable;
import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;

public class LoopQuery<T> {
	
	public static final int LOOP_QUERY = 25429;
	public static final String KEY = "loopQuery";
	
	private LoaderManager loaderManager;
	private LoaderCallbacks<Cursor> callback;
	private T[] sourceArray;
	private ArrayList<String[]> dataSet; 
	private int length;
	private int count;
	private Bundle bundle;
	
	public LoopQuery(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> instance){
		loaderManager = lm;
		callback = instance;
		bundle = new Bundle();
		length = sourceArray.length;
		this.sourceArray = sourceArray;
		this.dataSet = new ArrayList<String[]>();
		if(sourceArray != null && length==0){
			Log.d("LoopQuery","자료입력이 잘못되었습니다");
		}
	}
	
	// 스타트먼저 해줘야함
	public void start(){
		count = 0;
		restart();
	}
	
	public void restart(){
		bundle.putSerializable(KEY, (Serializable) sourceArray[count++]);
		loaderManager.restartLoader(LOOP_QUERY, bundle, callback);
	}
	
	public boolean isEnd(){
		if(count<length){
			return false;
		}
		return true;
	}
	
	public void addResultData(String... data){
		dataSet.add(data);
	}
	
	public int getCount(){
		return count;
	}
	
	public ArrayList<String[]> getResultData(){
		return dataSet;
	}
	
	public T getBundleData(){
		return (T) bundle.get(KEY);
	}
	
	
	
}
