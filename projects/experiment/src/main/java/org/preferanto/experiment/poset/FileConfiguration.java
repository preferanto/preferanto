package org.preferanto.experiment.poset;

public class FileConfiguration {
	private final String dirName;
	private final String fileNamePrefix;
	private final String fileExtension;
	
	private boolean timestampDirEnabled = true; 
	
	public FileConfiguration(String dirName, String fileNamePrefix, String fileExtension) {
		super();
		this.dirName = dirName;
		this.fileNamePrefix = fileNamePrefix;
		this.fileExtension = fileExtension;
	}

	public String getDirName() {
		return dirName;
	}
	
	public String getFileNamePrefix() {
		return fileNamePrefix;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public boolean isTimestampDirEnabled() {
		return timestampDirEnabled;
	}
	
	public void setTimestampDirEnabled(boolean timestampDirEnabled) {
		this.timestampDirEnabled = timestampDirEnabled;
	}	
}
