package org.preferanto.core;

import java.util.Locale;

import org.antlr.runtime.RecognitionException;

public class Utils {
	public static double boolDiff(boolean val1, boolean val2) {
		return (val1 == val2) ? 0.0 : val1 ? 1 : -1;
	}

	public static double intDiff(int val1, int val2) {
		return val1 - val2;
	}

	public static double longDiff(long val1, long val2) {
		return val1 - val2;
	}

	public static double doubleDiff(double val1, double val2) {
		return val1 - val2;
	}	

	public static double StringDiff(String val1, String val2) {
		return val1.compareTo(val2);
	}	

	public static int boolCompare(boolean val1, boolean val2) {
		return (val1 == val2) ? 0 : val1 ? 1 : -1;
	}

	public static int intCompare(int val1, int val2) {
		return (val1 < val2) ? -1 : (val1 == val2) ? 0 : 1;
	}

	public static int longCompare(long val1, long val2) {
		return (val1 < val2) ? -1 : (val1 == val2) ? 0 : 1;
	}

	public static int doubleCompare(double val1, double val2) {
		return (val1 < val2) ? -1 : (val1 == val2) ? 0 : 1;
	}	

	public static int StringCompare(String val1, String val2) {
		return val1.compareTo(val2);
	}	

	public static String getMessage(String errMsg, Throwable e) {
		if(e instanceof RecognitionException) {
			RecognitionException exc = (RecognitionException) e;
			String msg = errMsg + "\nError around token '" + exc.token.getText() + "' at line " + exc.line + ", char " + exc.charPositionInLine /* + "\n(" + exc.toString() + ")"*/;
			return msg;
		} else {
			return errMsg;
		}
	}
	
	public static String toString(boolean[][] connected) {
		StringBuilder sb = new StringBuilder();
		int size = connected.length;
		int digits = 1 + (int)Math.round(Math.log10(size - 1));
		String format = "%" + digits + "d";
		for(int i=0; i < digits + 3; i++) sb.append(' ');
		for(int i=0; i < size; i++) sb.append(String.format(format, i)).append(' ');
		sb.append('\n');
		for(int i=0; i < digits + 1; i++) sb.append(' ');
		for(int i=0; i < size * (digits + 1) + 2; i++) sb.append('-');
		sb.append('\n');
		for(int i=0; i < size; i++) {
			sb.append(String.format(format, i)).append(" | ");
			for(int j=0; j < size; j++) sb.append(String.format(format, (connected[i][j] ? 1 : 0))).append(' ');
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public static String toString(int[][] matrix) {
		StringBuilder sb = new StringBuilder();
		int size = matrix.length;
		int digits = 1 + (int)Math.round(Math.log10(size - 1));
		String format = "%" + digits + "d";
		for(int i=0; i < digits + 3; i++) sb.append(' ');
		for(int i=0; i < size; i++) sb.append(String.format(format, i)).append(' ');
		sb.append('\n');
		for(int i=0; i < digits + 1; i++) sb.append(' ');
		for(int i=0; i < size * (digits + 1) + 2; i++) sb.append('-');
		sb.append('\n');
		for(int i=0; i < size; i++) {
			sb.append(String.format(format, i)).append(" | ");
			for(int j=0; j < size; j++) sb.append(String.format(format, matrix[i][j])).append(' ');
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public static String toString(double[][] values) {
		StringBuilder sb = new StringBuilder();
		int rows = values.length;
		int cols = values[0].length;
		int digits = 7;
		String formatInt = "%7d";
		String formatDouble = "%7.2f";
		for(int i=0; i < digits + 3; i++) sb.append(' ');
		for(int i=0; i < cols; i++) sb.append(String.format(formatInt, i)).append(' ');
		sb.append('\n');
		for(int i=0; i < digits + 1; i++) sb.append(' ');
		for(int i=0; i < cols * (digits + 1) + 2; i++) sb.append('-');
		sb.append('\n');
		for(int i=0; i < rows; i++) {
			sb.append(String.format(formatInt, i)).append(" | ");
			for(int j=0; j < cols; j++) sb.append(String.format(Locale.US, formatDouble, values[i][j])).append(' ');
			sb.append('\n');
		}
		return sb.toString();
	}
	
}
