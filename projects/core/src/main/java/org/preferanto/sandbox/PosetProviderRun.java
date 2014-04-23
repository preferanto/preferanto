package org.preferanto.sandbox;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

import org.preferanto.core.Utils;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PosetProviderRun {
	private static final Logger log = LoggerFactory.getLogger(PosetProviderRun.class);
	
	private static final int[][] ruleMatrix1 = {
		{ 0,  2,  2, -1,  2},
		{-2,  0,  2, -1,  2},
		{-2, -2,  0, -1, -3},
		{ 1,  1,  1,  0,  1},
		{-2, -2,  3, -1,  0}
	};

	public static void run(String prefFileName, int[][] expectedRuleMatrix) throws Throwable {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/" + prefFileName));

			Poset poset = PosetUtil.getPoset(reader);
			log.info("poset:\n" + poset);

			if(expectedRuleMatrix != null) {
				boolean ok = Arrays.deepEquals(expectedRuleMatrix, poset.getRuleMatrix());
				if(ok) {
					log.info("OK: provided expected poset.");
				} else {
					log.error("FAILED. Expected poset:\n" + Utils.toString(expectedRuleMatrix));
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
	
	public static void main(String[] args) throws Throwable {
//		run("test1.pref", ruleMatrix1);
//		run("test2.pref", null);
		run("test3.pref", null);
	}
}
