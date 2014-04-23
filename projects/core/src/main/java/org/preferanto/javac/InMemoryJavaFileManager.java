package org.preferanto.javac;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

public class InMemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
	private final Map<String, InMemoryJavaFileObject> javaFileObjects;
	
	protected InMemoryJavaFileManager(JavaFileManager fileManager, Map<String, InMemoryJavaFileObject> javaFileObjects) {
		super(fileManager);
		this.javaFileObjects = javaFileObjects;
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		InMemoryJavaFileObject javaFileObject;
		try {
			javaFileObject = new InMemoryJavaFileObject(className, kind);
		} catch (URISyntaxException e) {
			throw new IOException("Invalid className: " + className, e);
		}
		javaFileObjects.put(className, javaFileObject);
		return javaFileObject;
	}
	
	
}