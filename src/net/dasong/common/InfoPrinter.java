package net.dasong.common;

import org.apache.commons.lang3.StringUtils;

public class InfoPrinter {
	public void showDocNullColErr(String tbName, int lineNo) {
		if (Constants.NULL_COL_WARN == 1) {
			System.err.println(Constants.INDENT_SPACE + "表 " + tbName + " 第 " + lineNo + " 行 字段名为空");
		}
	}

	public void showTableHeader(String titleName) {
		System.out.println("+-----------------------------------------+-----------+----------+");
		System.out.println("|" + StringUtils.rightPad(titleName, 40, " ") + " |Doc Exist? |DB Exist? |");
		System.out.println("+-----------------------------------------+-----------+----------+");
	}

	// 打印 字段区别 的表头信息
	public void showColTableHeader(String tbName, String nullOrIndex) {
		System.out.println(tbName);
		System.out.println(
				"+-----------------------------------------+---------------------+---------+---------------------+---------+");
		System.out.println(
				"|                                         |Document File                  |Database                       |");
		System.out.println(
				"+-----------------------------------------+---------------------+---------+---------------------+---------+");
		System.out.println(
				"|Column Name                              |Data Type            |" + StringUtils.rightPad(nullOrIndex, 8, " ")
						+ " |Data Type            |" + StringUtils.rightPad(nullOrIndex, 8, " ") + " |");
		System.out.println(
				"+-----------------------------------------+---------------------+---------+---------------------+---------+");
	}

	// 打印 字段区别 的具体字段信息
	public void showColTabeRow(String docCname, String docCdatatype, String docCnullable, String dbCdatatype,
			String dbCnullable) {
		System.out.println("|" + StringUtils.rightPad(docCname, 40, " ") + " |"
				+ StringUtils.rightPad(docCdatatype, 20, " ") + " |" + StringUtils.rightPad(docCnullable, 8, " ") + " |"
				+ StringUtils.rightPad(dbCdatatype, 20, " ") + " |" + StringUtils.rightPad(dbCnullable, 8, " ") + " |");
		System.out.println(
				"+-----------------------------------------+---------------------+---------+---------------------+---------+");
	}

	// 打印 字段区别 的表头信息
	public void showIdxTableHeader(String tbName) {
		System.out.println(tbName);
		System.out.println(
				"+-----------------------------------------+-------------------------------+-------------------------------+");
		System.out.println(
				"|                                         |Document File                  |Database                       |");
		System.out.println(
				"+-----------------------------------------+-------------------------------+-------------------------------+");
		System.out.println(
				"|Index Name                               |Column Name                    |Column Name                    |");
		System.out.println(
				"+-----------------------------------------+-------------------------------+-------------------------------+");
	}

	public void showIdxTabeRow(String idxName, String docIdxCols, String dbIdxCols) {
		String[] docIdxColsArray = docIdxCols.split(",");
		String[] dbIdxColsArray = dbIdxCols.split(",");

		int docIdxColsArrayLen = docIdxColsArray.length;
		int dbIdxColsArrayLen = dbIdxColsArray.length;

		// 每个索引可能会包含多个字段，要比较一下一上索引的字段个数doc、数据库中哪个多；打印输出信息，需要按最大的字段个数打印相应的行数
		int len = docIdxColsArrayLen > dbIdxColsArrayLen ? docIdxColsArrayLen : dbIdxColsArrayLen;

		// 索引中包含多个字段，每个字段需要单独打印一行信息
		for (int i = 0; i < len; i++) {
			String tmpIdxName = i == 0 ? idxName : "";
			String tmpDocIdxCols = docIdxColsArrayLen > i ? docIdxColsArray[i] : "";
			String tmpDbIdxCols = dbIdxColsArrayLen > i ? dbIdxColsArray[i] : "";

			System.out.println("|" + StringUtils.rightPad(tmpIdxName, 40, " ") + " |"
					+ StringUtils.rightPad(tmpDocIdxCols, 30, " ") + " |" + StringUtils.rightPad(tmpDbIdxCols, 30, " ")
					+ " |");
		}

		System.out.println(
				"+-----------------------------------------+-------------------------------+-------------------------------+");
	}

}
