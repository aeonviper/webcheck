package webcheck;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;

import java.awt.Color;
import java.io.IOException;

import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

public class RrdDbGraph {

	static final int IMG_WIDTH = 500;
	static final int IMG_HEIGHT = 300;

	public static void main(String[] args) throws IOException {
		if (args.length >= 2) {
			new RrdDbGraph().run(args[0], args[1]);
		}
	}

	public void run(String rrdPath, String imgPath) throws IOException {
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setWidth(IMG_WIDTH);
		gDef.setHeight(IMG_HEIGHT);
		gDef.setFilename(imgPath);
		gDef.setStartTime(Util.getTimestamp(2011, 5, 10));
		gDef.setEndTime(Util.getTimestamp(2011, 5, 11));
		gDef.setTitle("wasmonitor");
		gDef.setVerticalLabel("Response Time");

		gDef.datasource("responsetime", rrdPath, "responsetime", AVERAGE);
		gDef.line("responsetime", Color.GREEN, "Response Time");

		gDef.gprint("responsetime", MIN, "min = %.3f%s");
		gDef.gprint("responsetime", MAX, "max = %.3f%s");
		gDef.gprint("responsetime", AVERAGE, "avg = %.3f%S\\c");

		gDef.setImageInfo("<img src='%s' width='%d' height = '%d'>");
		gDef.setPoolUsed(false);
		gDef.setImageFormat("png");

		RrdGraph graph = new RrdGraph(gDef);
		System.out.println(graph.getRrdGraphInfo().dump());
	}

}
