package org.preferanto.experiment.poset;

import java.awt.Color;

public class ImageConfiguration extends FileConfiguration {
	private int width = 600;
	private int height = 600;
	
	private Color backgroundColor = Color.WHITE;
	private String imgFormatName = "png";
	
	public ImageConfiguration(String dirName, String fileNamePrefix, String fileExtension) {
		super(dirName, fileNamePrefix, fileExtension);
	}

	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	
	public Color getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public String getImgFormatName() {
		return imgFormatName;
	}
	public void setImgFormatName(String imgFormatName) {
		this.imgFormatName = imgFormatName;
	}
}
