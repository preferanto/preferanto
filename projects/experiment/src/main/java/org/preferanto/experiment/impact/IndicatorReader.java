package org.preferanto.experiment.impact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class IndicatorReader {
	private final String indicatorDir;

	public IndicatorReader(String indicatorDir) {
		this.indicatorDir = indicatorDir;
	}

	public IndicatorMap getIndicatorMap(int objCount) throws IOException {
		IndicatorMap map = new IndicatorMap();

		String indicatorFile = indicatorDir + "/indicator.all.objCount." + objCount + ".txt";
		try(BufferedReader reader = new BufferedReader(new FileReader(indicatorFile))) {
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;
				String[] items = line.split(",");
				if(items.length < 4) {
					throw new IOException("Too few items in line: " + line);
				}
				int nfe = Integer.parseInt(items[0].trim());				
				for(int i=0; i<items.length-3; i++) {
					double val = Double.parseDouble(items[i+3].trim());
					map.setValue(nfe, (i+1), val);
				}
			}
		}
		return map;
	}
	
	public static void main(String[] args) throws Exception {
		String indicatorDir = "results/indicator/domination";
		String datDir = "gnuplot";
		new File(datDir).mkdirs();
		
		IndicatorReader reader = new IndicatorReader(indicatorDir);
		
		for(int objCount=2; objCount <= 10; objCount++) {			
			String datFile = datDir + "/indicator.objCount." + objCount + ".txt";
			try(PrintWriter writer = new PrintWriter(datFile)) {
				writer.printf("# selectivity");				
				for(int nfe=1000; nfe<=10000; nfe += 1000) {
					writer.printf(" NFE" + nfe);
				}
				writer.println();
				for(int selIndex=1; selIndex<10; selIndex++) {
					double selectivity = 0.1 * selIndex;
					writer.printf(Locale.US, "%.1f", selectivity);
					for(int nfe=1000; nfe<=10000; nfe += 1000) {
						double[] values = reader.getIndicatorMap(objCount).getMap().get(nfe);
						writer.printf(Locale.US, " %12.8f", values[selIndex]);
					}
					writer.println();
				}
				boolean hasError = writer.checkError();
				if(hasError) {
					throw new IOException("Failed to write to " + datFile);
				}
			}
		}
	}
}
