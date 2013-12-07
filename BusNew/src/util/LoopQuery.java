package util;

import java.io.Serializable;
import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;

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
	
	
	public LoopQuery(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> callback){
		loaderManager = lm;
		this.callback = callback;
		bundle = new Bundle();
		length = sourceArray.length;
		this.id = DEFAULT_LOOP_QUERY_ID;
		this.sourceArray = sourceArray;
		this.dataSet = new ArrayList<String[]>();
		if(sourceArray != null && length==0){
			Log.d("LoopQuery","자료입력이 잘못되었습니다");
		}
	}
	
	public LoopQuery(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> instance, int id){
		loaderManager = lm;
		callback = instance;
		bundle = new Bundle();
		length = sourceArray.length;
		this.id = id;
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
	
	public void start(T[] changeResource){
		count = 0;
		length = changeResource.length;
		sourceArray = changeResource;
		restart();
	}
	
	
	public void restart(){
		bundle.putSerializable(KEY, (Serializable) sourceArray[count++]);
		loaderManager.restartLoader(id, bundle, callback);
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
	
	// 현재 루프수 index보단 1 큼
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
