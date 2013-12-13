package sub.favorite;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;

public abstract class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {

	private Cursor mCursor;

	public SQLiteCursorLoader(Context context) {
		super(context);
	}

	protected abstract Cursor loadCursor();

	/**
	 * 스레드작업. 로더에 의해 publish 되는 새로운 데이터를 
	 * 만드는 곳, 이미지를 로딩하는 거면 이미지, 커서로딩이면 커서, 다른 데이터면 다른 데이터를
	 * 만드는 작업을 하고 결과물을 보낸다. deliverResult를 이벤트 콜한다.
	 */
	@Override
	public Cursor loadInBackground() {
		Cursor cursor = loadCursor();
		if (cursor != null) {
			cursor.getCount();
		}
		return cursor;
	}


	/**
	 * 만약 새로운 결과물이 도착한다면 보내는 작업. UI에서 작업이랄까 배달?
	 * super가 보내는 로직 뭐 컴플리트 로더 어쩌구 한개 있지만.., 만약 로더가 isStart 되었다면 데이터를 보낼 수 있으므로
	 * 바로 배달. 이때 결과물이 꼭 새로운 결과물이 아니라 
	 * 캐쉬된 결과물일 수가 있는데 그런 캐쉬된 결과물이라면 
	 * 그대로 보내고 아예 새로운게 왔다면 이전에 캐쉬된 m 멤버는 릴리즈시켜준다 
	 */
	@Override
	public void deliverResult(Cursor data) {
		Cursor oldCursor = mCursor;
		mCursor = data;

		// 시작된거니 배달가능
		if (isStarted()) {
			super.deliverResult(data);
		}
		
		// 새로운게 있으면 과거거 지우고 없으면 걍 씀
		if(oldCursor != null && oldCursor != data && !oldCursor.isClosed()){
			oldCursor.close();
		}
	}

	/**
	 * 로더 시작시킬때, 반드시 구현해줘야함, 직접 불리는건 아니고 startLoading에서 변수 몇개 초기화후 불림, super는 텅텅빔
	 * 아마 get로더매니저한테 init 나 restart나 이럴때 불릴듯?
	 */
	@Override
	protected void onStartLoading() {
		// 이미 캐쉬된거 가지고 있다면 바로 배달
		if(mCursor != null){
			deliverResult(mCursor);
		}
		
		// 내용이 바뀌었거나 캐쉬된게 없으면 커서가 없으면 강제로딩(텅텅비었지만) 새로 만드는걸로 넣어줘야겠.. 
		if(takeContentChanged() || mCursor == null){
			forceLoad();
		}
		
	}

	/**
	 * 로더 정지시킬때, 이하동문
	 */
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}
	
	@Override
	public void onCanceled(Cursor cursor) {
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
	}

	/**
	 * 로더 리셋시킬때, 이하동문
	 */
	@Override
	protected void onReset() {
		// 로더 중단 확인, 중단아니면 중단시키고..
		onStopLoading();
		
		// 저장된 커서 닫고, 레퍼런스 해제
		if(mCursor != null && !mCursor.isClosed()){
			mCursor.close();
		}
		mCursor = null;
	}

}
