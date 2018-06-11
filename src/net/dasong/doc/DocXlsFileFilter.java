package net.dasong.doc;

import java.io.File;
import java.io.FileFilter;

public class DocXlsFileFilter implements FileFilter {

	@Override
	public boolean accept(File file) {
		String fileName = file.getName().toLowerCase();

		if (!file.isDirectory() && (fileName.endsWith(".docx") || fileName.endsWith(".xlsx"))) {
			return true;
		}

		return false;
	}

}
