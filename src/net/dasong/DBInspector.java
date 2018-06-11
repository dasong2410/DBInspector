package net.dasong;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import net.dasong.common.Constants;
import net.dasong.common.InfoPrinter;
import net.dasong.common.PropsReader;
import net.dasong.common.SqlReader;
import net.dasong.db.DbBodyUtils;
import net.dasong.db.DbUtil;
import net.dasong.doc.DocBodyUtils;
import net.dasong.doc.DocFileFilter;
import net.dasong.doc.Index;
import net.dasong.doc.TableBody;
import net.dasong.doc.WordTableRow;

public class DBInspector {

	private InfoPrinter infoPrinter = new InfoPrinter();

	public void showUsage() {
		System.out.println("Usage:");
		System.out.println("  java -jar dbi.jar db_conn_str files");
		System.out.println("  db_conn_str: 数据库连接串（username/password@ip:5432/dbname）");
		System.out.println("  files      : doc文件名，多个文件用英文逗号隔开（只支持docx）");
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

				System.out.println("|" + StringUtils.rightPad(tbName, 40, " ") + " |Y          |N         |");
				System.out.println("+-----------------------------------------+-----------+----------+");
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
					System.out.println("|" + StringUtils.rightPad(tbName, 40, " ") + " |N          |Y         |");
					System.out.println("+-----------------------------------------+-----------+----------+");
				}
			}
		}
	}

	// 对比doc与db中序列差异
	public void diffSeqs(LinkedHashMap<String, String> docSeqMap, LinkedHashMap<String, String> dbSeqMap) {
		boolean showTitle = true;

		System.out.println("\n********************************************************");
		System.out.println("  序列差异（文档中个数：" + docSeqMap.size() + " 数据库中个数：" + dbSeqMap.size() + "）");
		System.out.println("********************************************************");

		// 打印文档中存在，数据库中不存在的序列
		Iterator<Entry<String, String>> docSeqIt = docSeqMap.entrySet().iterator();

		while (docSeqIt.hasNext()) {
			Entry<String, String> seqEntry = docSeqIt.next();
			String seqName = seqEntry.getKey();

			if (!dbSeqMap.containsKey(seqName)) {
				if (showTitle) {
					infoPrinter.showTableHeader("Sequence Name");

					showTitle = false;
				}

				System.out.println("|" + StringUtils.rightPad(seqName, 30, " ") + " |Y          |N         |");
				System.out.println("+-------------------------------+-----------+----------+");
			}
		}

		// 打印DB中存在，DOC中不存的序列（双向比较才会打印）
		if (Constants.COMP_DIRECTION == Constants.SHOW_DB_DIFF) {
			// 打印数据库中存在，文档中不存在的序列
			Iterator<Entry<String, String>> dbSeqIt = dbSeqMap.entrySet().iterator();

			while (dbSeqIt.hasNext()) {
				Entry<String, String> seqEntry = dbSeqIt.next();
				String seqName = seqEntry.getKey();

				if (!docSeqMap.containsKey(seqName)) {
					if (showTitle) {
						infoPrinter.showTableHeader("Sequence Name");

						showTitle = false;
					}

					System.out.println("|" + StringUtils.rightPad(seqName, 30, " ") + " |N          |Y         |");
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
				HashMap<String, WordTableRow> docRowHm = docTb.getRowMap();

				TableBody dbTb = dbTbMap.get(tbName);
				HashMap<String, WordTableRow> dbRowHm = dbTb.getRowMap();

				// 比较文档中字段在表中是否存在、数据类型等是否一致
				Iterator<Entry<String, WordTableRow>> docRowIt = docRowHm.entrySet().iterator();
				while (docRowIt.hasNext()) {
					Entry<String, WordTableRow> er = docRowIt.next();
					String docCname = er.getKey();
					WordTableRow docRow = er.getValue();

					if (dbRowHm.containsKey(docCname)) {
						WordTableRow dbRow = dbRowHm.get(docCname);

						if (!docRow.equals(dbRow)) {
							// 打印表格式title
							if (showTab) {
								infoPrinter.showColTableHeader(tbName, "Null?");

								showTab = false;
							}

							infoPrinter.showColTabeRow(docCname, docRow.getDataType(), docRow.getNullable(),
									dbRow.getDataType(), dbRow.getNullable());
						}
					} else {
						// 打印表格式title
						if (showTab) {
							infoPrinter.showColTableHeader(tbName, "Null?");

							showTab = false;
						}

						infoPrinter.showColTabeRow(docCname, docRow.getDataType(), docRow.getNullable(), "", "");
					}
				}

				// 打印DB中存在，DOC中不存的字段（双向比较才会打印）
				if (Constants.COMP_DIRECTION == Constants.SHOW_DB_DIFF) {
					// 比较数据库中的字段在文档中是否存在
					Iterator<Entry<String, WordTableRow>> dbRowIt = dbRowHm.entrySet().iterator();
					while (dbRowIt.hasNext()) {
						Entry<String, WordTableRow> dbEr = dbRowIt.next();
						String dbCname = dbEr.getKey();
						WordTableRow dbRow = dbEr.getValue();

						if (!docRowHm.containsKey(dbCname)) {
							// 打印表格式title
							if (showTab) {
								infoPrinter.showColTableHeader(tbName, "Null?");

								showTab = false;
							}

							infoPrinter.showColTabeRow(dbCname, "", "", dbRow.getDataType(), dbRow.getNullable());
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

	// 对比doc与db中索引差异
	public void diffIdxes(LinkedHashMap<String, TableBody> docTbMap, LinkedHashMap<String, TableBody> dbTbMap) {
		System.out.println(
				"*************************************************************************************************");
		System.out.println("  索引差异");
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
				HashMap<String, Index> docIdxHm = docTb.getIdxMap();

				TableBody dbTb = dbTbMap.get(tbName);
				HashMap<String, Index> dbIdxHm = dbTb.getIdxMap();

				// 比较文档中字段在表中是否存在、数据类型等是否一致
				Iterator<Entry<String, Index>> docIdxIt = docIdxHm.entrySet().iterator();

				while (docIdxIt.hasNext()) {
					Entry<String, Index> er = docIdxIt.next();
					String docIdxName = er.getKey();
					Index docIdx = er.getValue();

					if (dbIdxHm.containsKey(docIdxName)) {
						Index dbIdx = dbIdxHm.get(docIdxName);

						if (!docIdx.equals(dbIdx)) {
							// 打印表格式title
							if (showTab) {
								infoPrinter.showIdxTableHeader(tbName);

								showTab = false;
							}

							infoPrinter.showIdxTabeRow(docIdxName, docIdx.getIdxCols(), dbIdx.getIdxCols());
						}
					} else {
						// 打印表格式title
						if (showTab) {
							infoPrinter.showIdxTableHeader(tbName);

							showTab = false;
						}

						infoPrinter.showIdxTabeRow(docIdxName, docIdx.getIdxCols(), "");
					}
				}

				// 打印DB中存在，DOC中不存的索引（双向比较才会打印）
				if (Constants.COMP_DIRECTION == Constants.SHOW_DB_DIFF) {
					// 比较数据库中的字段在文档中是否存在
					Iterator<Entry<String, Index>> dbIdxIt = dbIdxHm.entrySet().iterator();

					while (dbIdxIt.hasNext()) {
						Entry<String, Index> dbEr = dbIdxIt.next();
						String dbIndxName = dbEr.getKey();
						Index dbIdx = dbEr.getValue();

						if (!docIdxHm.containsKey(dbIndxName)) {
							// 打印表格式title
							if (showTab) {
								infoPrinter.showIdxTableHeader(tbName);

								showTab = false;
							}

							infoPrinter.showIdxTabeRow(dbIndxName, "", dbIdx.getIdxCols());
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
		SqlReader.read();

		Pattern pattern = Pattern.compile("(.+)/(.+)@(.+)/(.+)");
		Matcher matcher = pattern.matcher(Constants.DB_CONN_STR);

		if (matcher.matches()) {
			// jdbc:oracle:thin:@172.16.26.116:1521:ora11g
			Constants.DB_URL = "jdbc:postgresql://" + matcher.group(3) + "/" + matcher.group(4);
			Constants.DB_USER = matcher.group(1);
			Constants.DB_PWD = matcher.group(2);
		} else {
			System.err.println("数据库连接串不合法，参考格式：username/password@ip:1521/sid");
		}

		DBInspector dbi = new DBInspector();
		DocBodyUtils docBodyUtils = new DocBodyUtils();
		DbBodyUtils dbBodyUtils = new DbBodyUtils();

		File metadocDir = new File(Constants.DBDOC_DIR);
		File[] dbDocs = metadocDir.listFiles(new DocFileFilter());

		System.out.println(
				"*************************************************************************************************");
		System.out.println("  文档解析");
		System.out.println(
				"*************************************************************************************************");

		// doc目录下没有合适的文档则退出
		if (dbDocs.length == 0) {
			System.out.println("目录下没有合适的文档：" + Constants.DBDOC_DIR);

			System.exit(0);
		}

		LinkedHashMap<String, TableBody> docTbMap = new LinkedHashMap<String, TableBody>();
		LinkedHashMap<String, String> docSeqMap = new LinkedHashMap<String, String>();

		for (File docFile : dbDocs) {
			String docFileName = docFile.getAbsolutePath();
			System.out.println(docFileName);

			XWPFDocument wordDoc = docBodyUtils.getDocx(docFile);
			LinkedHashMap<String, TableBody> tmpDocTbMap = docBodyUtils.getDocTableMap(wordDoc);
			docTbMap.putAll(tmpDocTbMap);

			LinkedHashMap<String, String> tmpDocSeqMap = docBodyUtils.getDocSeqMap();
			docSeqMap.putAll(tmpDocSeqMap);
		}

		LinkedHashMap<String, TableBody> dbTbMap = dbBodyUtils.getDBTableMap();
		// LinkedHashMap<String, String> dbSeqMap = dbBodyUtils.getDBSeqMap();

		DbUtil.closeConn();

		dbi.diffTabs(docTbMap, dbTbMap);
		// dbi.diffSeqs(docSeqMap, dbSeqMap);
		dbi.diffCols(docTbMap, dbTbMap);
		dbi.diffIdxes(docTbMap, dbTbMap);
	}
}
