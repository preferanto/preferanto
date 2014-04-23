package org.preferanto.experiment.impact;

import static org.preferanto.experiment.util.Constants.CONFIG_COUNT;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.preferanto.core.PreferantoContext;
import org.preferanto.experiment.util.ContextListBuilder;

public class NoSelectivityMap {
	
	private final SortedMap<Integer, List<List<PreferantoContext>>> map = new TreeMap<>();
	private final ContextListBuilder ctxListBuilder = new ContextListBuilder();
	
	public List<List<PreferantoContext>> getParentList(int nfe) {
		List<List<PreferantoContext>> list = map.get(nfe);
		if(list == null) {
			list = new ArrayList<>();
			for(int i=0; i<CONFIG_COUNT; i++) {
				list.add(new ArrayList<PreferantoContext>());
			}
			map.put(nfe, list);
		}
		return list;
	}
	
	public List<PreferantoContext> getList(int nfe, int config) {
		return getParentList(nfe).get(config);
	}

	public void addValues(int nfe, int config, double[] values) {
		PreferantoContext ctx = ctxListBuilder.createContext(values);
		getList(nfe, config).add(ctx);
	}
}
