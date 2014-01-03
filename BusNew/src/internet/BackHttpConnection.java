package internet;

import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class BackHttpConnection {
	
	private InputStream is = null;

	public BackHttpConnection(Context context, String url) throws Exception {

		if (isNetworkOn(context)) {
			urlConnect(url);
		} else
			throw new Exception("인터넷에 연결되지 않은 상태입니다");

	}

	private boolean isNetworkOn(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = cm.getActiveNetworkInfo();
		if (net != null && net.isConnected()) {
			return true;
		}
		return false;
	}

	private void urlConnect(String url) throws Exception{
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		try {
			HttpParams params = client.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);
			HttpResponse response = client.execute(get);
			is = response.getEntity().getContent();
		} catch (ConnectTimeoutException e){
			throw new Exception("홈페이지에 부하가 걸려 접속이 원할 하지 않습니다. 다시 접속해주세요");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("인터넷 연결이 되어 있지 않습니다");
		}
	}

	public InputStream getInputStream() {
		return is;
	}

}
