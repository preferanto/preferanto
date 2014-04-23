package org.preferanto.javac;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class InMemoryClassLoader extends URLClassLoader {
	private final Map<String, InMemoryJavaFileObject> javaFileObjects;
	
	public InMemoryClassLoader(Map<String, InMemoryJavaFileObject> javaFileObjects) {
		super(new URL[0]);
		this.javaFileObjects = javaFileObjects;
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		InMemoryJavaFileObject javaFileObject = javaFileObjects.get(name);
		if(javaFileObject == null) {
			throw new ClassNotFoundException("Class " + name + " not found in map.");
		}
		byte[] buf = javaFileObject.getBinary();
		return defineClass(name, buf, 0, buf.length);
	}
}
