package org.preferanto.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.preferanto.core.EvaluatorCreatorBytecode;
import org.preferanto.core.PreferantoCompiler;
import org.preferanto.core.PreferantoContext;
import org.preferanto.core.PreferantoContextImpl;
import org.preferanto.core.PreferantoEvaluator;
import org.preferanto.core.QuantitySymbol;
import org.preferanto.core.Specification;
import org.preferanto.core.Utils;
import org.preferanto.html.HTMLizer;
import org.preferanto.poset.Poset;
import org.preferanto.poset.PosetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferantoPanel extends JPanel {	
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PreferantoPanel.class);
	private static final String INPUT_TITLE = "Solution #";
	private static final String CONTEXT_SECTION_DELIMITER = "--CONTEXTS--";
	private static final String DEFAULT_INPUT = "/input.default.pref";

	private final JSplitPane mainSplitPane;
	private final JSplitPane leftPane;
	
	private final JButton butImport;
	private final JButton butExport;
	private final JButton butGenerate;
	private final JButton butCompare;
	private final JButton butDeleteContext;
	private final JButton butNewContext;
	
	private final JComboBox<ContextComboItem> cmbContext;
	
	private final JTextArea prefTextArea;
	private final JTextPane javaTextPane;
	private final JScrollPane javaScrollPane;
	private final JTextPane outputTextPane;
	private final JScrollPane outputScrollPane;
	private final JTextPane logTextPane;
	private final JScrollPane logScrollPane;
	private final JTabbedPane resultPane;
	
	private final List<PreferantoContext> contexts = new ArrayList<PreferantoContext>();
	private final InputPanel inputPanel;

	private PreferantoCompiler compiler;
	private PosetProvider posetProvider;

	private String lastPrefText = "";
	
	private final LogStream logStream = new LogStream();
	private boolean initialized = false;
	
	private class LogStream extends ByteArrayOutputStream {
		private JTextPane textPane = null;
		
		public void setTextPane(JTextPane textPane) {
			this.textPane = textPane;
		}

		private void updateTextPane() {
			if(PreferantoPanel.this.initialized && (textPane != null)) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textPane.setText(LogStream.this.toString());
					}
				});
			}
		}
		
		@Override
		public synchronized void write(byte[] b, int off, int len) {
			super.write(b, off, len);
			updateTextPane();
		}

		@Override
		public synchronized void write(int b) {
			super.write(b);
			updateTextPane();
		}
	}
	
	private static class ContextComboItem {
		private final int index;
		public ContextComboItem(int index) {
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}

		@Override
		public String toString() {
			return INPUT_TITLE + (index + 1);
		}
	}
	
	public PreferantoPanel() {
		PrintStream ps = new PrintStream(logStream, true);		
		System.setErr(ps);
		System.setOut(ps);
		
		contexts.add(new PreferantoContextImpl());
		
		setLayout(new BorderLayout());
		this.mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);		
		this.add(mainSplitPane, BorderLayout.CENTER);
		
		this.leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);		
		mainSplitPane.setLeftComponent(leftPane);
		
		JPanel prefPanel = new JPanel(new BorderLayout());
		leftPane.setTopComponent(prefPanel);
		
		JPanel prefEditPanel = new JPanel(new BorderLayout());
		
		JLabel prefLabel = new JLabel("Enter your preferences:");
		prefEditPanel.add(prefLabel, BorderLayout.NORTH);

		JPanel prefPaddingPanel = new JPanel();
		prefPanel.add(prefPaddingPanel, BorderLayout.WEST);
		
		this.prefTextArea = new JTextArea();
		prefTextArea.setMargin(new Insets(10, 10, 10, 10));
		prefTextArea.setFont(new Font("Verdana", 0, 12));
		
		JScrollPane scrollPrefTextArea = new JScrollPane(prefTextArea);
		prefEditPanel.add(scrollPrefTextArea, BorderLayout.CENTER);
		
		prefPanel.add(prefEditPanel, BorderLayout.CENTER);
		
		JPanel prefButPanel = new JPanel(new BorderLayout());

		JPanel prefNorthPanel = new JPanel();
		prefNorthPanel.setLayout(new BoxLayout(prefNorthPanel, BoxLayout.Y_AXIS));
		JPanel importPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		this.butImport = new JButton("Import");
		importPanel.add(butImport);
		prefNorthPanel.add(importPanel);
		
		JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		this.butExport = new JButton("Export");
		exportPanel.add(butExport);
		prefNorthPanel.add(exportPanel);
		prefButPanel.add(prefNorthPanel, BorderLayout.NORTH);
		
		JPanel prefSouthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		this.butGenerate = new JButton("Generate");
		prefSouthPanel.add(butGenerate);
		prefButPanel.add(prefSouthPanel, BorderLayout.SOUTH);
		
		JPanel prefCenterPanel = new JPanel();
		prefButPanel.add(prefCenterPanel, BorderLayout.CENTER);
		prefPanel.add(prefButPanel, BorderLayout.EAST);
		
		JPanel inputMainPanel = new JPanel(new BorderLayout());
		
		
		JPanel inputControlPanel = new JPanel();
		this.cmbContext = new JComboBox<>();
		updateContextComboBox(0);
		inputControlPanel.add(cmbContext);
		
		this.butDeleteContext = new JButton("Delete");
		inputControlPanel.add(butDeleteContext);
		
		this.butNewContext = new JButton("New");
		inputControlPanel.add(butNewContext);
		
		inputMainPanel.add(inputControlPanel, BorderLayout.NORTH);
		
		
		JPanel inputDataPanel = new JPanel(new BorderLayout());
		
		JPanel comparePanel = new JPanel(new BorderLayout());
		JPanel butComparePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
		this.butCompare = new JButton("Compare");
		butComparePanel.add(butCompare);
		comparePanel.add(butComparePanel, BorderLayout.SOUTH);
		
		JPanel inputSubPanel = new JPanel();
		inputSubPanel.setLayout(new BoxLayout(inputSubPanel, BoxLayout.Y_AXIS));
		this.inputPanel = new InputPanel(inputSubPanel);
		JPanel inputWrapperPanel = new JPanel();
		inputWrapperPanel.add(inputSubPanel);
		JScrollPane inputScrollPane = new JScrollPane(inputWrapperPanel);

		inputDataPanel.add(comparePanel, BorderLayout.EAST);
		inputDataPanel.add(inputScrollPane, BorderLayout.CENTER);
		inputMainPanel.add(inputDataPanel, BorderLayout.CENTER);

		leftPane.setBottomComponent(inputMainPanel);
		
		
		this.resultPane = new JTabbedPane(SwingConstants.TOP);
		
		this.javaTextPane = createHtmlTextPane();
		this.javaScrollPane = new JScrollPane(javaTextPane);
		resultPane.addTab("Java", javaScrollPane);
				
		this.outputTextPane = createHtmlTextPane();
		this.outputScrollPane = new JScrollPane(outputTextPane);
		resultPane.addTab("Output", outputScrollPane);

		this.logTextPane = new JTextPane();
		logTextPane.setEditable(false);
		logTextPane.setBackground(SystemColor.control);
		this.logScrollPane = new JScrollPane(logTextPane);
		resultPane.addTab("Debug log", logScrollPane);
		logStream.setTextPane(logTextPane);
		
		mainSplitPane.setRightComponent(resultPane);
		
		configureControls();
		updateControls(true);
		validate();

		InputStream inputStream = getClass().getResourceAsStream(DEFAULT_INPUT);
		if(inputStream != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			try {
				importPreferences(reader);
			} catch(IOException e) {
				JOptionPane.showMessageDialog(this, "Cannot import default preferences: " + e);
			}
		}
	}

	private static JTextPane createHtmlTextPane() {
		JTextPane textPane = new JTextPane();
		textPane.setMargin(new Insets(10, 10, 10, 10));
		textPane.setContentType("text/html");
		
    	HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        
        styleSheet.addRule("body {color:#000000;font-weight:normal;font-size:12;font-family:Verdana,sans-serif;}");
        styleSheet.addRule(".identifier {color:#000040;}");
        styleSheet.addRule(".keyword {color:#800040;}");
        styleSheet.addRule(".number {color:#0000C0;}");
        styleSheet.addRule(".string_literal {color:#008000;}");
        
        textPane.setEditorKit(kit);
				
		textPane.setEditable(false);
		textPane.setBackground(SystemColor.control);
		return textPane;
	}
	
	private void configureControls() {
		prefTextArea.addKeyListener(new KeyListener() {			
			@Override public void keyTyped(KeyEvent e) {updateControls(false);}
			@Override public void keyReleased(KeyEvent e) {updateControls(false);}
			@Override public void keyPressed(KeyEvent e) {updateControls(false);}
		});

		butImport.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String inputPath = chooseFilePath(".", "Open");
				if(inputPath == null) return;
				importPreferences(inputPath);
			}
		});
		
		butExport.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				String outputPath = chooseFilePath(".", "Save");
				if(outputPath == null) return;
				exportPreferences(outputPath);
			}
		});
		
		butGenerate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generateSpecification();
			}
		});		

		butCompare.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSelectedContext();
				
				if(posetProvider == null) return;

				StringBuilder sb = new StringBuilder(1024);
				sb.append("<html><body>\n");
				try {
					HTMLTable table1 = HTMLTable.createFromSolutions(compiler.getSpecification().quantities, contexts);
					sb.append(table1.toString());

					sb.append("\n<p/>\n");
					
					Poset poset = posetProvider.getPoset(contexts);
					HTMLTable table2 = HTMLTable.createFromPoset(compiler.getSpecification().quantities, poset);
					sb.append(table2.toString());
				
				} catch(Exception exc) {
					String errMsg = "Failed to compare solutions";
					log.error(errMsg, exc);
					JOptionPane.showMessageDialog(PreferantoPanel.this, errMsg);
				} finally {
					sb.append("</body></html>\n");
					outputTextPane.setText(sb.toString());
					resultPane.setSelectedComponent(outputScrollPane);
					updateControls(false);
				}
			}
		});
		
		cmbContext.addItemListener(new ItemListener() {			
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(isIgnoreItemStateChanges()) return;
				if(inputPanel == null) return;
				int index = ((ContextComboItem)e.getItem()).getIndex();
				if(index < 0) return;
				PreferantoContext context = contexts.get(index);
				if(e.getStateChange() == ItemEvent.SELECTED) {
					inputPanel.fillFromContext(context);
				} else {
					inputPanel.writeToContext(context);
				}
			}
		});
		
		butDeleteContext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(contexts.size() < 2) return;
				int index = cmbContext.getSelectedIndex();
				if(index < 0) return;
				contexts.remove(index);
				if(index > contexts.size() - 1) {
					index = contexts.size() - 1;
				}
				updateContextComboBox(index);				
			}
		});
		
		butNewContext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateSelectedContext();
				PreferantoContextImpl context = new PreferantoContextImpl();
				contexts.add(context);
				if(compiler !=null) {
					context.reset(compiler.getSpecification().quantities);
				}
				updateContextComboBox(contexts.size() - 1);
			}
		});
	}

	private void generateSpecification() {
		setIgnoreItemStateChanges(true);
		PreferantoCompiler oldCompiler = compiler;
		PosetProvider oldPosetProvider = posetProvider;
		String oldJavaText = javaTextPane.getText();
		
		boolean ok = true;
		try {
			String prefText = prefTextArea.getText().trim();
			compiler = new PreferantoCompiler(prefText);
			String sourceCode = compiler.getJavaPreferantoEvalSourceCode();
			HTMLizer htmlizer = new HTMLizer(new StringReader(sourceCode));
			htmlizer.go();
			
			javaTextPane.setText(htmlizer.output);
			resultPane.setSelectedComponent(javaScrollPane);

			PreferantoEvaluator prefEvaluator = EvaluatorCreatorBytecode.createFrom(compiler);
			posetProvider = new PosetProvider(prefEvaluator);
			
			lastPrefText = prefText;
			updateInputComponents();
		} catch(Throwable exc) {
			ok = false;

			compiler = oldCompiler;
			posetProvider = oldPosetProvider;
			javaTextPane.setText(oldJavaText);
			
			String errMsg = "Failed to compile preferences";
			log.error(errMsg, exc);
			JOptionPane.showMessageDialog(PreferantoPanel.this, Utils.getMessage(errMsg, exc));
		}
		updateControls(ok);
		updateContextComboBox(-1);
		setIgnoreItemStateChanges(true);
	}
	
	private String chooseFilePath(String initDirPath, String butName) {
		JFileChooser chooser = new JFileChooser(initDirPath);
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Preference File (*.pref)";
			}			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".pref");
			}
		});
		chooser.showDialog(this, butName);
		File selectedFile = chooser.getSelectedFile();
		return (selectedFile == null) ? null : selectedFile.getAbsolutePath();	
	}

	private void importPreferences(String inputPath) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputPath));
			importPreferences(reader);
		} catch(IOException e) {
			JOptionPane.showMessageDialog(this, "Cannot read preferences from '" + inputPath + "': " + e);
		}
	}

	private void importPreferences(BufferedReader reader) throws IOException {
		try {
			StringBuilder sbSpec = new StringBuilder();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				if(line.trim().startsWith(CONTEXT_SECTION_DELIMITER)) break;
				sbSpec.append(line).append('\n');
			}
			String specText = sbSpec.toString();
			if(specText.trim().isEmpty()) return;
			prefTextArea.setText(specText);
			generateSpecification();
			if(compiler == null) return;
			List<QuantitySymbol> quantities = compiler.getSpecification().quantities;
			ArrayList<PreferantoContext> tmpContexts = new ArrayList<PreferantoContext>();
			while(true) {
				String line = reader.readLine();
				if(line == null) break;
				if(line.trim().isEmpty()) continue;
				String[] values = line.split("[, \\t]+");
				if(values.length != quantities.size()) {
					JOptionPane.showMessageDialog(this, "Expected " + quantities.size() + " values in line '" + line + "'");
					continue;
				}
				PreferantoContext context = new PreferantoContextImpl();
				try {
					for(int i=0; i<quantities.size(); i++) {					
						context.setFromString(quantities.get(i), values[i]);
					}
				} catch(Exception e) {
					JOptionPane.showMessageDialog(this, "Invalid data in line '" + line + "': " + e);
					continue;
				}
				tmpContexts.add(context);
			}
			if(!tmpContexts.isEmpty()) {
				contexts.clear();
				contexts.addAll(tmpContexts);
				updateContextComboBox(0);
			}
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e) {
					log.error("Failed to close preference file", e);
				}
			}
		}
	}

	private void exportPreferences(String outputPath) {
		FileWriter writer = null;
		try {
			writer = new FileWriter(outputPath);
			writer.write(prefTextArea.getText());
			if((compiler != null) && !contexts.isEmpty()) {
				writer.write("\n" + CONTEXT_SECTION_DELIMITER + "\n");
				List<QuantitySymbol> quantities = compiler.getSpecification().quantities;
				for(PreferantoContext context : contexts) {
					String sep = "";
					for(QuantitySymbol quantity : quantities) {
						String value = context.getAsString(quantity);
						writer.write(sep);
						sep = ", ";
						writer.write(value);
					}
					writer.write("\n");
				}
			}			
		} catch(IOException e) {
			JOptionPane.showMessageDialog(this, "Cannot write preferences to '" + outputPath + "': " + e);
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					log.error("Failed to close preference file", e);
				}
			}
		}
	}
	
	private void resetContexts() {
		contexts.clear();
		PreferantoContext context = new PreferantoContextImpl();
		contexts.add(context);
		if(compiler !=null) {
			context.reset(compiler.getSpecification().quantities);
		}
		updateContextComboBox(0);
	}
	
	private void updateSelectedContext() {
		if(inputPanel == null) return;
		int index = cmbContext.getSelectedIndex();
		if(index < 0) return;
		inputPanel.writeToContext(contexts.get(index));
	}
	
	private void updateContextComboBox(int selectedIndex) {
		setIgnoreItemStateChanges(true);
		cmbContext.removeAllItems();
		for(int i=0; i<contexts.size(); i++) {
			cmbContext.addItem(new ContextComboItem(i));
		}
		if(selectedIndex < 0) {
			selectedIndex = cmbContext.getSelectedIndex();
		}
		if(selectedIndex >= 0) {
			cmbContext.setSelectedIndex(selectedIndex);
			if(inputPanel != null) {
				inputPanel.fillFromContext(contexts.get(selectedIndex));
			}
		}
		setIgnoreItemStateChanges(false);
	}
	
	private boolean isGenerateEnabled() {
		String prefText = prefTextArea.getText().trim();
		return !prefText.isEmpty() && !prefText.equals(lastPrefText);
	}
	
	private boolean isCompareEnabled() {
		String prefText = prefTextArea.getText().trim();
		return (posetProvider != null) && prefText.equals(lastPrefText);
	}
	
	private void updateControls(boolean updateInput) {
		butGenerate.setEnabled(isGenerateEnabled());
		if(updateInput) {
			updateInputComponents();
		}
		boolean cmpEnabled = isCompareEnabled();
		butCompare.setEnabled(cmpEnabled);
		butNewContext.setEnabled(cmpEnabled);
		butDeleteContext.setEnabled(cmpEnabled);
		cmbContext.setEnabled(cmpEnabled);
		inputPanel.setEnabled(cmpEnabled);
		javaTextPane.setEnabled(cmpEnabled);
	}

	
	private static final String IGNORE_ITEM_STATE_CHANGES = "IGNORE_ITEM_STATE_CHANGES";
	private void setIgnoreItemStateChanges(boolean ignore) {
		cmbContext.putClientProperty(IGNORE_ITEM_STATE_CHANGES, ignore ? Boolean.TRUE : Boolean.FALSE);
	}

	private boolean isIgnoreItemStateChanges() {
		return (cmbContext.getClientProperty(IGNORE_ITEM_STATE_CHANGES) == Boolean.TRUE);
	}
	
	private void updateInputComponents() {
		Specification specification = (compiler == null) ? null : compiler.getSpecification();
		inputPanel.setSpecification(specification);
		resetContexts();
		cmbContext.setSelectedIndex(0);
		
		
		validate();
	}

	public void initDividers() {
		mainSplitPane.setDividerLocation(0.5);
		leftPane.setDividerLocation(0.5);		
		validate();
	}
	
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
		log.info("PreferantoPanel successfully initialized.");
	}
}
