package org.preferanto.experiment.selectivity;

import static org.junit.Assert.*;

import org.junit.Test;

public class SelectivityEntryTest {
	@Test
	public void test1() {
		SelectivityEntry entry = new SelectivityEntry("10, 0.100000, 0.007746, 1, 3, 0.0238249457, 0.1083851072, 0.7982074641");
		PrefGen prefGen = entry.getPrefGen();
		assertEquals(10, prefGen.getObjCount());
		double delta = 0.0000001;
		assertEquals(0.1, entry.getSelectivity(), delta);
		assertEquals(0.007746, entry.getStdev(), delta);
		assertEquals(1, prefGen.getRuleCount());
		assertEquals(3, prefGen.getTupleSize());
		double[] alphas = prefGen.getAlphas();
		assertEquals(4, alphas.length);
		assertEquals(1.0, alphas[0], delta);
		assertEquals(0.0238249457, alphas[1], delta);
		assertEquals(0.1083851072, alphas[2], delta);
		assertEquals(0.7982074641, alphas[3], delta);		
	}
}
