package internet;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.os.AsyncTask;

public class ConnectTask extends AsyncTask<String, Integer, ArrayList<BusInfoNet>> {

	public static final String BUS_URL = "http://businfo.daegu.go.kr/ba/arrbus/arrbus.do?act=arrbus&winc_id=";

	private Context context;
	private String stationNumber;
	private String errorMessage = null;
	public ResponseTask proxy = null;

	public ConnectTask(Context context, String stationNumber) {
		this.context = context;
		this.stationNumber = stationNumber;
	}

	@Override
	protected ArrayList<BusInfoNet> doInBackground(String... s) {
		ArrayList<BusInfoNet> busInfoList = new ArrayList<BusInfoNet>();

		InputStream is = null;
		try {
			if (!stationNumber.equals("0")) {
				is = new BackHttpConnection(context, BUS_URL + stationNumber).getInputStream();
				XmlPullParser parser = new BackXmlParser(is, "euc-kr").getParser();
				BusStationParsing parsingWork = new BusStationParsing(parser, busInfoList);
			} else {
				errorMessage = "0번 정류장은 전광판정보가 제공되지 않습니다";
			}
		} catch (Exception e) {
			errorMessage = e.getMessage();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return busInfoList;
	}

	@Override
	protected void onPostExecute(ArrayList<BusInfoNet> result) {
		super.onPostExecute(result);
		proxy.onTaskFinish(result, errorMessage);
	}

}
