package org.preferanto.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.preferanto.experiment.poset.DurationListener;
import org.preferanto.experiment.poset.ImageConfiguration;
import org.preferanto.experiment.poset.ImageListener;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PosetTest {
	private static final Logger log = LoggerFactory.getLogger(PosetTest.class);

//	private Poset createPoset(int size, int ruleCount, long seed, double... a) {
//		Random rnd = new Random(seed); 
//		Poset poset = new Poset(size);
//		int[][] matrix = poset.getMatrix();
//		
//		double[] alpha = new double[ruleCount];		
//		if(a.length == 0) {
//			for(int r=0; r<ruleCount; r++) {
//				alpha[r] = 1.0 / (ruleCount + 2 - r);
//			}
//		} else {
//			for(int r=0; r<a.length && r<ruleCount; r++) {
//				alpha[r] = a[r];
//			}
//			for(int r=a.length; r<ruleCount; r++) {
//				alpha[r] = a[a.length - 1];
//			}
//		}
//		for(int r=0; r<ruleCount; r++) {
//			for(int i=0; i<size; i++) {
//				for(int j=0; j<size; j++) {
//					if((i != j) && (matrix[i][j] == 0) && (matrix[j][i] != r + 1)) {
//						if(rnd.nextDouble() < alpha[r]) {
//							matrix[i][j] = r + 1;
//						}
//					}
//				}
//			}
//		}
//		return poset;
//	}
	
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/test1.pref"));
		
		ImageConfiguration imgConfig = new ImageConfiguration("jung", "algo", ".png");
		Poset poset = PosetUtil.getPoset(reader, new DurationListener(), new ImageListener(imgConfig));
		log.info("poset:\n" + poset);
	}
}
