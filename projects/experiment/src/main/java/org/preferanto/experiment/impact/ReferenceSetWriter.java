package org.preferanto.experiment.impact;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.moeaframework.core.Solution;
import org.preferanto.experiment.selectivity.PrefGen;
import org.preferanto.experiment.selectivity.SelectivityEntry;

public class ReferenceSetWriter {
	private final String outputDir;
	private final PrefGen prefGen;
	
	public ReferenceSetWriter(String outputDir, SelectivityEntry selectivityEntry) throws IOException {
		this.outputDir = outputDir;
		this.prefGen = selectivityEntry.getPrefGen();
		
		File dir = new File(outputDir);
		dir.mkdirs();
		if(!dir.isDirectory()) {
			throw new IOException("Cannot create directory " + outputDir);
		}
		
		String prefFile = outputDir + "/pref.txt";
		try(PrintWriter writer = new PrintWriter(prefFile)) {
			writer.println(selectivityEntry);
			writer.println(prefGen);
			boolean hasError = writer.checkError();
			if(hasError) {
				throw new IOException("Failed to write to " + prefFile);
			}
		}
	}
	
	public PrefGen getPrefGen() {
		return prefGen;
	}
	
	public String getOutputDir() {
		return outputDir;
	}
	
	public void writeObjectives(int nfe, Iterable<Solution> result, String comment) throws IOException {
		String nfeFile = outputDir + "/nfe." + nfe + ".txt";
		try(PrintWriter writer = new PrintWriter(nfeFile)) {
			if((comment != null) && !comment.trim().isEmpty()) {
				writer.println("# " + comment);
			}
			for(Solution solution : result) {
				for(double val : solution.getObjectives()) {
					writer.print(val + " ");
				}
				writer.println();
				boolean hasError = writer.checkError();
				if(hasError) {
					throw new IOException("Failed to write to " + nfeFile);
				}
			}
		}
	}
	
}
