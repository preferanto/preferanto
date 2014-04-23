package org.preferanto.experiment.impact;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.preferanto.experiment.selectivity.SelectivityEntry;

public class BestSolutionsReader {
	private final String selectivityDir;

	public BestSolutionsReader(String selectivityDir) {
		this.selectivityDir = selectivityDir;
	}

	public BestSolutionsMap getSolutionsMap(int config) throws IOException {
		String fileName = selectivityDir + "/best.config." + config + ".txt";
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			String line = reader.readLine();
			if(!line.startsWith("#")) {
				throw new IOException("First line must start with '#'");
			}
			SelectivityEntry selectivityEntry = new SelectivityEntry(line.substring(1));
			BestSolutionsMap solutionsMap = new BestSolutionsMap(selectivityEntry);
			
			while((line = reader.readLine()) != null) {
				String[] items = line.split(",");
				if(items.length < 4) {
					throw new IOException("Too few items in line: " + line);
				}
				int nfe = Integer.parseInt(items[0].trim());
				double distance = Double.parseDouble(items[1].trim());
				double[] values = new double[items.length - 2];
				for(int i=0; i<values.length; i++) {
					values[i] = Double.parseDouble(items[i+2].trim());
				}
				solutionsMap.putSolutionData(nfe, distance, values);
			}
			return solutionsMap;
		}
	}
}
