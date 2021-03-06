package sub.favorite;

import internet.BusInfoNet;
import internet.ConnectTask;
import internet.ResponseTask;

import java.io.File;
import java.util.ArrayList;

import adapter.OnCommunicationActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.MyContentProvider;
import com.zoeas.qdeagubus.R;
import com.zoeas.util.ImageUtil;
import com.zoeas.util.LoopQuery;

/* 뷰페이저에 있는 미리 저장된 즐겨찾기중 하나를 선택하면
 * 그것을 전광판에 뿌림. 시작시는 가장 처음것(이후 다시 수정)을 뿌림
 * 
 * 전광판과 번호+옵션이 일치하면 그대로 뿌리고 
 * 일치하지 않을 경우 번호만 취해서 
 * 버스검색으로 날려버림
 * 
 * 버스즐겨찾기의 경우 
 * 대표즐겨찾기 (그 번호의 모든 방면)
 * 상세즐겨찾기
 */

public class FavoriteFragment extends Fragment implements ResponseTask, LoaderCallbacks<Cursor>, OnBackAction {

	private static final String TAG = "FavoriteFragment";

	public static final String KEY_BUS_NET_INFO_LIST = "qbus.busNETlist";
	public static final String KEY_BUS_INFO_LIST = "qbus.buslist";
	public static final String KEY_STATION_NAME = "qbus.station";
	public static final String KEY_STATION_ID = "qbus.stationID";
	public static final String KEY_PASS_FAVORITE = "qbus.passFavorite";
	public static final String KEY_ERROR = "error";

	private Context context;
	private Cursor cursor;
	private String stationNum;
	private String stationName;
	private String stationID;
	private String stationPass; // 이 정류장을 거치는 버스들 id
	private String pass_favorite; // 그 버스들의 즐겨찾기여부
	private ArrayList<BusInfo> busInfoList;
	private String[] dbPassBusId;
	private View view;
	private FavoritePreviewPagerAdapter adapter;
	private ViewPager pager;
	private LoopQuery<String> loopQueryBus;
	private float density;
	private boolean isFirst;
	private FavoriteFragmentBusList busListFragment;
	private ProgressBar loadingBar;
	private File savedFile;
	private ImageButton btnReflash;
	private ImageButton btnFavorite;
	private ImageButton btnChangePicture;
	private ImageButton btnDelete;
	private ImageUtil imageUtil;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRetainInstance(true); 유보는 액티비티당 하나만 되는듯. FragmentViewPager의 경우는
		// 안되는듯
		density = context.getResources().getDisplayMetrics().density;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		isFirst = true;
		imageUtil = new ImageUtil(context);
		view = inflater.inflate(R.layout.fragment_favorite_layout, container, false);
		// getLoaderManager().initLoader(0, null, this);
		loadingBar = (ProgressBar) view.findViewById(R.id.progressbar_favorite_buslist_loading);
		btnReflash = (ImageButton) view.findViewById(R.id.btn_testreflash);
		btnFavorite = (ImageButton) view.findViewById(R.id.btn_favorite_bus_check_open);
		btnChangePicture = (ImageButton) view.findViewById(R.id.btn_favorite_bus_peekup);
		btnDelete = (ImageButton) view.findViewById(R.id.btn_delete);
		
		ButtonSetting();
		viewPagerSetting(view);

