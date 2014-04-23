package org.preferanto.core;

import org.antlr.runtime.FailedPredicateException;

public class PreferantoPredicateException extends FailedPredicateException {
	private static final long serialVersionUID = 1L;

	public PreferantoPredicateException(String ruleName, String predicateText) {
		super(ThreadLocalContext.getInput(), ruleName, predicateText);
	}
}
