package org.preferanto.poset;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.preferanto.core.EvaluatorCreatorBytecode;
import org.preferanto.core.PreferantoCompiler;
import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoContextImpl;
import org.preferanto.core.PreferantoEvaluator;
import org.preferanto.core.PreferantoException;
import org.preferanto.core.QuantitySymbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PosetUtil {
	private static final Logger log = LoggerFactory.getLogger(PosetUtil.class);

	private static final String CONTEXT_SECTION_DELIMITER = "--CONTEXTS--";

	static public Poset getPoset(String filePath) {
		try {
			return getPoset(new BufferedReader(new FileReader(filePath)));
		} catch(FileNotFoundException e) {
			throw new PreferantoException(e);
		}
	}

	static public Poset getPoset(BufferedReader reader, PosetProvider.Listener... listeners) {
		try {
			StringBuilder sbSpec = new StringBuilder();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				if(line.trim().startsWith(CONTEXT_SECTION_DELIMITER)) break;
				sbSpec.append(line).append('\n');
			}
			String specText = sbSpec.toString();
			if(specText.trim().isEmpty()) throw new PreferantoException("Empty preferanto specification");
			
			PreferantoCompiler compiler = new PreferantoCompiler(specText);
			
			
			if(log.isDebugEnabled()) {
				String sourceCode = compiler.getJavaPreferantoEvalSourceCode();
				log.debug("Generated Java code:\n" + sourceCode);
			}
			
			////////// FOR TEST ONLY !!! ///////
			////////// TODO REMOVE IT !!! //////

			byte[] objectivesByteCode = compiler.getObjectivesByteCode();
			FileOutputStream fos = new FileOutputStream("src/main/java/org/preferanto/dynamic/PreferantoObjectives.class");
			fos.write(objectivesByteCode);
			fos.close();

			byte[] evalByteCode = compiler.getEvalByteCode();
			fos = new FileOutputStream("src/main/java/org/preferanto/dynamic/PreferantoEvaluatorImpl.class");
			fos.write(evalByteCode);
			fos.close();
			
			/////////////////////////////////////
			
			
			
			PreferantoEvaluator prefEvaluator = EvaluatorCreatorBytecode.createFrom(compiler);
			PosetProvider posetProvider = new PosetProvider(prefEvaluator);
			for(PosetProvider.Listener listener : listeners) {
				posetProvider.addListener(listener);
			}

			List<QuantitySymbol> quantities = compiler.getSpecification().quantities;
			ArrayList<PreferantoContext> contexts = new ArrayList<PreferantoContext>();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				if(line.trim().isEmpty()) continue;
				String[] values = line.split("[, \\t]+");
				if(values.length != quantities.size()) {
					throw new PreferantoException("Expected " + quantities.size() + " values in line '" + line + "'");
				}
				PreferantoContext context = new PreferantoContextImpl();
				try {
					for(int i=0; i<quantities.size(); i++) {					
						context.setFromString(quantities.get(i), values[i]);
					}
				} catch(Exception e) {
					throw new PreferantoException("Invalid data in line '" + line, e);
				}
				contexts.add(context);
			}
			if(contexts.isEmpty()) throw new PreferantoException("No contexts defined");
			Poset poset = posetProvider.getPoset(contexts);
			return poset;
		} catch(Throwable t) {
			if(t instanceof PreferantoException) {
				throw (PreferantoException)t;
			} else throw new PreferantoException(t);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					log.error("Failed to close preference reader", e);
				}
			}
		}
	}

	static public PosetProvider getPosetProvider(BufferedReader reader) {
		try {
			StringBuilder sbSpec = new StringBuilder();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				if(line.trim().startsWith(CONTEXT_SECTION_DELIMITER)) break;
				sbSpec.append(line).append('\n');
			}
			String specText = sbSpec.toString();
			if(specText.trim().isEmpty()) throw new PreferantoException("Empty preferanto specification");			
			PosetProvider posetProvider = new PosetProvider(specText);
			return posetProvider;
		} catch(Throwable t) {
			if(t instanceof PreferantoException) {
				throw (PreferantoException)t;
			} else throw new PreferantoException(t);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					log.error("Failed to close preference reader", e);
				}
			}
		}
	}
}