		return view;
	}

	private void ButtonSetting() {
		btnReflash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cursor.moveToPosition(pager.getCurrentItem());
				settingInfo();
			}
		});

		btnFavorite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				busListFragment.onDialogOpen();
			}
		});

		btnChangePicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				ArrayList<String> data = new ArrayList<String>();
				data.add("갤러리에서 가져오기");
				data.add("카메라로 찍기");

				ArrayAdapter<String> menu = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, data);

				new AlertDialog.Builder(context).setNeutralButton("취소", null)
						.setAdapter(menu, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								//프리뷰 이미지뷰의 크기를 가져와서 저장
								ImageView iv = adapter.getImageView();
								int height = iv.getMeasuredHeight();
								int width =iv.getMeasuredWidth();
								imageUtil.setImageSizeBoundary(Math.max(height, width), 2);
								
								// 프리뷰파일의 경로와 파일명을 던져줌(아직 생성은 안된상태, 그래서 현재 존재하더라도 클릭만으론 안바뀜)
								savedFile = createPreviewFile();
								imageUtil.setTargetFile(savedFile);  // 처리를 위해 등록

								switch (which) {
								case 0:
									Intent i = new Intent(Intent.ACTION_PICK);
									i.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
									i.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
									startActivityForResult(i, 2001);
									break;
								case 1:
									Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
									intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(savedFile));
									intent.putExtra("return-data", true);
									startActivityForResult(intent, 2002);
									break;
								}
							}

						}).create().show();
			}
		});
		
		btnDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(context).setTitle("현재 정류장을 즐겨찾기에서 삭제하시겠습니까?")
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SQLiteDatabase db = SQLiteDatabase.openDatabase(context.getDatabasePath("StationDB.png").getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);
						ContentValues cv = new ContentValues();
						cv.put("station_favorite", 0);
						db.update(MyContentProvider.TABLE_NAME_STATION, cv, "station_id=" + stationID, null);
						db.close();
						adapter.notifyDataSetChanged();
						refrashPreview();
						OnCommunicationActivity refrashSearchList = (OnCommunicationActivity)getActivity();
						refrashSearchList.OnFavoriteRefresh();
					}
				})
				.setNegativeButton("취소", null).create().show();
			}
		});
	}

	private File createPreviewFile() {
		File path = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName()
				+ "/preview/");

		if (!path.exists()) {
			path.mkdirs();
		}

		File saveImage = new File(path, stationNum + ".png");
		return saveImage;
	}

	// 사진변경 메뉴선택 반환
	// 등록된 파일명과 경로에 이미지뷰의 크기에 맞게 사이즈 변경 + 저장
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		

		if (resultCode == Activity.RESULT_OK) {
			switch (requestCode) {
			case 2001:
				
				imageUtil.startGallery(data.getData());

				break;
			case 2002:

				imageUtil.startCamera();

				adapter.notifyDataSetChanged();
				break;
				default : 
					super.onActivityResult(requestCode, resultCode, data);
			}
		}

	}

	// 여기서 정류장 번호를 받는다.
	// 즐겨찾기된 그림들을 DB에서 받아온다 그 정보는 어뎁터 내에 집어넣는다.
	private void viewPagerSetting(final View view) {

		int dip = (int) (density * 30);

		PagerTitleStrip titlepager = (PagerTitleStrip) view.findViewById(R.id.pager_title_strip);
		titlepager.setTextSpacing(dip);

		adapter = new FavoritePreviewPagerAdapter(context, null);
		pager = (ViewPager) view.findViewById(R.id.viewpager_favorite);
		pager.setAdapter(adapter);

		final ViewPager dummy = (ViewPager) view.findViewById(R.id.viewpager_favorite_preview_dummy);
		FavoriteDummyPagerAdapter dummyAdapter = new FavoriteDummyPagerAdapter(adapter, context);
		dummy.setAdapter(dummyAdapter);
		dummy.setOffscreenPageLimit(7);
		dummy.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		// 미리보기에 쓰일 쿼리를 가져온다 and 미리보기를 표시할 아탑터를 세팅한다
		OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// 페이지가 선택되면 선택된 번호로 커서를 이동시켜 정류소 번호를 가져온다음 showInfo로 보내준다
				cursor.moveToPosition(position);
				settingInfo();
				Log.d(TAG, "onPageSelected");
				dummy.setCurrentItem(position, true);
			}

			public void onPageScrolled(int position, float offset, int positionOffsetPixels) {
				// dummy.scrollTo((int)offset, 0);
				// currentPos = arg0;
			}

			public void onPageScrollStateChanged(int arg0) {
				// dummy.setCurrentItem(currentPos,false);
			}
		};

		pager.setOnPageChangeListener(onPageChangeListener);
