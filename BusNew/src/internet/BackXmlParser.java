package internet;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class BackXmlParser{
	
	
	private XmlPullParser parser;
	
	public BackXmlParser(InputStream is, String encoding){
		
		try{
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		parser = factory.newPullParser();
		parser.setInput(is, encoding);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public XmlPullParser getParser() {
		return parser;
	}
	
	
}
