package internet;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

/*
 * <div class="arrlist">
 <p class="bsnm png">정류소명 : <span class="strong">영대병원역</span></p>
 <p class="viewmap"><a href="../route/route.do?act=main&amp;layout=mainArr&amp;arrBsInput1=영대병원역" onclick="window.open(this.href, 'map_window', 'left=0,top=0,width=1028,height=690,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,resizable=yes'); return false;">정류소위치</a><p>

 /*
 * <li class="st"><span class="route_no"><span
 * class="marquee">순환3-1</span></span><span class="arr_state">전</span><span
 * class="cur_pos"><span class="marquee">출발</span></span></li>
 * 
 *  <li class="nm"><span class="route_no"><span class="marquee">성서2</span></span><span class="arr_state">13분</span>
 *  <span class="cur_pos"><span class="marquee">월성푸르지오 <span class="route_note">(대천-다사-대평-다사-대천)</span></span></span></li>
 * 
 * <li class="nm"><span class="route_no"><span
 * class="marquee">604</span></span><span class="arr_state">8분</span><span
 * class="cur_pos"><span class="marquee">대명초교</span></span></li>


 <script>
 applyMarquee('marquee');
 </script>

 */

public class BusStationParsing {
	
	private static final String TAG = "BusStationParsing";

	public BusStationParsing(XmlPullParser parser, ArrayList<BusInfoNet> busInfoArray) throws Exception {
		parser.next(); // 문서의 시작은 넘어감
		int status = parser.getEventType(); // 현재 parser가 가르키는 곳의 상황을
											// 가져옴.문서의
											// 시작이라던지, 태그의 시작점이라던지..
		String tag = null; // 태그 이름
		BusInfoNet bus = null;
		boolean end_flag = false;
		// busInfoArray = new ArrayList<BusInfo>();

		Log.d(TAG, "시작합니다");
		// 문서끝에 닿을때까지 돌림

		String busStation = null;
		while (status != XmlPullParser.END_DOCUMENT && !end_flag) {
			switch (status) {
			case XmlPullParser.START_TAG:
				tag = parser.getName();
				// li 태그에 있는 모든 버스 정보를 차례로 빼냄
				if (tag.equals("li")) {

					String attr = parser.getAttributeValue(0);
					bus = new BusInfoNet();
					bus.setStation(busStation);

					if (attr.equals("st")) {
						bus.setSoon(true);
					} else if (attr.equals("nm")) {
						bus.setSoon(false);
					} else if (attr.equals("noop")) {
						busInfoArray = null;
						return;
					}

					nextCount(parser, 3);
					bus.setBusNum(parser.getText());

					nextCount(parser, 4);
					bus.setTime(parser.getText());

					nextCount(parser, 4);
					bus.setCurrent(parser.getText());
					
					status = parser.next();
					if(status == XmlPullParser.START_TAG){
						tag = parser.getName();
						if(tag.equals("span"))
							parser.next();
							bus.setRoute(parser.getText());
					}
						
					busInfoArray.add(bus);
				} else if (tag.equals("p")) {
					int count = parser.getAttributeCount();
					if (count > 0) {
						if (parser.getAttributeValue(0).equals("bsnm png")) {
							parser.next(); // <p class="bsnm png">정류소명 :
											// <span
											// class="strong">영대병원역</span></p>
							parser.next();
							parser.next(); // 영대병원역
							busStation = parser.getText();
						} else if (parser.getAttributeValue(0).equals("png")) {
							throw new Exception("입력하신 정류소정보가 없습니다");
						}
					}
				}
				break;
			case XmlPullParser.END_TAG: // end_tag가 /ul 이면 while문 탈출
				tag = parser.getName();
				if (tag.equals("ul"))
					end_flag = true;
				break;
			default:
				break;
			}

			status = parser.next();
		}
		Log.d(TAG, "종료했습니다");
	}

	private void nextCount(XmlPullParser html, int count) throws Exception {
		for (int i = 0; i < count; i++) {
			html.next();
		}
	}

}
