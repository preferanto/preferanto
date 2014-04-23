package org.preferanto.experiment.util;

import java.io.File;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LogConfig {
	private static final Logger log = LoggerFactory.getLogger(LogConfig.class);

	public static void setLogbackDir(String logbackDir) {
		File configDir = new File(logbackDir);
		if (!configDir.isDirectory()) {
			throw new IllegalArgumentException("Missing configuration directory " + configDir.getAbsolutePath());
		}
		File logbackFile = new File(configDir, "logback.xml");
		setLogbackFile(logbackFile);
	}

	public static void setLogbackPath(String logbackPath) {
		File logbackFile = new File(logbackPath);
		setLogbackFile(logbackFile);
	}

	public static void setLogbackFile(File logbackFile) {
		if (!logbackFile.exists()) {
			log.warn("Cannot reconfigure logback. Missing file " + logbackFile.getAbsolutePath());
		} else {
			LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(context);
				context.reset();
				configurator.doConfigure(logbackFile.getAbsolutePath());
			} catch (JoranException je) {
				// StatusPrinter will handle this
			}
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
		}
	}

	public static void setLogbackResource(String resourceName) {
		InputStream stream = LogConfig.class.getResourceAsStream(resourceName);
		setLogbackStream(stream);
	}

	public static void setLogbackStream(InputStream stream) {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure(stream);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	}

}
