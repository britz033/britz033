package internet;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

/*
 * <div class="arrlist">
 <p class="bsnm png">정류소명 : <span class="strong">영대병원역</span></p>
 <p class="viewmap"><a href="../route/route.do?act=main&amp;layout=mainArr&amp;arrBsInput1=영대병원역" onclick="window.open(this.href, 'map_window', 'left=0,top=0,width=1028,height=690,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes'); return false;">정류소위치</a><p>


 <ul>

 <li class="st"><span class="route_no"><span class="marquee">순환3-1</span></span><span class="arr_state">전</span><span class="cur_pos"><span class="marquee">출발</span></span></li>

 <li class="nm"><span class="route_no"><span class="marquee">604</span></span><span class="arr_state">8분</span><span class="cur_pos"><span class="marquee">대명초교</span></span></li>

 <li class="nm"><span class="route_no"><span class="marquee">452</span></span><span class="arr_state">11분</span><span class="cur_pos"><span class="marquee">한마음주유소</span></span></li>

 </ul>
 </div>

 <script>
 applyMarquee('marquee');
 </script>

 */

/*
 * 버스전광판 웹사이트 연결, 파싱, 그리고 각각의 버스정보를 
 * AsyncTask.get(task실행후 결과값=result)를 통해 busInfoArray배열로 리턴
 */

public class ConnectBusTask extends AsyncTask<String, Integer, ArrayList<BusInfo>> {
	ArrayList<BusInfo> busInfoArray;

	public ConnectBusTask() {
		busInfoArray = new ArrayList<BusInfo>();
	}
	
	public boolean isNetworkOn(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net = cm.getActiveNetworkInfo();
		if(net != null && net.isConnected()){
			return true;
		}
		return false;
	}

	@Override
	protected ArrayList<BusInfo> doInBackground(String... url) {
		try {
			urlConnect(url[0]);
			Log.d("url",url[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return busInfoArray;
	}

	// 연결
	private void urlConnect(String url) throws Exception {
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		HttpResponse response = client.execute(get);

		InputStream is = response.getEntity().getContent();

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlPullParser html = factory.newPullParser();

		html.setInput(is, "euc-kr");

		busParser(html);
	}
	
	/*
	 * <li class="st"><span class="route_no"><span
	 * class="marquee">순환3-1</span></span><span class="arr_state">전</span><span
	 * class="cur_pos"><span class="marquee">출발</span></span></li>
	 * 
	 * <li class="nm"><span class="route_no"><span
	 * class="marquee">604</span></span><span class="arr_state">8분</span><span
	 * class="cur_pos"><span class="marquee">대명초교</span></span></li>
	 */
	
	// 파싱
	private void busParser(XmlPullParser html) throws Exception {

		html.next(); // 문서의 시작은 넘어감
		int status = html.getEventType(); // 현재 parser가 가르키는 곳의 상황을 가져옴.문서의
											// 시작이라던지, 태그의 시작점이라던지..
		String tag = null; // 태그 이름
		BusInfo bus = null;
		boolean end_flag = false;

		// 문서끝에 닿을때까지 돌림
		while (status != XmlPullParser.END_DOCUMENT && !end_flag) {
			switch (status) {
			case XmlPullParser.START_TAG:
				tag = html.getName();
				Log.d("tagname", tag);
				// li 태그에 있는 모든 버스 정보를 차례로 빼냄
				if (tag.equals("li")) {
					
					String attr = html.getAttributeValue(0);
					bus = new BusInfo();
					
					if (attr.equals("st")) {
						bus.setSoon(true);
					} else if (attr.equals("nm")) {
						bus.setSoon(false);
					} else {
						busInfoArray = null;
						return;
					}

					
					
					nextCount(html, 3);
					bus.setBusNum(html.getText());
					Log.d("버스번호", html.getText());

					nextCount(html, 4);
					bus.setTime(html.getText());
					Log.d("버스시간", html.getText());

					nextCount(html, 4);
					bus.setCurrent(html.getText());
					Log.d("버스현재장소", html.getText());

					busInfoArray.add(bus);
				} else if (tag.equals("p")) {
					int count=html.getAttributeCount();
					if (count>0) {
						if (html.getAttributeValue(0).equals("bsnm png")) {
							html.next(); // <p class="bsnm png">정류소명 : <span
											// class="strong">영대병원역</span></p>
							html.next();
							html.next(); // 영대병원역
							bus.setStation(html.getText());
						}
					}
				}
				break;
			case XmlPullParser.END_TAG: // end_tag가 /ul 이면 while문 탈출
				tag = html.getName();
				Log.d("end_tag",tag);
				if(tag.equals("ul"))
					end_flag = true;
				break;
			default:
				Log.d("busParser() default", "text");
				break;
			}

			status = html.next();
		}

	}

	private void nextCount(XmlPullParser html, int count) throws Exception {
		for (int i = 0; i < count; i++) {
			html.next();
		}
	}

}
