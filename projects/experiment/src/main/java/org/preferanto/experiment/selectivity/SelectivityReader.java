package org.preferanto.experiment.selectivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public enum SelectivityReader {
	INSTANCE;
	
	/**
	 * @param reader
	 * @param objCount
	 * @return - a map of {@link SelectivityEntry} values having the required objCount, indexed by selectivity 
	 * @throws IOException
	 */
	public SortedMap<Double, List<SelectivityEntry>> getMap(Reader reader, int objCount) throws IOException {
		SortedMap<Double, List<SelectivityEntry>> map = new TreeMap<>();
		try(BufferedReader r = new BufferedReader(reader)) {
			while(true) {
				String line = r.readLine();
				if(line == null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;
				SelectivityEntry entry = new SelectivityEntry(line);
				if(entry.getPrefGen().getObjCount() == objCount) {
					double selectivity = entry.getSelectivity();
					List<SelectivityEntry> entries = map.get(selectivity);
					if(entries == null) {
						entries = new ArrayList<>();
						map.put(selectivity, entries);
					}
					entries.add(entry);
				}
			}
		}
		return map;
	}	
}
