package org.preferanto.experiment.poset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.preferanto.poset.PosetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileListener implements PosetProvider.Listener {
	private static final Logger log = LoggerFactory.getLogger(FileListener.class);

	protected static AtomicInteger LISTENER_ID = new AtomicInteger();
	
	protected final FileConfiguration config;
	
	protected final File dir;
	
	protected boolean enabled = true;
	protected String currentSuffix = "";
	protected int currentFileIndex = -1;
	
	protected final SimpleDateFormat sdf = new SimpleDateFormat("YYYY.MM.dd-HH.mm.ss.SSS.");

	public FileListener(FileConfiguration config) {
		this.config = config;

		String tsPart = config.isTimestampDirEnabled() ? (sdf.format(new Date())) : "";
		int listenerId = LISTENER_ID.incrementAndGet();
		String dirName = config.getDirName() + "/" + tsPart + listenerId;
		this.dir = new File(dirName);
		this.dir.mkdirs();
		if(!this.dir.isDirectory()) {
			log.error("Cannot create directory " + dirName);
			this.enabled = false;
		} else {
			log.info("dir: " + dir.getAbsolutePath());
		}
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public FileOutputStream getOutputStream(int currentRule) {
		if(!enabled) return null;
		String rulePart = (currentRule > 0) ? ("-rule" + currentRule) : "";
		currentFileIndex++;
		String fileName = currentFileIndex + "." + config.getFileNamePrefix() + rulePart + currentSuffix + config.getFileExtension();
		try {
			return new FileOutputStream(new File(dir, fileName));
		} catch(FileNotFoundException e) {
			log.warn("Cannot create outputStream for: " + dir.getAbsolutePath() + ", " + fileName);
			return null;
		}
	}

}
