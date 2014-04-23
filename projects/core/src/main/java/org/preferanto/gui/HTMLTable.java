package org.preferanto.gui;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import java.util.ArrayList;
import java.util.List;

import org.preferanto.core.PreferantoContext;
import org.preferanto.core.QuantitySymbol;
import org.preferanto.poset.Poset;

public class HTMLTable {
	private final List<String> columnNames = new ArrayList<String>();
	private final List<List<String>> rows = new ArrayList<List<String>>();
	
	public static HTMLTable createFromSolutions(List<QuantitySymbol> quantities, List<PreferantoContext> contexts) {
		HTMLTable table = new HTMLTable();
		table.addColumnName("Solution");
		for(QuantitySymbol quantity : quantities) {
			table.addColumnName(quantity.getName());
		}
		final int contextCount = contexts.size();

		for(int i = 0; i < contextCount; i++) {
			PreferantoContext context = contexts.get(i);

			List<String> row = table.newRow();
			row.add("Solution #" + (i + 1));
			for(QuantitySymbol quantity : quantities) {
				row.add(context.getAsString(quantity));
			}			
		}
		return table;
	}
	
	public static HTMLTable createFromPoset(List<QuantitySymbol> quantities, Poset poset) {
		HTMLTable table = new HTMLTable();
		table.addColumnName(" ");
		int size = poset.getSize();
		for(int i = 0; i < size; i++) {
			table.addColumnName("sol" + (i+1));
		}

		int[][] ruleMatrix = poset.getRuleMatrix();
		for(int i = 0; i < size; i++) {
			List<String> row = table.newRow();
			row.add("sol" + (i+1));
			for(int j = 0; j < size; j++) {
				row.add((i == j) ? "" : "" + ruleMatrix[i][j]);
			}
		}
		return table;
	}

	public void addColumnName(String columnName) {
		columnNames.add(columnName);
	}
	
	public List<String> newRow() {
		List<String> row = new ArrayList<String>();
		rows.add(row);
		return row;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table border=\"1\">\n<tr>");
		for(String name : columnNames) {
			sb.append("<th>").append(escapeHtml4(name)).append("</th>");
		}
		sb.append("</tr>\n");
		for(List<String> row : rows) {
			sb.append("<tr>");
			for(String value : row) {
				sb.append("<td>").append(escapeHtml4(value)).append("</td>");
			}
			sb.append("</tr>\n");
		}		
		sb.append("</table>\n");
		return sb.toString();
	}
}
