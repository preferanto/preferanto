package org.preferanto.experiment.moea;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.data.xy.XYSeries;
import org.moeaframework.core.Solution;

public class MOEAUtils {
	public static final String APPROX_SET_KEY = "Approximation Set";
	public static final String NUMBER_OF_EVAL_KEY = "NFE";

	public static String toString(Solution solution) {
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < solution.getNumberOfVariables(); i++) {
			sb.append(i).append(": ").append(solution.getVariable(i)).append('\n');
		}
		sb.append(ArrayUtils.toString(solution.getObjectives())).append('\n');
		sb.append(solution.getAttributes()).append("\n\n");

		return sb.toString();
	}
	
	public static void toGnuplot(Iterable<Solution> result, String fileName) throws IOException {
		PrintStream stream = new PrintStream(fileName);
		try{
			if(!toGnuplot(result, stream)) {
				throw new IOException("Cannot write gnuplot data to '" + fileName + "'.");
			}
		} finally {
			stream.close();
		}
		
	}

	public static boolean toGnuplot(Iterable<Solution> result, PrintStream stream) {
		for(Solution solution : result) {
			for(double val : solution.getObjectives()) {
				stream.print(val + " ");
			}
			stream.println();
		}
		return !stream.checkError();
	}

	public static XYSeries createSeries(String title, String fileName) throws IOException {
		XYSeries series = new XYSeries(title);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		try {
			String line;
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;
				String[] tokens = line.split("[ \\t]+");
				if(tokens.length < 2) {
					throw new IOException("Invalid line: " + line + " in '" + fileName + "'.");
				}
				try {
					series.add(Double.parseDouble(tokens[0].trim()), Double.parseDouble(tokens[1].trim()));
				} catch(NumberFormatException e) {
					throw new IOException("Invalid line: " + line + " in '" + fileName + "'.", e);
				}
			}
		} finally {
			reader.close();
		}
		return series;
	}
	
	public static XYSeries createSeries(String title, Iterable<Solution> result) {
		XYSeries series = new XYSeries(title);
		for(Solution solution : result) {
			double[] values = solution.getObjectives();
			if(values.length < 2) {
				throw new IllegalArgumentException("Only " + values.length + " available in solution " + toString(solution));
			}
			series.add(values[0], values[1]);
		}
		
		return series;
	}
	
	// preftest
	private static StackTraceElement[] lastStackTrace = {};
	private static int lastStackTraceCount = 0;
	public static void trace(String message) {
		try {
			throw new Exception(message);
		} catch(Exception e) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			if(!Arrays.equals(stackTrace, lastStackTrace)) {
				System.out.println("lastStackTraceCount: " + lastStackTraceCount + "\n----------------------\n");
				lastStackTrace = stackTrace.clone();
				lastStackTraceCount = 1;
				System.out.println(message);
				for(int i=0; i<stackTrace.length && i < 1000; i++) {
					System.out.println("\t" + stackTrace[i]);
				}
			} else {
				lastStackTraceCount++;
			}
		}
	}
}
