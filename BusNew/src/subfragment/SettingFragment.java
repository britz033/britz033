package subfragment;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.zoeas.qdeagubus.MainActivity.OnBackAction;
import com.zoeas.qdeagubus.R;
import com.zoeas.util.CalculateC;


public class SettingFragment extends Fragment implements OnBackAction{
	
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.temp, null);
	    ExpandableListView elv = (ExpandableListView) view.findViewById(R.id.listtest);
	    elv.setAdapter(new BaseExpandableAdapter(getActivity()));
	    elv.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, 1001);
				return false;
			}
		});
	    elv.setOnGroupClickListener(new OnGroupClickListener() {
			
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempImageFile()));
				intent.putExtra("return-data", true);
				startActivityForResult(intent, 1002);
				return false;
			}
		});
	    
		return view;
	}
	
	@SuppressLint("NewApi") @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		BitmapDrawable bit = new BitmapDrawable(getScaledDrawable(getTempImageFile()));
		view.setBackground(bit);
	}
	
	private Bitmap getScaledDrawable(File tempImageFile) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;	// 비트맵이 로딩되기전 option 가능
		// 디코딩하지만 위에 생성을 막고 옵션만 받아오게 설정해놨기에 생성은 안되고 그 파일의 옵션(크기등)만 받아온다
		BitmapFactory.decodeFile(tempImageFile.getAbsolutePath(), options); 
		
		// 그 options으로 지지고 뽂고 한다.. 라고 일단 가정 그리고 사용이 끝났다고도 가정
			// 지지고 뽁음 - 가정
		// 이후 다시 옵션을 초기화
		
		options = new BitmapFactory.Options();
		options.inSampleSize = 6;
		
		Bitmap bitmap = BitmapFactory.decodeFile(tempImageFile.getAbsolutePath(), options);
		return bitmap;
		
	}

	private File getTempImageFile() {
		File path = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getActivity().getPackageName() + "/preview/");
		if (!path.exists()) {
			path.mkdirs();
		}
		File file = new File(path, "temp.png");
		return file;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClear() {
		// TODO Auto-generated method stub
		
	}
}

class BaseExpandableAdapter extends BaseExpandableListAdapter{
	
	Context context;
	
	public BaseExpandableAdapter(Context context){
		this.context = context;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		TextView tv = new TextView(context);
		tv.setText("챠챠 챠일드!!");
		
		return tv;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 2;
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		return null;
	}

	@Override
	public int getGroupCount() {
		return 10;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		TextView tv = new TextView(context);
		CalculateC c = new CalculateC();
		tv.setText("그룹입니다만");
		return tv;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}