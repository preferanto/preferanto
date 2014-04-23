package org.preferanto.experiment.impact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.preferanto.experiment.selectivity.SelectivityEntry;
import org.preferanto.experiment.util.Constants;
import org.preferanto.poset.PosetProvider;

public class ReferenceSetReader {
	private final String inputDir;
	private final SelectivityEntry selectivityEntry;
	private final PosetProvider posetProvider;
	
	public ReferenceSetReader(String inputDir) throws Exception {
		this.inputDir = inputDir;
		this.selectivityEntry = createSelectivityEntry();
		this.posetProvider = createPosetProvider();		
	}
	
	private PosetProvider createPosetProvider() throws Exception {
		String prefFile = inputDir + "/pref.txt";
		StringBuilder sb = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new FileReader(prefFile))) {
			String line = reader.readLine();			
			if(line != null) {
				while((line = reader.readLine()) != null) {
					sb.append(line).append('\n');
				}
			}			
		}
		String preferantoText = sb.toString();
		if(preferantoText.isEmpty()) {
			throw new Exception("No preferanto specification found in " + prefFile + ".");
		}
		return new PosetProvider(preferantoText);
	}

	private SelectivityEntry createSelectivityEntry() throws Exception {
		String prefFile = inputDir + "/pref.txt";
		try(BufferedReader reader = new BufferedReader(new FileReader(prefFile))) {
			String line = reader.readLine();
			return new SelectivityEntry(line);
		}
	}

	
	public String getInputDir() {
		return inputDir;
	}
	
	public SelectivityEntry getSelectivityEntry() {
		return selectivityEntry;
	}

	public PosetProvider getPosetProvider() {
		return posetProvider;
	}

	public List<double[]> readObjectiveValues(int nfe) throws IOException {
		List<double[]> objectiveValues = new ArrayList<>();
		String nfeFile = inputDir + "/nfe." + nfe + ".txt";
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
				objectiveValues.add(values);
			}
		}
		return objectiveValues;
	}

	private static final Pattern NFE_FILE_PATTERN = Pattern.compile("nfe\\.([0-9]+)\\.txt");
	public int[] getNfes() {
		String[] nfeFiles = new File(inputDir).list(new FilenameFilter() {
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
	
	public static void main(String[] args) throws Exception {
//		ReferenceSetReader refReader = new ReferenceSetReader("reference/objCount.7/selectivity.0.4/config.3");
//		System.out.println(Arrays.toString(refReader.readObjectiveValues(2500).get(0)));
//		System.out.println(Arrays.toString(refReader.getNfes()));

		if(args.length < 2) {
			throw new IllegalArgumentException("Expected at least 2 arguments: provider minObjCount [maxObjCount [step]]");
		}
		String selProviderName = args[0];
		int minObjCount = Integer.parseInt(args[1]);
		if(minObjCount < 2) throw new IllegalArgumentException("Invalid value for minObjCount: " + minObjCount + ". Minimum value allowed: 2");
		int maxObjCount = (args.length > 2) ? Integer.parseInt(args[2]) : minObjCount;
		if(maxObjCount < minObjCount) throw new IllegalArgumentException("maxObjCount (" + maxObjCount + ") < minObjCount (" + minObjCount + ")");
		int objCountStep = (args.length > 3) ? Integer.parseInt(args[3]) : 1;
		
		
		String baseDir = "reference/" + selProviderName;

		for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {
			System.out.println("objCount: " + objCount);
			for(int selIndex = 0; selIndex < 10; selIndex++) {
				double selectivity = 0.1 * selIndex;
				String selectivitySuffix = String.format(Locale.US, "%.1f", selectivity);
				System.out.print("\tselectivity " + selectivitySuffix + ": ");
				for(int config=0; config < Constants.CONFIG_COUNT; config++) {
					String inputDir = baseDir + "/objCount." + objCount + "/selectivity." + selectivitySuffix + "/config." + config;
					ReferenceSetReader refReader = new ReferenceSetReader(inputDir);
					List<double[]> values = refReader.readObjectiveValues(5000);
					System.out.printf(Locale.US, "%3d ", values.size());
				}
				System.out.println();
			}
		}		
	}
}
