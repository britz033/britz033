package internet;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


/*
 * BackHttpConnection, BackXmlParser,BusStationParsing  클래스로
 * 각각 인터넷 연결, xml 파싱, 파싱된 데이터 처리를 담당한다. 
 * 각자가 커스텀 익셉션을 소유하고 있으며 그 익셉션의 메세지를 추출하여 그대로 TextView에서 사용
 * 결과값과 에러메세지는 ResponseTask proxy.onFinishTask 인터페이스를 통해 
 * 테스크가 끝난후 전달되며 이때 에러메세지가 null 일 경우는 에러메세지 대신 일반적 데이터를 표시한다.
 */
public class BusInfoDownloaderTask extends
		AsyncTask<String, Integer, ArrayList<BusInfo>> {
	ArrayList<BusInfo> busInfoArray;
	
	public static final String BUS_URL = "http://businfo.daegu.go.kr/ba/arrbus/arrbus.do?act=arrbus&winc_id=";

	private Context context;
	private String stationNumber;
	private ProgressDialog wait;
	private String errorMessage = null;
	public ResponseTask proxy = null;
	

	public BusInfoDownloaderTask(Context context, String station_number) {
		wait = ProgressDialog.show(context, null, "잠시만 기다려주세요", true);
		this.context = context;
		this.stationNumber = station_number;
	}

	@Override
	protected ArrayList<BusInfo> doInBackground(String... s) {
		ArrayList<BusInfo> busInfoList = new ArrayList<BusInfo>();
		
		try{
		InputStream is = new BackHttpConnection(context, BUS_URL + stationNumber).getInputStream();
		XmlPullParser parser = new BackXmlParser(is, "euc-kr").getParser();
		BusStationParsing parsingWork = new BusStationParsing(parser, busInfoList);
		}catch(Exception e){
			errorMessage = e.getMessage();
			Toast.makeText(context, ""+errorMessage, 0).show();
		}
		return busInfoArray;
	}
	
	@Override
	protected void onPostExecute(ArrayList<BusInfo> result) {
		super.onPostExecute(result);
		
		proxy.onTaskFinish(result, errorMessage);
		wait.dismiss();
		
	}


}
