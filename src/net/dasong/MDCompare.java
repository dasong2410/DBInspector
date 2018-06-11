package net.dasong;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import net.dasong.common.Constants;
import net.dasong.common.InfoPrinter;
import net.dasong.common.PropsReader;
import net.dasong.db.DbBodyUtils;
import net.dasong.db.MetaRow;
import net.dasong.doc.DocBodyUtils;
import net.dasong.doc.DocXlsFileFilter;
import net.dasong.doc.Index;
import net.dasong.doc.TableBody;

public class MDCompare {
	private InfoPrinter infoPrinter = new InfoPrinter();

	public void setMetaRowIndex(LinkedHashMap<String, TableBody> docTbMap) {
		Iterator<Entry<String, TableBody>> docIt = docTbMap.entrySet().iterator();

		System.out.println("设置索引列");

		while (docIt.hasNext()) {
			Entry<String, TableBody> et = docIt.next();
			String tbName = et.getKey();
			TableBody docTbb = et.getValue();

			// System.out.println(tbName);

			LinkedHashMap<String, Index> idxMap = docTbb.getIdxMap();
			Iterator<String> idxIt = idxMap.keySet().iterator();

			LinkedHashMap<String, MetaRow> mrhm = docTbb.getMetaRowMap();

			// 设置MetaRow的isIndex字段
			while (idxIt.hasNext()) {
				String ename = idxIt.next();

				if (mrhm.containsKey(ename)) {
					MetaRow mr = mrhm.get(ename);
					mr.setIsIndex(1);
				} else {
					System.err.println(Constants.INDENT_SPACE + "表 " + tbName + " 中 索引字段 " + ename + " 不存在");
				}
			}
		}
	}

	// 对比doc与db中表差异
	public void diffTabs(LinkedHashMap<String, TableBody> docTbMap, LinkedHashMap<String, TableBody> dbTbMap) {
		boolean showTitle = true;

		System.out.println("\n********************************************************");
		System.out.println("  表差异（文档中个数：" + docTbMap.size() + " 数据库中个数：" + dbTbMap.size() + "）");
		System.out.println("********************************************************");

		// 打印DOC中存在，DB中不存的表
		Set<String> tbNameSet = docTbMap.keySet();
		Iterator<String> it = tbNameSet.iterator();

		while (it.hasNext()) {

			String tbName = it.next();

			if (!dbTbMap.containsKey(tbName)) {
				if (showTitle) {
					infoPrinter.showTableHeader("Table Name");

					showTitle = false;
				}

				System.out.println("|" + StringUtils.rightPad(tbName, 30, " ") + " |Y          |N         |");
				System.out.println("+-------------------------------+-----------+----------+");
			}
		}

		// 打印DB中存在，DOC中不存的表（双向比较才会打印）
		if (Constants.COMP_DIRECTION == Constants.SHOW_DB_DIFF) {
			// if (mode / 10 == 1) {
			tbNameSet = dbTbMap.keySet();
			it = tbNameSet.iterator();

			while (it.hasNext()) {
				if (showTitle) {
					infoPrinter.showTableHeader("Table Name");

					showTitle = false;
				}

				String tbName = it.next();

				if (!docTbMap.containsKey(tbName)) {
					System.out.println("|" + StringUtils.rightPad(tbName, 30, " ") + " |N          |Y         |");
					System.out.println("+-------------------------------+-----------+----------+");
				}
			}
		}
	}