//		pager.setOffscreenPageLimit(5);
//		pager.setClipChildren(false);

		adapter.setDummy(dummyAdapter);

		Log.d(TAG, "페이저세팅 끝");
	}

	/*
	 * 받은 정류소 번호를 이용하여 ConnectTask 로 인터넷 연결을 수행하고 그 결과값을 받을 인터페이스를 등록한다
	 */
	private void showInfo(String stationNum) {

		ConnectTask busInfoTask = new ConnectTask(context, stationNum);
		busInfoTask.proxy = this; // 인터페이스를 등록하여 Async의 작업이 끝나면 onTaskFinish 함수를
									// 호출 가능케 한다

		busInfoTask.execute();

	}

	private void loadingVisible(int visibility) {
		loadingBar.setVisibility(visibility);
	}

	/*
	 * 위에서 인터넷에서 버스정보 가져오기 작업이 끝났을때 호출되는 인터페이스, 최종결과를 뿌린다 주의점 !!!! commit가 아니라
	 * commitAllowingStateLoss를 써야만 에러가 나지 않는다. 이부분은 다시 찾아볼것
	 */
	@Override
	public void onTaskFinish(ArrayList<BusInfoNet> list, String error) {
		if (loopQueryBus != null) {
			loopQueryBus.setUpdate(true);
		}
		loadingVisible(View.INVISIBLE);
		busListFragment = new FavoriteFragmentBusList();
		Bundle initData = new Bundle();

		// 각종오류 감지 및 정보뿌림
		if (error == null && list != null) {
			// 정상일 경우
			btnFavorite.setVisibility(View.VISIBLE);
			btnChangePicture.setVisibility(View.VISIBLE);
			initData.putParcelableArrayList(KEY_BUS_NET_INFO_LIST, list);
		} else if (error != null) {
			// error 메세지가 있을 경우
			btnFavorite.setVisibility(View.INVISIBLE);
			btnChangePicture.setVisibility(View.INVISIBLE);
			initData.putString(KEY_ERROR, error);
		} else if (busInfoList == null) {
			btnFavorite.setVisibility(View.INVISIBLE);
			btnChangePicture.setVisibility(View.INVISIBLE);
			initData.putString(KEY_ERROR, "정보를 찾을 수 없습니다.");
		}

		initData.putParcelableArrayList(KEY_BUS_INFO_LIST, busInfoList);
		initData.putString(KEY_STATION_NAME, stationName);
		initData.putString(KEY_STATION_ID, stationID);
		initData.putString(KEY_PASS_FAVORITE, pass_favorite);

		// 리스트뷰로 보내는 버스의 모든정보
		busListFragment.setArguments(initData);

		FragmentManager fm = getChildFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.fragment_favorite_buslist, busListFragment);
		ft.addToBackStack(null);
		ft.commitAllowingStateLoss();
	}

	public void refrashPreview() {
		getLoaderManager().restartLoader(0, null, this);
	}

	// 리스트뷰를 보여주기 위한 시작, 루프의 시작
	public void settingInfo() {
		cursor.moveToPosition(pager.getCurrentItem());
		loadingVisible(View.VISIBLE);
		if (cursor != null && cursor.getCount() > 0) {
			stationNum = cursor.getString(0);
			stationName = cursor.getString(1);
			stationPass = cursor.getString(2);
			pass_favorite = cursor.getString(3);
			stationID = cursor.getString(4);
		} else {
			new AlertDialog.Builder(context).setTitle("정보 갱신에 문제가 있습니다").setIcon(android.R.drawable.ic_dialog_alert)
					.create().show();
		}

		busInfoList = new ArrayList<BusInfo>();

		// 모든 버스 id는 10자리 쉼표포함해서 11자리, 마지막은 쉼표가 없으니 + 1, 해서 모든 버스id에서 버스정보를 추출한다
		if (stationPass.length() != 0) {
			dbPassBusId = stationPass.split(",");

			Log.d(TAG, "버스숫자 " + dbPassBusId.length);

			loopQueryBus = new LoopQuery<String>(getLoaderManager(), dbPassBusId, this);
			loopQueryBus.start();
		} else {
			// 이 역에는 암것도 없음.. 혹시나 에러날 가능성 좀 있으니 후에 체크
			showInfo(stationNum);
		}
	}

	// 즐겨찾기 미리보기를 할 즐겨찾기된 스테이션을 모두 검색, 이름,번호, 그리고 등록된 버스들
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle data) {
		Uri uri = null;
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;

		switch (id) {
		case 0:
			if (isFirst) {
				isFirst = false;
			}
			uri = MyContentProvider.CONTENT_URI_STATION;
			projection = new String[] { MyContentProvider.STATION_NUMBER, MyContentProvider.STATION_NAME,
					MyContentProvider.STATION_PASS, MyContentProvider.PASS_FAVORITE, MyContentProvider.STATION_ID };
			selection = MyContentProvider.STATION_FAVORITE + "=?";
			selectionArgs = new String[] { "1" };
			break;
		case LoopQuery.DEFAULT_LOOP_QUERY_ID:
			loopQueryBus.setUpdate(false);
			uri = MyContentProvider.CONTENT_URI_BUS;
			projection = new String[] { MyContentProvider.BUS_ID, MyContentProvider.BUS_NUMBER};
			selection = MyContentProvider.BUS_ID + "=" + data.getString(LoopQuery.KEY);
			break;
		}

		return new CursorLoader(context, uri, projection, selection, selectionArgs, null);
	}

	// 검색이 끝나면 settingInfo에서 찾은 정보를 세팅하고 그것들을 showInfo에서 보여줌
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		if (isFirst && loader.getId() != 0) {
			return;
		}

		switch (loader.getId()) {
		case 0:
			Log.d(TAG, "스왑작동");
			adapter.swapCursor(cursor);
			this.cursor = cursor;
			if (cursor.getCount() == 0){
				btnDelete.setVisibility(View.INVISIBLE);
				btnReflash.setVisibility(View.INVISIBLE);
				onTaskFinish(null, getResources().getString(R.string.favorite_first));
			}
			else {
				btnReflash.setVisibility(View.VISIBLE);
				btnDelete.setVisibility(View.VISIBLE);
				cursor.moveToPosition(pager.getCurrentItem());
				settingInfo();
			}
			break;
		case LoopQuery.DEFAULT_LOOP_QUERY_ID:
			if (loopQueryBus.isUpdate()) {
				return;
			}

			/*
			 * for (int i = 0; i < busCount; i++) {
			 * bus.putString(KEY_STATION_PASS, passBus[i]);
			 * getLoaderManager().restartLoader(1, bus, this); } 이런식으로
			 * restartLoader를 단번에 같은 id로 여러개를 호출하면 무한로딩에 들어감..;;
			 */
			// 결국.. 쿼리가 끝나면 하나 다시 restart 하고 하는 수밖에..ㅠㅠ;;

			// 버스번호가 검색이 안될때는 건너뜀 (앞으로의 예정버스임)
			if (cursor.getCount() != 0) {
				cursor.moveToFirst();
				BusInfo busInfo = new BusInfo();
				busInfo.setBusId(cursor.getString(0));
				busInfo.setBusName(cursor.getString(1));
				busInfoList.add(busInfo);
			}

			if (!loopQueryBus.isEnd()) {
				loopQueryBus.restart();
			} else {
				showInfo(stationNum);
			}
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	@Override
	public void onBackPressed() {

	}

	@Override
	public void onClear() {
		// TODO Auto-generated method stub

	}
}
