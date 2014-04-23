package org.preferanto.sandbox;

import org.preferanto.core.PreferantoContext;

public class PreferantoObjectives_EXAMPLE {
    public final double responseTime;
    public final double cost;
    public final long colors;
    public final boolean secure;
    public PreferantoObjectives_EXAMPLE(PreferantoContext ctx) {
        responseTime = ctx.getDouble("responseTime");
        cost = ctx.getDouble("cost");
        colors = ctx.getLong("colors");        	
        secure = ctx.getBoolean("secure");        	
    }
}