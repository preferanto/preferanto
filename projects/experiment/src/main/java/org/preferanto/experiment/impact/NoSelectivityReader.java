package org.preferanto.experiment.impact;

import static org.preferanto.experiment.util.Constants.CONFIG_COUNT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoSelectivityReader {
	private final String noSelectivityDir;

	public NoSelectivityReader(String noSelectivityDir) {
		this.noSelectivityDir = noSelectivityDir;
	}

	public NoSelectivityMap getNoSelectivityMap() throws IOException {
		NoSelectivityMap map = new NoSelectivityMap();

		for(int config=0; config < CONFIG_COUNT; config++) {
			String dir = noSelectivityDir + "/config." + config;
			int[] nfes = getNfes(dir);
			for(int nfe : nfes) {
				String nfeFile = dir + "/nfe." + nfe + ".txt";
				try(BufferedReader reader = new BufferedReader(new FileReader(nfeFile))) {
					while(true) {
						String line = reader.readLine();
						if(line == null) break;
						line = line.trim();
						if(line.isEmpty()) continue;
						if(line.startsWith("#")) continue;
						String[] items = line.split("[\\t ]+");
						double[] values = new double[items.length];
						for(int i=0; i<items.length; i++) {
							values[i] = Double.parseDouble(items[i].trim());
						}
						map.addValues(nfe, config, values);
					}
				}
			}
		}		
		return map;
	}

	private static final Pattern NFE_FILE_PATTERN = Pattern.compile("nfe\\.([0-9]+)\\.txt");
	public int[] getNfes(String dir) {
		String[] nfeFiles = new File(dir).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return NFE_FILE_PATTERN.matcher(name).matches();
			}
		});
		int[] nfes = new int[nfeFiles.length];
		for(int i=0; i<nfeFiles.length; i++) {
			Matcher matcher = NFE_FILE_PATTERN.matcher(nfeFiles[i]);
			if(matcher.matches()) {
				String sNfe = matcher.group(1);
				nfes[i] = Integer.parseInt(sNfe);
			}
		}
		Arrays.sort(nfes);
		return nfes;
	}
	
}
