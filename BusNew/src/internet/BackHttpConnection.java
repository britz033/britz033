package internet;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class BackHttpConnection {
	
	private InputStream is = null;

	public BackHttpConnection(Context context, String url) throws Exception{
		
//		 기기의 인터넷사용여부 확인후 연결
		if (isNetworkOn(context)) {
			urlConnect(url);
		} else 
			throw new Exception("인터넷에 연결되지 않은 상태입니다");

	}

	// 인터넷 연결을 확인하는 메소드
	private boolean isNetworkOn(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = cm.getActiveNetworkInfo();
		if (net != null && net.isConnected()) {
			return true;
		}
		return false;
	}

	private void urlConnect(String url) {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		try{
		HttpResponse response = client.execute(get);
		is = response.getEntity().getContent();
		} catch(Exception e){
			Log.d("http 연결", "실패했습니다");
		}
	}
	
	public InputStream getInputStream(){
			return is;
	}

}
