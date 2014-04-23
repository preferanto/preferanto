package org.preferanto.javac;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.tools.SimpleJavaFileObject;

public class InMemoryJavaFileObject extends SimpleJavaFileObject {
	private ByteArrayOutputStream baos = null;

	protected InMemoryJavaFileObject(String className, Kind kind) throws URISyntaxException {
		super(new URI(className), kind);
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return new ByteArrayInputStream(baos.toByteArray());
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		baos = new ByteArrayOutputStream();
		return baos;
	}

	public byte[] getBinary() {
		return baos.toByteArray();
	}
}
