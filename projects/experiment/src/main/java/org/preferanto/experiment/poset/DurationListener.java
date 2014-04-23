package org.preferanto.experiment.poset;

import java.util.List;

import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;
import org.preferanto.poset.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurationListener implements PosetProvider.Listener {
	private static final Logger log = LoggerFactory.getLogger(DurationListener.class);

	private long getPosetStart;
	private long ruleStart;

	@Override
	public void getPosetStarted(Poset poset) {
		log.trace("getPoset() started.");
		getPosetStart = System.nanoTime();
	}

	@Override
	public void ruleStarted(int rule, Poset poset) {
		log.trace("rule " + rule + " started.");
		ruleStart = System.nanoTime();
	}

	@Override
	public void ruleFinished(int rule, Poset poset, List<Relation> relations, int relationsAdded) {
		long duration = System.nanoTime() - ruleStart;
		log.debug("Rule " + rule + " finished after " + duration + " ns. Detected " 
				+ relations.size() + " relations. added: " + relationsAdded + ", discarded: " + (relations.size() - relationsAdded));
		if(log.isTraceEnabled()) {
			log.trace("Poset:\n" + poset);
		}
	}

	@Override
	public void getPosetFinished(Poset poset) {
		long duration = System.nanoTime() - getPosetStart;
		log.info("getPoset() finished after " + duration + " ns.");
	}
}
