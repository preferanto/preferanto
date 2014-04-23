package org.preferanto.experiment.selectivity;

import java.util.List;

import org.preferanto.core.PreferantoContext;
import org.preferanto.poset.PosetProvider;

public interface SelectivityProvider {
	String getShortName();
	double getSelectivity(List<PreferantoContext> contexts, PosetProvider posetProvider);
}