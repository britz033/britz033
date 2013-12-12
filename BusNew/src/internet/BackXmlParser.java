package internet;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

public class BackXmlParser{
	
	private static final String TAG = "BackXmlParser";
	
	private XmlPullParser parser;
	
	public BackXmlParser(InputStream is, String encoding){
		
		try{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		parser = factory.newPullParser();
		parser.setInput(is, encoding);
		} catch(Exception e){
			Log.d(TAG,"파서 받아오기 실패했습니다");
			e.printStackTrace();
		}
	}

	public XmlPullParser getParser() {
		return parser;
	}
	
	
}
