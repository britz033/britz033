package com.zoeas.util;

import java.io.Serializable;
import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.util.Log;

/**
 * 
 * 로더를 사용시 반복쿼리를 간편하게 하기 위한 보조 클래스 기본적으로 한번만 사용하며 하나의 프레그먼트나 액티비티에서 사용시는 생성자에서 사용할 id를 입력해야 중복을 피할 수 있다.
 * 
 * @author lol
 * @param <T>
 *            소스배열를 입력받을때의 타입을 결정한다. 소스는 [] 배열타입이다
 */

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

	/**
	 * 
	 * @param lm
	 *            로더매니저
	 * @param sourceArray
	 *            소스자료
	 * @param callback
	 *            콜백인스턴스
	 */
	public LoopQuery(LoaderManager lm, T[] sourceArray, LoaderCallbacks<Cursor> callback) {
		this.id = DEFAULT_LOOP_QUERY_ID;
		init(lm,sourceArray,callback);
	}

	/**
	 * 
	 * @param lm
	 *            로더매니저
	 * @param sourceArray
	 *            소스자료
	 * @param callback
	 *            콜백인스턴스
	 * @param id
	 *            만약 하나의 프레그먼트나 액티비티에서 사용시 getId 에서의 구분을 위한 새로운 id를 부여해야한다
	 */
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
			Log.d("LoopQuery", "자료입력이 잘못되었습니다");
		}
	}

	/**
	 * 시작
	 */
	public void start() {
		count = 0;
		restart();
	}

	/**
	 * 새로운 객체할당없이 소스만 바꿔서 재사용이 가능하다
	 * 
	 * @param changeResource
	 */
	public void start(T[] changeResource) {
		count = 0;
		length = changeResource.length;
		sourceArray = changeResource;
		restart();
	}

	/**
	 * loaderManger.restartLoader 에 소스를 자동으로 입력하여 시작
	 */
	public void restart() {
		bundle.putSerializable(KEY, (Serializable) sourceArray[count++]);
		loaderManager.restartLoader(id, bundle, callback);
	}

	/**
	 * 루프가 완료됐는지를 반환
	 * 
	 * @return true시 완료
	 */
	public boolean isEnd() {
		return (count < length) ? false : true;
	}

	/**
	 * 결과값을 넣는다. 결과값은 String 형태이고 data1, data2.. 등으로 원하는 수만큼 입력이 가능
	 * 
	 * @param data
	 */
	public void addResultData(String... data) {
		dataSet.add(data);
	}

	/**
	 * 앞서 입력된 데이터를 반환한다
	 * @return	ArrayList<String[]>	addResultData를 부른 수만큼의 ArrayList가 반환된다
	 */
	public ArrayList<String[]> getResultData() {
		return dataSet;
	}

	/**
	 * 현재 카운터를 반환한다.   
	 * @return	1을 시작으로 하는 카운터를 반환한다. 배열index등으로 쓸려면 -1 을 해줘야한다
	 */
	public int getCount() {
		return count;
	}

//	/**
//	 * 생성자에서 입력된 소스중 sourceArray[count-1] 에 해당하는 값을 반환한다
//	 * 왜 넣었는지 스스로도 의문.. 일단 지우는거 보류
//	 * @return	
//	 */
//	public T getBundleData() {
//		return (T) bundle.get(KEY);
//	}

	/**
	 * 직접적인 쿼리로 오는 것인지 캐쉬로 로더가 다시 들어오는 것인지 판단한다
	 * @return
	 */
	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

}
