package org.preferanto.experiment.selectivity;

import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.List;
import java.util.SortedMap;

public class SelFormatter {
	public static void main(String[] args) throws Exception {
		int[] objCounts = {2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25};
		
		for(int objCount : objCounts) {
			String inputName = "src/main/resources/selectivity/orig/pref.objCount." + objCount + ".txt";
			String outputName = "src/main/resources/selectivity/pref.objCount." + objCount + ".txt";
			
			try(Reader reader = new FileReader(inputName); PrintWriter writer = new PrintWriter(outputName)) {
				SortedMap<Double, List<SelectivityEntry>> map = SelectivityReader.INSTANCE.getMap(reader, objCount);
				for(List<SelectivityEntry> entries : map.values()) {
					for(SelectivityEntry entry : entries) {
						writer.println(entry);
					}
				}
			}
		}
	}
}
