package org.preferanto.experiment.selectivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PreferantoSelectivitySolverLauncher {
	public static void main(String[] args) throws Exception {
		if(args.length < 2) {
			throw new IllegalArgumentException("Expected at least 2 arguments: minObjCount maxObjCount [step]");
		}
		int minObjCount = Integer.parseInt(args[0]);
//		if(minObjCount < 2) throw new IllegalArgumentException("Invalid value for minObjCount: " + minObjCount + ". Minimum value allowed: 2");
		int maxObjCount = Integer.parseInt(args[1]);
		if(maxObjCount < minObjCount) throw new IllegalArgumentException("maxObjCount (" + maxObjCount + ") < minObjCount (" + minObjCount + ")");
		int objCountStep = (args.length > 2) ? Integer.parseInt(args[2]) : 1;
		
		System.out.println("minObjCount: " + minObjCount + ", maxObjCount: " + maxObjCount + ", objCountStep: " + objCountStep);
		
        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("java.home") + "/bin/java.exe");

        cmd.add("-server");
        
        cmd.add("-Dcom.sun.management.jmxremote");
        cmd.add("-Dcom.sun.management.jmxremote.port=1088");
        cmd.add("-Dcom.sun.management.jmxremote.authenticate=false");
        cmd.add("-Dcom.sun.management.jmxremote.ssl=false");
        cmd.add("-Xms256m");
        cmd.add("-Xmx256m");
        cmd.add("-XX:MaxPermSize=1024m");

        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        
        cmd.add(PreferantoSelectivitySolver.class.getName());

        for(int objCount = minObjCount; objCount <= maxObjCount; objCount += objCountStep) {
        	List<String> currCmd = new ArrayList<>(cmd);
        	currCmd.add("" + objCount);
        	System.out.println("Starting solver with objCount " + objCount + " using:\n" + currCmd);
        	ProcessBuilder pb = new ProcessBuilder(currCmd);
        	pb.redirectErrorStream(true);
        	
        	
        	Process process = pb.start();        	
        	int exitCode = process.waitFor();
        	BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        	while(true) {
        		String line = reader.readLine();
        		if(line == null) break;
        		System.out.println(line);
        	}
        	System.out.println("Solver with objCount " + objCount + " terminated with exit code " + exitCode);
        }
	}	
}
