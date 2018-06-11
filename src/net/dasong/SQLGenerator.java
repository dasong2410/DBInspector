package net.dasong;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import net.dasong.common.Constants;
import net.dasong.common.PropsReader;
import net.dasong.doc.DocBodyUtils;
import net.dasong.doc.DocFileFilter;
import net.dasong.doc.Index;
import net.dasong.doc.TableBody;
import net.dasong.doc.WordTableRow;

public class SQLGenerator {

	public static void main(String[] args) {
		PropsReader.read();

		Pattern pattern = Pattern.compile("(.+)/(.+)@(.+)/(.+)");
		Matcher matcher = pattern.matcher(Constants.DB_CONN_STR);

		if (matcher.matches()) {
			// jdbc:oracle:thin:@172.16.26.116:1521:ora11g
			Constants.DB_URL = "jdbc:oracle:thin:@" + matcher.group(3) + ":" + matcher.group(4);
			Constants.DB_USER = matcher.group(1);
			Constants.DB_PWD = matcher.group(2);
		} else {
			System.err.println("数据库连接串不合法，参考格式：username/password@ip:1521/sid");
		}

		DocBodyUtils docBodyUtils = new DocBodyUtils();

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

		System.out.println(
				"\n*************************************************************************************************");
		System.out.println("  生成sql");
		System.out.println(
				"*************************************************************************************************");

		Iterator<Entry<String, String>> docSeqIt = docSeqMap.entrySet().iterator();
		StringBuffer seqSb = new StringBuffer();

		// 创建序列
		while (docSeqIt.hasNext()) {
			Entry<String, String> seqEntry = docSeqIt.next();
			String seqName = seqEntry.getKey();
			String seqVal = seqEntry.getValue();

			seqSb.append("create sequence " + seqName.toLowerCase() + " start with " + seqVal + ";\n");
		}

		if (seqSb.length() > 0) {
			System.out.println(seqSb.toString());
		}

		// 表、索引sql
		Iterator<Entry<String, TableBody>> tbIt = docTbMap.entrySet().iterator();

		while (tbIt.hasNext()) {
			StringBuffer sb = new StringBuffer();
			StringBuffer idxSb = new StringBuffer();

			Entry<String, TableBody> tbe = tbIt.next();
			String tbName = tbe.getKey();
			TableBody tbb = tbe.getValue();

			sb.append("create table " + tbName.toLowerCase() + "\n(\n");

			LinkedHashMap<String, WordTableRow> rhm = tbb.getRowMap();
			Iterator<Entry<String, WordTableRow>> rit = rhm.entrySet().iterator();

			while (rit.hasNext()) {
				Entry<String, WordTableRow> re = rit.next();
				String colName = re.getKey().toLowerCase();
				WordTableRow wRow = re.getValue();

				String dataType = wRow.getDataType().toLowerCase();
				String nullable = wRow.getNullableSQL();
				String defaultVal = wRow.getDefaultVal();

				if (!defaultVal.equals("")) {
					// System.out.println(dataType +
					// "=====================================================" +
					// wRow.getDefaultVal());
					if (dataType.startsWith("varchar") || dataType.startsWith("char")) {
						defaultVal = "'" + defaultVal + "'";
					}

					defaultVal = "default " + defaultVal + " ";
				}

				sb.append("  " + StringUtils.rightPad(colName, 32, " ") + StringUtils.rightPad(dataType, 16, " ")
						+ defaultVal + nullable + ",\n");
			}

			sb.setLength(sb.length() - 2);

			sb.append("\n) tablespace tb_tbs_placeholder;\n");

			LinkedHashMap<String, Index> ihm = tbb.getIdxMap();
			Iterator<Entry<String, Index>> iit = ihm.entrySet().iterator();

			while (iit.hasNext()) {
				Entry<String, Index> ie = iit.next();
				String idxName = ie.getKey();

				Index idx = ie.getValue();
				String idxCols = idx.getIdxCols();

				idxSb.append("\ncreate index " + idxName.toLowerCase() + " on " + tbName.toLowerCase() + "("
						+ idxCols.toLowerCase() + ") tablespace idx_tbs_placeholder;");
			}

			sb.append(idxSb.length() > 0 ? idxSb.toString() + "\n" : "");

			System.out.println(sb.toString());
		}
	}

}
