package org.preferanto.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


import org.preferanto.antlr.ArgSetter;
import org.preferanto.antlr.ByteCodeBuilder;
import org.preferanto.antlr.DefaultPrefOpSetter;
import org.preferanto.antlr.JavaPreferantoEval;
import org.preferanto.antlr.PreferantoLexer;
import org.preferanto.antlr.PreferantoParser;
import org.preferanto.antlr.PreferantoTree;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.stringtemplate.StringTemplateGroup;

public class PreferantoCompiler {
	private final SymbolTable symbolTable;
	private final Specification specification;
	private final String JavaPreferantoEvalSourceCode;
	private final byte[] evalByteCode;
	private final byte[] objectivesByteCode;
	private final String prefEvalClassNameDot;
	private final String prefObjectivesClassNameDot;

	public PreferantoCompiler(String inputText) throws IOException, RecognitionException {
		this(new ANTLRStringStream(inputText));
	}

	public PreferantoCompiler(InputStream input) throws IOException, RecognitionException {
		this(new ANTLRInputStream(input));
	}

	public PreferantoCompiler(CharStream input) throws IOException, RecognitionException {
		PreferantoLexer lex = new PreferantoLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		PreferantoParser p = new PreferantoParser(tokens);
		PreferantoNodeAdapter adapter = new PreferantoNodeAdapter();
		p.setTreeAdaptor(adapter);
		org.preferanto.antlr.PreferantoParser.specification_return result = p.specification();

		PreferantoNode ast = (PreferantoNode) result.getTree();
		CommonTreeNodeStream nodes = new CommonTreeNodeStream(adapter, ast);
		PreferantoTree treeParser = new PreferantoTree(nodes);
		treeParser.setTreeAdaptor(adapter);
		PreferantoNode modifiedAST = (PreferantoNode) treeParser.specification().getTree();

		DefaultPrefOpSetter defaultPrefOpSetter = new DefaultPrefOpSetter(nodes);
		defaultPrefOpSetter.setTreeAdaptor(adapter);
		modifiedAST = (PreferantoNode) defaultPrefOpSetter.downup(modifiedAST, false);
		
		ArgSetter argSetter = new ArgSetter(nodes);
		argSetter.setTreeAdaptor(adapter);
		modifiedAST = (PreferantoNode) argSetter.downup(modifiedAST, false);
		
		InputStream inputStream = getClass().getResourceAsStream("/JavaPreferantoEval.stg");
        Reader templateReader = new InputStreamReader(inputStream);        
        StringTemplateGroup templates = new StringTemplateGroup(templateReader);
        templateReader.close();

		nodes = new CommonTreeNodeStream(adapter, modifiedAST);
		nodes.setTokenStream(tokens);
		this.symbolTable = treeParser.symtab;
		this.specification = treeParser.spec;
		JavaPreferantoEval JavaPreferantoEval = new JavaPreferantoEval(nodes, symbolTable, specification);
		JavaPreferantoEval.setTemplateLib(templates);
		org.preferanto.antlr.JavaPreferantoEval.specification_return ret = JavaPreferantoEval.specification();
		
		this.JavaPreferantoEvalSourceCode = String.valueOf(ret.getTemplate());
		
		
		nodes = new CommonTreeNodeStream(adapter, modifiedAST);
		nodes.setTokenStream(tokens);
		ByteCodeBuilder byteCodeBuilder = new ByteCodeBuilder(nodes, specification);
		byteCodeBuilder.setTreeAdaptor(adapter);

		modifiedAST = (PreferantoNode) byteCodeBuilder.specification().getTree();		

		this.evalByteCode = byteCodeBuilder.getEvalByteCode();
		this.objectivesByteCode = byteCodeBuilder.getObjectivesByteCode();
		this.prefEvalClassNameDot = byteCodeBuilder.getPrefEvalClassNameDot();
		this.prefObjectivesClassNameDot = byteCodeBuilder.getPrefObjectivesClassNameDot();
	}
	
	public SymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	public Specification getSpecification() {
		return specification;
	}
	
	public String getJavaPreferantoEvalSourceCode() {
		return JavaPreferantoEvalSourceCode;
	}
	
	public byte[] getEvalByteCode() {
		return evalByteCode;
	}
	
	public byte[] getObjectivesByteCode() {
		return objectivesByteCode;
	}

	public String getPrefEvalClassNameDot() {
		return prefEvalClassNameDot;
	}
	
	public String getPrefObjectivesClassNameDot() {
		return prefObjectivesClassNameDot;
	}
}
