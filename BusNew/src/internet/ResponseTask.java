package internet;

import java.util.ArrayList;

public interface ResponseTask {
	public void onTaskFinish(ArrayList<BusInfo> list, String error);
}
