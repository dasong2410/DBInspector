package net.dasong.common;

import java.io.File;
import java.io.FilenameFilter;

public class XMLFilenameFilter implements FilenameFilter {

	@Override
	public boolean accept(File file, String fileName) {
		boolean isXML = fileName.toLowerCase().endsWith(".xml");

		return isXML;
	}
}
