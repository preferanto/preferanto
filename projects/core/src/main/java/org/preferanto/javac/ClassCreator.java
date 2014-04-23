package org.preferanto.javac;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.preferanto.core.PreferantoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassCreator {
	private static final Logger log = LoggerFactory.getLogger(ClassCreator.class);

	private final LogWriter logWriter;
	private final JavaCompiler compiler;
	private final Map<String, InMemoryJavaFileObject> javaFileObjects;
	private final InMemoryClassLoader loader;
	
    public ClassCreator() {
    	this.logWriter = new LogWriter();
    	this.compiler = ToolProvider.getSystemJavaCompiler();
    	if(compiler == null) {
    		throw new PreferantoException("No JavaCompiler implementation available.");
    	}
    	this.javaFileObjects = new HashMap<String, InMemoryJavaFileObject>();
    	this.loader = AccessController.doPrivileged(new PrivilegedAction<InMemoryClassLoader>() {
			@Override
			public InMemoryClassLoader run() {
				return new InMemoryClassLoader(javaFileObjects);
			}
    	});
    }
    
	public Class<?> createClass(String className, String code) throws PreferantoException {		
		JavaSourceFromString source = new JavaSourceFromString(className, code);
		List<JavaSourceFromString> sources = Arrays.asList(source);
		LogDiagnosticListener diagnosticListener = new LogDiagnosticListener(className);
    	StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);
    	JavaFileManager fm = new InMemoryJavaFileManager(fileManager, javaFileObjects);
    	try {
    		CompilationTask task = compiler.getTask(logWriter, fm, diagnosticListener, null, null, sources);
    		boolean ok = task.call();
    		if(!ok) {
    			throw new PreferantoException("Compilation of class " + className + " failed.\n" + diagnosticListener);
    		}
    	} finally {
    		try {
				fm.close();
			} catch (IOException e) {
				throw new PreferantoException(e);
			}
    	}
		try {
			return Class.forName(className, true, loader);
		} catch (ClassNotFoundException e) {
			throw new PreferantoException(e);
		}
	}
    
	private static final String CODE = "public class B implements Runnable { public void run() {System.out.println(\"Hello, world!\"); } }";
	public static void main(String[] args) {
		ClassCreator creator = new ClassCreator();
		Runnable runnable;
		try {
			Class<?> cls = creator.createClass("B", CODE);
			runnable = (Runnable)cls.newInstance();
		} catch (Exception e) {
			log.error("Cannot create class.", e);
			return;
		}
		runnable.run();
	}
}
