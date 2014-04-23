package org.preferanto.poset;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PosetProviderTest {
	private static final Logger log = LoggerFactory.getLogger(PosetProviderTest.class);
	
	private static final int[][] ruleMatrix1 = {
		{ 0,  2,  2, -1,  2},
		{-2,  0,  2, -1,  2},
		{-2, -2,  0, -1, -3},
		{ 1,  1,  1,  0,  1},
		{-2, -2,  3, -1,  0}
	};

	 @Test
	public void test1() throws Throwable {
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/test1.pref"));

		Poset poset = PosetUtil.getPoset(reader);
		log.info("poset:\n" + poset);

		assertTrue(Arrays.deepEquals(ruleMatrix1, poset.getRuleMatrix()));
	}
}
