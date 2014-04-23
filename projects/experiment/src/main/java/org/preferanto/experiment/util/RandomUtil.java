package org.preferanto.experiment.util;

import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum RandomUtil {
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(RandomUtil.class);
	
	private long seed = System.currentTimeMillis();
	private MersenneTwister random = new MersenneTwister(seed);
	
	public void setSeed(long seed) {
		this.seed = seed;
		this.random = new MersenneTwister(seed);
		log.trace("Created random " + random + " with seed " + seed);
	}
	
	public long getSeed() {
		return seed;
	}
	
	public MersenneTwister getRandom() { 
		log.trace("Getting random " + random);
		return random;
	}
	
	public void setRandom(MersenneTwister random) {
		this.random = random;
		log.trace("random set to " + random);
	}
	
	public boolean nextBoolean() {
		return random.nextBoolean();
	}

	public void nextBytes(byte[] bytes) {
		random.nextBytes(bytes);
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public double nextDouble(double minVal, double maxVal) {
		double val = minVal + (maxVal - minVal) * random.nextDouble();
		return val;
	}

	public float nextFloat() {
		return random.nextFloat();
	}

	public float nextFloat(float minVal, float maxVal) {
		float val = minVal + (maxVal - minVal) * random.nextFloat();
		return val;
	}

	public double nextGaussian() {
		return random.nextGaussian();
	}

	public int nextInt() {
		return random.nextInt();
	}

	public int nextInt(int n) {
		int val = random.nextInt(n);
		return val;
	}

	public int nextInt(int minVal, int maxVal) {
		int val = minVal + random.nextInt(maxVal - minVal);
		return val;
	}

	public long nextLong() {
		return random.nextLong();
	}
	
	public long nextLong(long n) {
		return random.nextLong(n);
	}
	
	public long nextLong(long minVal, long maxVal) {
		long val = minVal + random.nextLong(maxVal - minVal);
		return val;
	}

	public int[] getRandomPermutation(int n) {
		int[] perm = new int[n];
		for (int i = 0; i < n; i++) {
			perm[i] = i;
		}
		for (int i = 0; i < n; i++) {
			int j = i + nextInt(n - i);
			int tmp = perm[i];
			perm[i] = perm[j];
			perm[j] = tmp;
		}
		return perm;
	}
}
