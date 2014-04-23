package org.preferanto.core;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTreeAdaptor;

public class PreferantoNodeAdapter extends CommonTreeAdaptor {
	@Override
	public Object create(Token payload) {
		return new PreferantoNode(payload);
	}
}
