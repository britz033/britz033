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

public class BusInfoDownloaderTask extends
		AsyncTask<String, Integer, ArrayList<BusInfo>> {
	ArrayList<BusInfo> busInfoArray;
	
	private Context context;
	private String url;
	private ProgressDialog wait;
	public ResponseTask proxy = null;

	public BusInfoDownloaderTask(Context context, String url) {
		wait = ProgressDialog.show(context, null, "잠시만 기다려주세요", true);
		this.context = context;
		this.url = url;
	}

	@Override
	protected ArrayList<BusInfo> doInBackground(String... s) {
		ArrayList<BusInfo> busInfoList = new ArrayList<BusInfo>();
		
		InputStream is = new BackHttpConnection(context, url).getInputStream();
		XmlPullParser parser = new BackXmlParser(is, "euc-kr").getParser();
		BusStationParsing parsingWork = new BusStationParsing(parser, busInfoList);
		return busInfoArray;
	}
	
	@Override
	protected void onPostExecute(ArrayList<BusInfo> result) {
		super.onPostExecute(result);
		
		proxy.onTaskFinish(result);
		wait.dismiss();
		
	}


}
