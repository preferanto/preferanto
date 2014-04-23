package org.preferanto.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.preferanto.core.BuiltInTypeSymbol;
import org.preferanto.core.PreferantoContext;
import org.preferanto.core.QuantitySymbol;
import org.preferanto.core.Specification;
import org.preferanto.core.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputPanel {
	private static final Logger log = LoggerFactory.getLogger(InputPanel.class);
	private final JPanel mainPanel;
	private final List<InputComponent> inputComponents = new ArrayList<InputPanel.InputComponent>();
	
	private static abstract class InputComponent {
		private final String name;
		
		abstract JComponent getComponent();
		abstract void writeToContext(PreferantoContext context);
		abstract void fillFromContext(PreferantoContext context);
		abstract String getValueFromContext(PreferantoContext context);
		abstract String getValueAsString();
		abstract void reset();

		InputComponent(String name) {
			this.name = name;
		}
	}
	
	public InputPanel(JPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public void setEnabled(boolean enabled) {
		for(InputComponent comp : inputComponents) {
			comp.getComponent().setEnabled(enabled);
		}
	}
	
	public void setSpecification(Specification specification) {
		mainPanel.removeAll();
		inputComponents.clear();

		if(specification == null || specification.quantities == null) return;
		for(QuantitySymbol quantity : specification.quantities) {
			InputComponent comp = createComponent(quantity.name, quantity.type);
			inputComponents.add(comp);
			JPanel panel = new JPanel(new BorderLayout());
			JPanel subPanel = new JPanel();
			panel.add(subPanel, BorderLayout.EAST);
			
			JLabel label = new JLabel(quantity.name + ": ");
			subPanel.add(label);
			subPanel.add(comp.getComponent());
			mainPanel.add(panel);
		}
	}
	
	private static InputComponent createComponent(final String name, final Type type) {
		InputComponent iComp;
		if(type == BuiltInTypeSymbol._BOOLEAN) {
			final JCheckBox comp = new JCheckBox();
			iComp = new InputComponent(name) {
				@Override JComponent getComponent() {return comp;}
				@Override void writeToContext(PreferantoContext context) {context.setBoolean(name, comp.isSelected());}
				@Override void fillFromContext(PreferantoContext context) {comp.setSelected(context.getBoolean(name));}
				@Override void reset() {comp.setSelected(false);}
				@Override String getValueFromContext(PreferantoContext context) {return "" + context.getBoolean(name);}
				@Override String getValueAsString() {return "" + comp.isSelected();}
			};
		} else if(type == BuiltInTypeSymbol._REAL) {
			final JTextField comp = new JTextField(4);
			iComp = new InputComponent(name) {
				@Override JComponent getComponent() {return comp;}
				@Override void writeToContext(PreferantoContext context) {context.setDouble(name, Double.parseDouble(comp.getText()));}
				@Override void fillFromContext(PreferantoContext context) {comp.setText("" + context.getDouble(name));}
				@Override void reset() {comp.setText("");}
				@Override String getValueFromContext(PreferantoContext context) {return "" + context.getDouble(name);}
				@Override String getValueAsString() {return comp.getText();}
			};
		} else if(type == BuiltInTypeSymbol._INTEGER) {
			final JTextField comp = new JTextField(4);
			iComp = new InputComponent(name) {
				@Override JComponent getComponent() {return comp;}
				@Override void writeToContext(PreferantoContext context) {context.setLong(name, Long.parseLong(comp.getText()));}
				@Override void fillFromContext(PreferantoContext context) {comp.setText("" + context.getLong(name));}
				@Override void reset() {comp.setText("");}
				@Override String getValueFromContext(PreferantoContext context) {return "" + context.getLong(name);}
				@Override String getValueAsString() {return comp.getText();}
			};
		} else {
			final JTextField comp = new JTextField(4);
			iComp = new InputComponent(name) {
				@Override JComponent getComponent() {return comp;}
				@Override void writeToContext(PreferantoContext context) {context.setString(name, comp.getText());}
				@Override void fillFromContext(PreferantoContext context) {comp.setText(context.getString(name));}
				@Override void reset() {comp.setText("");}
				@Override String getValueFromContext(PreferantoContext context) {return "" + context.getString(name);}
				@Override String getValueAsString() {return comp.getText();}
			};
		}
		return iComp;
	}
	
	public boolean writeToContext(PreferantoContext context) {
		for(InputComponent comp : inputComponents) {
			try {
				comp.writeToContext(context);
			} catch(Exception e) {
				String errMsg = "Invalid value for parameter '" + comp.name + "'";
				log.error(errMsg, e);
				JOptionPane.showMessageDialog(mainPanel, errMsg + ": " + e + "\nResetting to old value '" + comp.getValueFromContext(context) + "'");
				comp.fillFromContext(context);
			}
		}
		return true;
	}
	
	public void fillFromContext(PreferantoContext context) {
		for(InputComponent comp : inputComponents) {
			comp.fillFromContext(context);			
		}
	}
	
	public void reset() {
		for(InputComponent comp : inputComponents) {
			comp.reset();			
		}
	}
}
