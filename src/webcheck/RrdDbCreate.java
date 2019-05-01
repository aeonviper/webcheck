package webcheck;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;
import static org.rrd4j.DsType.GAUGE;
import java.io.IOException;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Util;

public class RrdDbCreate {

	public static void main(String[] args) throws IOException {
		if (args.length >= 1) {
			new RrdDbCreate().run(args[0]);
		}
	}

	private void run(String rrdPath) throws IOException {
		RrdDef rrdDef = new RrdDef(rrdPath, Util.getTimestamp(), 300);
		rrdDef.setVersion(2);
		
		rrdDef.addDatasource("responsetime", GAUGE, 1800, 0, Double.NaN);
		rrdDef.addDatasource("responsecode", GAUGE, 1800, -10, Double.NaN);
		
		rrdDef.addArchive(AVERAGE, 0.5, 1, 35040); 	// 15 minute - 1 year 
		rrdDef.addArchive(AVERAGE, 0.5, 4, 8760); 	// 1 hour - 1 year
		rrdDef.addArchive(AVERAGE, 0.5, 96, 365); 	// 1 day - 1 year
		rrdDef.addArchive(AVERAGE, 0.5, 672, 260); 	// 1 week - 5 years
		rrdDef.addArchive(AVERAGE, 0.5, 2688, 65); 	// 4 weeks - 5 years
		
		rrdDef.addArchive(MIN, 0.5, 1, 35040); 	// 15 minute - 1 year 
		rrdDef.addArchive(MIN, 0.5, 4, 8760); 	// 1 hour - 1 year
		rrdDef.addArchive(MIN, 0.5, 96, 365); 	// 1 day - 1 year
		rrdDef.addArchive(MIN, 0.5, 672, 260); 	// 1 week - 5 years
		rrdDef.addArchive(MIN, 0.5, 2688, 65); 	// 4 weeks - 5 years

		rrdDef.addArchive(MAX, 0.5, 1, 35040); 	// 15 minute - 1 year 
		rrdDef.addArchive(MAX, 0.5, 4, 8760); 	// 1 hour - 1 year
		rrdDef.addArchive(MAX, 0.5, 96, 365); 	// 1 day - 1 year
		rrdDef.addArchive(MAX, 0.5, 672, 260); 	// 1 week - 5 years
		rrdDef.addArchive(MAX, 0.5, 2688, 65); 	// 4 weeks - 5 years
		
		System.out.println(rrdDef.dump());

		RrdDb rrdDb = new RrdDb(rrdDef);
		if (rrdDb.getRrdDef().equals(rrdDef)) {
			System.out.println("Checking RRD file structure... OK");
		} else {
			System.out.println("Invalid RRD file created. This is a serious bug, bailing out");
			return;
		}
		rrdDb.close();
		
		System.out.println("RRD file created");
	}

}
