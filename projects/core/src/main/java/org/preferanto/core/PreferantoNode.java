package org.preferanto.core;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class PreferantoNode extends CommonTree {
	public Expression expr = null;

	public PreferantoNode(Token t) {
		super(t);
	}

	public PreferantoNode(PreferantoNode node) {
		super(node);
		this.expr = new Expression(node.expr);
	}

	public void setArgFromParent() throws PreferantoPredicateException {
		if(parent != null) {
			Expression parentExpr = ((PreferantoNode)parent).expr;
			if(parentExpr != null) {
				int parentArg = parentExpr.arg;
				if(parentArg != 0) {
					if((expr.arg != 0)/* && (expr.arg != parentArg)*/) {
						Expression.error("setArgFromParent", "Trying to change argument value from " + expr.arg + " to " + parentArg + " for node " + this);
					}
					expr.arg = parentArg;
				} 
			}
		}
	}
	
	@Override
	public Tree dupNode() {
		return new PreferantoNode(this);
	}
	
	@Override
	public String toString() {
		return super.toString() + " : " + expr;
	}
}
