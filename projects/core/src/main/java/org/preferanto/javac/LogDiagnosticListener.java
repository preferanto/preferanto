package org.preferanto.javac;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDiagnosticListener implements DiagnosticListener<JavaFileObject> {
	private static final Logger log = LoggerFactory.getLogger(LogDiagnosticListener.class);
	
	private final String className;
	private String firstMessage = "";
	private Kind firstMessageKind = null;
	
	public LogDiagnosticListener(String className) {
		this.className = className;
	}
	
	@Override
	public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
		String msg = "[DIAGNOSTIC/" + className + "]: "+ diagnostic.getMessage(null);
		Kind kind = diagnostic.getKind();
		if((firstMessage.length() == 0) || (kind == Kind.ERROR && firstMessageKind != Kind.ERROR)) {
			firstMessage = msg;
			firstMessageKind = kind;
		}
		switch(diagnostic.getKind()) {
			case ERROR: 
				log.error(msg); break;
			case WARNING:
			case MANDATORY_WARNING:
				log.warn(msg); break;
			default: 
				log.error(msg); break;
		}
	}
	
	public String getFirstMessage() {
		return firstMessage;
	}
	
	@Override
	public String toString() {
		return firstMessage;
	}
}