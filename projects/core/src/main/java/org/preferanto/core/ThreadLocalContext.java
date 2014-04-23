package org.preferanto.core;

import org.antlr.runtime.tree.TreeNodeStream;

public abstract class ThreadLocalContext {
	
	private ThreadLocalContext() {
		throw new AssertionError();
	}
	
	private static ThreadLocal<ContextData> contextData = new ThreadLocal<ContextData>() {
		@Override
		protected ContextData initialValue() {
			return new ContextData();
		}
	};

	private static final class ContextData {
		TreeNodeStream input;
	}
	
	public static void clear() {
		contextData.remove();
	}
	
	public static TreeNodeStream getInput() {
		return contextData.get().input;
	}
	
	public static void setInput(TreeNodeStream input) {
		contextData.get().input = input;
	}
}
