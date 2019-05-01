package webcheck;

import static org.rrd4j.ConsolFun.AVERAGE;

import java.io.IOException;

import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.Util;

public class RrdDbDump {

	public static void main(String[] args) throws IOException {
		if (args.length >= 1) {
			new RrdDbDump().run(args[0]);
		}
	}

	public void run(String rrdPath) throws IOException {
		RrdDb rrdDb = new RrdDb(rrdPath, true);
		System.out.println("Last update time: " + rrdDb.getLastUpdateTime());
		System.out.println("Last info: " + rrdDb.getInfo());

		FetchRequest request = rrdDb.createFetchRequest(AVERAGE, Util.getTimestamp(2011, 5, 10), Util.getTimestamp(2011, 5, 11));
		System.out.println(request.dump());

		FetchData fetchData = request.fetchData();
		System.out.println("Row count: " + fetchData.getRowCount());
		System.out.println(fetchData.toString());
	}

}
