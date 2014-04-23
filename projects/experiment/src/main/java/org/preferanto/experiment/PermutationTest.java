package org.preferanto.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.preferanto.core.PreferantoContext;
import org.preferanto.core.Utils;
import org.preferanto.experiment.poset.DurationListener;
import org.preferanto.experiment.poset.ImageConfiguration;
import org.preferanto.experiment.poset.ImageListener;
import org.preferanto.experiment.util.ContextListBuilder;
import org.preferanto.experiment.util.ObjectiveValuesGenerator;
import org.preferanto.experiment.util.RandomUtil;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;
import org.preferanto.poset.PosetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PermutationTest {
	private static final Logger log = LoggerFactory.getLogger(PermutationTest.class);
	
	private static final String PREF_DIR = "src/test/resources/";

	public static void main(String[] args) throws Exception {
		RandomUtil.INSTANCE.setSeed(123454321);
		
		int populationSize = 100;
		int objectiveCount = 3;
		BufferedReader reader = new BufferedReader(new FileReader(PREF_DIR + "obj3_1.pref"));
		
		PosetProvider posetProvider = PosetUtil.getPosetProvider(reader);
		
//		posetProvider.addListener(new DurationListener());
//		ImageConfiguration imgConfig = new ImageConfiguration("jung", "algo", ".png");
//		posetProvider.addListener(new ImageListener(imgConfig));
		
		
		ObjectiveValuesGenerator generator = new ObjectiveValuesGenerator(0.0, 1.0);
		
		long seed = 0;
		for(int m=0; m<1000; m++) {
			seed++;
			RandomUtil.INSTANCE.setSeed(seed);
			log.info("\n###################\nSeed " + seed + "\n###################");

			int[][] refMatrix = null;

			double[][] values = generator.getValues(populationSize, objectiveCount);
			if(log.isDebugEnabled()) {
				log.debug("values:\n" + Utils.toString(values));
			}
			
			
			ContextListBuilder builder = new ContextListBuilder();

			for(int k=0; k<1000; k++) {
				int[] permutation = RandomUtil.INSTANCE.getRandomPermutation(populationSize);
				if(log.isDebugEnabled()) {
					log.debug("\n###################\nStep " + k + ". permutation: " + Arrays.toString(permutation) + "\n###################");
				}
				
				List<PreferantoContext> contexts = builder.createContextList(values, permutation);

				Poset poset = posetProvider.getPoset(contexts);
				int[][] matrix = getNonPermutatedMatrix(poset, permutation);
				if(refMatrix == null) {
					refMatrix = matrix;
				} else {
					if(!Arrays.deepEquals(refMatrix, matrix)) {
						log.error("seed " + seed + ". Different matrices.\nreference matrix:\n" + Utils.toString(refMatrix) + "\ncurrent matrix (k=" + k + "):\n" + Utils.toString(matrix));
						throw new Exception();
					}
				}
			}
		}
	}
	
	private static int[][] getNonPermutatedMatrix(Poset poset, int[] permutation) {
		int size = poset.getSize();
		int[][] matrix = new int[size][size];
		for(int i=0; i<size; i++) {
			int row = permutation[i];
			for(int j=0; j<size; j++) {
				int col = permutation[j];
				matrix[row][col] = poset.getRule(i, j);
			}
		}
		return matrix;
	}
}