	// 对比doc与db中表字段差异
	public void diffCols(LinkedHashMap<String, TableBody> docTbMap, LinkedHashMap<String, TableBody> dbTbMap) {
		System.out.println(
				"\n*************************************************************************************************");
		System.out.println("  字段差异");
		System.out.println(
				"*************************************************************************************************");

		Set<Entry<String, TableBody>> est = docTbMap.entrySet();
		Iterator<Entry<String, TableBody>> eit = est.iterator();

		while (eit.hasNext()) {
			boolean showTab = true;

			Entry<String, TableBody> et = eit.next();
			String tbName = et.getKey();
			TableBody docTb = et.getValue();

			// System.out.println(tbName);

			if (dbTbMap.containsKey(tbName)) {
				// HashMap<String, Row> docRowHm = docTb.getRowMap();
				LinkedHashMap<String, MetaRow> docMetaRowHm = docTb.getMetaRowMap();

				TableBody dbTb = dbTbMap.get(tbName);
				LinkedHashMap<String, MetaRow> dbRowHm = dbTb.getMetaRowMap();

				// 比较文档中字段在表中是否存在、数据类型等是否一致
				Iterator<Entry<String, MetaRow>> docRowIt = docMetaRowHm.entrySet().iterator();
				while (docRowIt.hasNext()) {
					Entry<String, MetaRow> er = docRowIt.next();
					String docCname = er.getKey();
					MetaRow docRow = er.getValue();

					if (dbRowHm.containsKey(docCname)) {
						MetaRow dbRow = dbRowHm.get(docCname);

						if (!docRow.equals(dbRow)) {
							// 打印表格式title
							if (showTab) {
								infoPrinter.showColTableHeader(tbName, "Index?");

								showTab = false;
							}

							infoPrinter.showColTabeRow(docCname, docRow.getType(), docRow.getIsIndex() + "",
									dbRow.getType(), dbRow.getIsIndex() + "");
						}
					} else {
						// 打印表格式title
						if (showTab) {
							infoPrinter.showColTableHeader(tbName, "Index?");

							showTab = false;
						}

						infoPrinter.showColTabeRow(docCname, docRow.getType(), docRow.getIsIndex() + "", "", "");
					}
				}

				// 打印DB中存在，DOC中不存的字段（双向比较才会打印）
				if (Constants.COMP_DIRECTION == Constants.SHOW_DB_DIFF) {
					// 比较数据库中的字段在文档中是否存在
					Iterator<Entry<String, MetaRow>> dbRowIt = dbRowHm.entrySet().iterator();
					while (dbRowIt.hasNext()) {
						Entry<String, MetaRow> dbEr = dbRowIt.next();
						String dbCname = dbEr.getKey();
						MetaRow dbRow = dbEr.getValue();

						if (!docMetaRowHm.containsKey(dbCname)) {
							// 打印表格式title
							if (showTab) {
								infoPrinter.showColTableHeader(tbName, "Index?");

								showTab = false;
							}

							infoPrinter.showColTabeRow(dbCname, "", "", dbRow.getType(), dbRow.getIsIndex() + "");
						}
					}
				}

				// 为了排版，每个表之前加一个空行
				if (!showTab) {
					System.out.println();
				}
			}
		}
	}

	public static void main(String[] args) {
		PropsReader.read();

		Pattern pattern = Pattern.compile("(.+)/(.+)@(.+)/(.+)");
		Matcher matcher = pattern.matcher(Constants.META_CONN_STR);

		if (matcher.matches()) {
			// jdbc:oracle:thin:@172.16.26.116:1521:ora11g
			Constants.DB_URL = "jdbc:oracle:thin:@" + matcher.group(3) + ":" + matcher.group(4);
			Constants.DB_USER = matcher.group(1);
			Constants.DB_PWD = matcher.group(2);
		} else {
			System.err.println("数据库连接串不合法，参考格式：username/password@ip:1521/sid");
		}

		MDCompare mdc = new MDCompare();

		// DocBodyUtils
		DocBodyUtils docBodyUtils = new DocBodyUtils();

		LinkedHashMap<String, TableBody> docTbMap = new LinkedHashMap<String, TableBody>();

		File metadocDir = new File(Constants.METADOC_DIR);
		File[] metaDocs = metadocDir.listFiles(new DocXlsFileFilter());

		System.out.println(
				"*************************************************************************************************");
		System.out.println("  文档解析");
		System.out.println(
				"*************************************************************************************************");

		// doc目录下没有合适的文档则退出
		if (metaDocs.length == 0) {
			System.out.println("目录下没有合适的文档：" + Constants.METADOC_DIR);

			System.exit(0);
		}

		for (File docFile : metaDocs) {
			String docName = docFile.getName();
			String docFileName = docFile.getAbsolutePath();
			System.out.println(docFileName);

			if (docName.contains("HBASE")) {
				// HBASE 文档
				XWPFDocument wordDoc = docBodyUtils.getDocx(docFile);
				LinkedHashMap<String, TableBody> docTbMap1 = docBodyUtils.getDocMetaTableMapHbase(wordDoc);

				docTbMap.putAll(docTbMap1);
			} else if (docName.endsWith("xlsx")) {
				// 海量相关表，xlsx
				Workbook wb = docBodyUtils.getWorkbook(docFileName);
				LinkedHashMap<String, TableBody> docTbMap3 = docBodyUtils.getDocMetaTableMapMassOracle(wb);

				docTbMap.putAll(docTbMap3);
			} else {
				// 普通 oracle 文档
				XWPFDocument wordDoc = docBodyUtils.getDocx(docFile);
				LinkedHashMap<String, TableBody> docTbMap2 = docBodyUtils.getDocMetaTableMapOracle(wordDoc);

				docTbMap.putAll(docTbMap2);
			}
		}

		// 设置MetaRow索引字段
		mdc.setMetaRowIndex(docTbMap);

		// db
		DbBodyUtils dbBodyUtils = new DbBodyUtils();
		LinkedHashMap<String, TableBody> dbTbMetaMap = dbBodyUtils.getDBTableMetaMap();

		// 表名比较
		mdc.diffTabs(docTbMap, dbTbMetaMap);
		mdc.diffCols(docTbMap, dbTbMetaMap);
	}
}
