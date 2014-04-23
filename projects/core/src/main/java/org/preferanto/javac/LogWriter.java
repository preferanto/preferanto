package org.preferanto.javac;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWriter extends Writer {
	private static final Logger log = LoggerFactory.getLogger(LogWriter.class);
	
	private String firstMessage = "";
	
	@Override
	public void close() throws IOException {
		// Nothing to do here
	}
	
	@Override
	public void flush() throws IOException {
		// Nothing to do here
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		String message = new String(cbuf, off, len);
		if(firstMessage.length() == 0) {
			firstMessage = message;
		}
		log.warn(message);
	}
	
	public String getFirstMessage() {
		return firstMessage;
	}
}