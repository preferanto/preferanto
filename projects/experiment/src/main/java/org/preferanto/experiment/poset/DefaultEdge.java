package org.preferanto.experiment.poset;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultEdge implements UIEdge {
	private final int rule;
	private Style style = Style.NORMAL;
	
	private static final AtomicInteger ID = new AtomicInteger();
	
	private final int id;
	
	enum Style {NORMAL, REMOVED, PRESERVED}
	
	public DefaultEdge(int rule){
		this.rule = rule;
		this.id = ID.incrementAndGet();
	}

	@Override
	public String toString() {
		return "#" + id + ": " + "rule " + rule;
	}
	
	public Style getStyle() {
		return style;
	}
	
	public void setStyle(Style style) {
		this.style = style;
	}
	

	@Override
	public Stroke getStroke() {
		switch(style) {
			case PRESERVED: return new BasicStroke(2.0f);
			case REMOVED: return new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f, 10.0f}, 0.0f);
			default: return new BasicStroke();	
		}
	}

	@Override
	public Paint getDraw() {
		switch(style) {
			case PRESERVED: return Color.BLUE;
			case REMOVED: return Color.RED;
			default: return Color.BLACK;	
		}
	}
}
