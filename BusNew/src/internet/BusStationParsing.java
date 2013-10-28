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
 * <li class="nm"><span class="route_no"><span
 * class="marquee">604</span></span><span class="arr_state">8분</span><span
 * class="cur_pos"><span class="marquee">대명초교</span></span></li>


 <script>
 applyMarquee('marquee');
 </script>

 */

public class BusStationParsing {

	public BusStationParsing(XmlPullParser parser,
			ArrayList<BusInfo> busInfoArray) {
		try {
			parser.next(); // 문서의 시작은 넘어감
			int status = parser.getEventType(); // 현재 parser가 가르키는 곳의 상황을
												// 가져옴.문서의
												// 시작이라던지, 태그의 시작점이라던지..
			String tag = null; // 태그 이름
			BusInfo bus = null;
			boolean end_flag = false;

			// 문서끝에 닿을때까지 돌림
			while (status != XmlPullParser.END_DOCUMENT && !end_flag) {
				switch (status) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					Log.d("tagname", tag);
					// li 태그에 있는 모든 버스 정보를 차례로 빼냄
					if (tag.equals("li")) {

						String attr = parser.getAttributeValue(0);
						bus = new BusInfo();

						if (attr.equals("st")) {
							bus.setSoon(true);
						} else if (attr.equals("nm")) {
							bus.setSoon(false);
						} else {
							busInfoArray = null;
							return;
						}

						nextCount(parser, 3);
						bus.setBusNum(parser.getText());
						Log.d("버스번호", parser.getText());

						nextCount(parser, 4);
						bus.setTime(parser.getText());
						Log.d("버스시간", parser.getText());

						nextCount(parser, 4);
						bus.setCurrent(parser.getText());
						Log.d("버스현재장소", parser.getText());

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
								bus.setStation(parser.getText());
							}
						}
					}
					break;
				case XmlPullParser.END_TAG: // end_tag가 /ul 이면 while문 탈출
					tag = parser.getName();
					Log.d("end_tag", tag);
					if (tag.equals("ul"))
						end_flag = true;
					break;
				default:
					Log.d("busParser() default", "text");
					break;
				}

				status = parser.next();
			}
		} catch (Exception e) {
			Log.d("BusStationParsing", "실패했습니다");
			e.printStackTrace();
		}
	}

	private void nextCount(XmlPullParser html, int count) throws Exception {
		for (int i = 0; i < count; i++) {
			html.next();
		}
	}

}
