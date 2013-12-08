package internet;

import java.util.ArrayList;

public interface ResponseTask {
	public void onTaskFinish(ArrayList<BusInfoNet> list, String error);
}
