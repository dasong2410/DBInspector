package net.dasong.doc;

import java.io.File;
import java.io.FileFilter;

public class DocFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		String fileName = file.getName().toLowerCase();

		if (!file.isDirectory() && (fileName.endsWith(".docx"))) {
			return true;
		}

		return false;
	}

}
