package net.dasong.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import net.dasong.common.Constants;
import net.dasong.common.InfoPrinter;
import net.dasong.common.Utils;
import net.dasong.db.MetaRow;

public class DocBodyUtils {
	private int ORACLE_COL_CNT = 4;
	private int HBASE_COL_CNT = 2;
	private int ORACLE_COL_START_POS = 2;
	private int HBASE_COL_START_POS = 5;

	// 输出内容的表格基本都是通过这个类来打印
	private InfoPrinter infoPrinter = new InfoPrinter();

	// 存放序列名
	private LinkedHashMap<String, String> docSeqMap = new LinkedHashMap<String, String>();

	public LinkedHashMap<String, String> getDocSeqMap() {
		return docSeqMap;
	}

	/*
	 * 获取doc对象
	 */
	public XWPFDocument getDocx(File docFile) {
		FileInputStream fis = null;
		XWPFDocument wordDoc = null;

		try {
			fis = new FileInputStream(docFile);
			wordDoc = new XWPFDocument(fis);
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(1);
		}

		return wordDoc;
	}

	/*
	 * 获取xlsx Workbook对象
	 */
	public Workbook getWorkbook(String xlsFileName) {
		File xlsFile = new File(xlsFileName);
		String xlsName = xlsFile.getName();

		Workbook wb = null;

		try {
			if (xlsName.endsWith("xlsx")) {
				wb = new XSSFWorkbook(new FileInputStream(xlsFile));
			} else if (xlsName.endsWith("xls")) {
				wb = new HSSFWorkbook(new FileInputStream(xlsFile));
			}
		} catch (IOException e) {
			e.printStackTrace();

			System.exit(1);
		}

		return wb;
	}

	/*
	 * 获取doc文件中table内容信息，并封装到HashMap
	 */
	public LinkedHashMap<String, TableBody> getDocTableMap(XWPFDocument wordDoc) {
		LinkedHashMap<String, TableBody> tbMap = new LinkedHashMap<String, TableBody>();

		List<XWPFTable> tbList = wordDoc.getTables();

		for (XWPFTable tb : tbList) {
			List<XWPFTableRow> rowList = tb.getRows();
			int rowCnt = rowList.size();

			if (rowCnt > ORACLE_COL_START_POS) {
				XWPFTableRow tbr = rowList.get(0);
				String tbNameLabel = Utils.cutStr2(tbr.getCell(0).getText());

				if (tbNameLabel.equals("表名")) {
					String tbName = Utils.cutStr2(tbr.getCell(1).getText());
					// System.out.println(
					// "-----------------------------------------------------------------------------------"
					// + tbName);

					if (!tbName.contains(Constants.OBSOLETE)) {

						// 表结构
						TableBody tbb = new TableBody();
						tbb.setTableName(tbName);
						tbMap.put(tbName, tbb);

						LinkedHashMap<String, WordTableRow> rowMap = new LinkedHashMap<String, WordTableRow>();
						tbb.setRowMap(rowMap);

						LinkedHashMap<String, Index> idxMap = new LinkedHashMap<String, Index>();
						tbb.setIdxMap(idxMap);

						for (int i = ORACLE_COL_START_POS; i < rowCnt; i++) {
							tbr = tb.getRow(i);
							List<XWPFTableCell> cellList = tbr.getTableCells();
							int cellCnt = cellList.size();

							if (cellCnt > 0) {
								String c0 = Utils.cutStr2(tbr.getCell(0).getText());

								// 文档中表结构中可能会有 补充说明 一行，跳过此行
								if (c0.equals("补充说明") || c0.equals("相关约束") || c0.equals("预估数据量级")
										|| c0.equals("数据保存时间")) {
									continue;
								} else if (c0.equals("相关索引")) {
									// 表相关的索引都写在一个单元格内，格式为
									// 索引名:字段1,字段2,字段3，索引名与字段间用英文冒号分隔，多个字段用英文分号分隔
									// 每个索引占一行
									String idxStr = Utils.cutStr2(tbr.getCell(1).getTextRecursively());

									if (idxStr != null && !idxStr.equals("")) {
										String[] idxes = idxStr.split("\t");

										for (String idx : idxes) {

											String[] idxFields = idx.split(":");

											try {
												String idxName = idxFields[0].trim();

												Index index = new Index(idxName, idxFields[1].trim().replace(" ", ""));

												idxMap.put(idxName, index);
											} catch (ArrayIndexOutOfBoundsException aiobe) {
												System.err.println(
														Constants.INDENT_SPACE + "请检查表 " + tbName + " 相关索引是否有格式错误");

												System.exit(1);
											}
										}
									}
								} else if (c0.equals("相关序列")) {
									// 获取文档中sequence
									String seqStr = Utils.cutStr2(tbr.getCell(1).getTextRecursively());
									if (seqStr != null && !seqStr.equals("")) {
										String[] seqs = seqStr.split("\t");

										for (String seq : seqs) {
											String seqFields[] = seq.split(":");
											String seqName = seqFields[0].trim();
											String startVal = seqFields[1].trim();

											docSeqMap.put(seqName, startVal);
										}
									}
								} else {
									// 解析文档中表结构文件字段信息
									WordTableRow row = new WordTableRow();

									// 解析字段信息：字段名、数据类型、是否可空、说明
									// 默认每个字段都会有以上四类型信息，如果不够则打印错误，并跳过此字段
									if (cellCnt == ORACLE_COL_CNT) {
										// 字段名中不会包含空格， 如果有空格可以直接删除
										String colName = Utils.cutStr2(tbr.getCell(0).getText());

										// 文档表中可能会存在一些看不到的元素，比如多出看不到的行，需要过滤一下
										if (colName != null && !colName.equals("")) {
											if (!colName.contains(Constants.OBSOLETE)) {
												row.setColName(colName);

												String dataType = Utils.cutStr2(tbr.getCell(1).getText());
												dataType = dataType.equals("integer") ? "int" : dataType;
												row.setDataType(dataType);

												String nullable = Utils.cutStr(tbr.getCell(2).getText());
												String comment = Utils.cutStr(tbr.getCell(3).getText());
												String defaultVal = Utils.getDefault(comment);

												if (nullable.equals("not null") || nullable.equals("n")) {
													nullable = "NO";
												} else {
													nullable = "YES";
												}

												row.setNullable(nullable);
												row.setDefaultVal(defaultVal);
												row.setComment(comment);

												rowMap.put(colName, row);
											}
										} else {
											infoPrinter.showDocNullColErr(tbName, i + 1);
										}
									} else {
										System.err.println(Constants.INDENT_SPACE + "表 " + tbName + " 第 " + (i + 1)
												+ " 行不够 " + ORACLE_COL_CNT + " 列");
									}
								}
							}

						}
					}
				}
			}
		}

		return tbMap;
	}

	/*
	 * 解析 HBase元数据文档（docx格式）
	 */
	public LinkedHashMap<String, TableBody> getDocMetaTableMapHbase(XWPFDocument wordDoc) {
		LinkedHashMap<String, TableBody> tbMap = new LinkedHashMap<String, TableBody>();

		List<XWPFTable> tbList = wordDoc.getTables();

		for (XWPFTable tb : tbList) {
			List<XWPFTableRow> rowList = tb.getRows();
			int rowCnt = rowList.size();

			if (rowCnt > HBASE_COL_START_POS) {
				XWPFTableRow tbr = rowList.get(1);
				String tbNameLabel = Utils.cutStr2(tbr.getCell(0).getText());

				if (tbNameLabel.equals("表类型名") || tbNameLabel.equals("表名")) {
					String tbName = Utils.cutStr2(tbr.getCell(1).getText()).replace(" ", "");

					if (!tbName.contains(Constants.OBSOLETE) && !tbName.equals("必填")) {

						// 表结构
						TableBody tbb = new TableBody();
						tbb.setTableName(tbName);
						tbMap.put(tbName, tbb);

						LinkedHashMap<String, Index> idxMap = new LinkedHashMap<String, Index>();
						tbb.setIdxMap(idxMap);

						LinkedHashMap<String, MetaRow> metaRowMap = new LinkedHashMap<String, MetaRow>();
						tbb.setMetaRowMap(metaRowMap);

						for (int i = HBASE_COL_START_POS; i < rowCnt; i++) {
							tbr = tb.getRow(i);
							List<XWPFTableCell> cellList = tbr.getTableCells();
							int cellCnt = cellList.size();

							if (cellCnt > 0) {
								String c0 = Utils.cutStr2(tbr.getCell(0).getText());

								// 文档中表结构中可能会有 补充说明 一行，跳过此行
								if (c0.equals("")) {
									// 解析文档中表结构文件字段信息
									MetaRow metaRow = new MetaRow();

									// 解析字段信息：字段名、数据类型、是否可空、说明
									// 默认每个字段都会有以上四类型信息，如果不够则打印错误，并跳过此字段
									if (cellCnt >= HBASE_COL_CNT) {
										// 字段名中不会包含空格， 如果有空格可以直接删除
										String colName = Utils.cutStr2(tbr.getCell(1).getText());
										colName = colName.replaceAll("[\\W&&[^_]]", "");

										// 文档表中可能会存在一些看不到的元素，比如多出多出看不到的行，需要过滤一下
										if (colName != null && !colName.equals("")) {
											if (!colName.contains(Constants.OBSOLETE)) {
												metaRow.setEname(colName);

												// hbase文档中没有数据类型，默认都设置成VARCHAR2
												metaRow.setType("VARCHAR2");

												// 此处暂不处理索引标识，默认先给0，在 索引 if
												// 子句中再处理
												metaRow.setIsIndex(0);

												metaRowMap.put(colName, metaRow);
											}
										} else {
											infoPrinter.showDocNullColErr(tbName, i + 1);
										}
									} else {
										System.err.println(Constants.INDENT_SPACE + "表 " + tbName + " 第 " + (i + 1)
												+ " 行小于 " + HBASE_COL_CNT + " 列");
									}
								} else if (c0.equals("主键列") || c0.equals("索引列")) {
									// 大数据的文档格式比较规范，所以这边做个特殊处理，将不是 数字、字母、下划线
									// 的字符都替换成 英文逗号；下一步再根据 英文逗号 切分
									String idxStr = Utils.cutStr2(tbr.getCell(1).getTextRecursively());
									idxStr = idxStr.replaceAll("[\\W&&[^_]]", ",");

									// 如果 索引 字段有值，则切分成单个字段，装进map中
									if (idxStr != null && !idxStr.equals("")) {
										String[] idxFields = idxStr.split(",");

										for (String idxName : idxFields) {
											if (!idxName.equals("")) {
												idxMap.put(idxName, null);
											}
										}
									}
								}
							}

						}
					}
				}
			}
		}

		return tbMap;
	}

	/*
	 * 解析 普通项目Oracle元数据文档（docx格式）
	 */
	public LinkedHashMap<String, TableBody> getDocMetaTableMapOracle(XWPFDocument wordDoc) {
		LinkedHashMap<String, TableBody> tbMap = new LinkedHashMap<String, TableBody>();

		List<XWPFTable> tbList = wordDoc.getTables();

		for (XWPFTable tb : tbList) {
			List<XWPFTableRow> rowList = tb.getRows();
			int rowCnt = rowList.size();

			if (rowCnt > ORACLE_COL_START_POS) {
				XWPFTableRow tbr = rowList.get(0);
				String tbNameLabel = Utils.cutStr2(tbr.getCell(0).getText());

				if (tbNameLabel.equals("表名")) {
					String tbName = Utils.cutStr2(tbr.getCell(1).getText()).replace(" ", "");

					if (!tbName.contains(Constants.OBSOLETE)) {

						// 表结构
						TableBody tbb = new TableBody();
						tbb.setTableName(tbName);
						tbMap.put(tbName, tbb);

						LinkedHashMap<String, MetaRow> rowMap = new LinkedHashMap<String, MetaRow>();
						tbb.setMetaRowMap(rowMap);

						LinkedHashMap<String, Index> idxMap = new LinkedHashMap<String, Index>();
						tbb.setIdxMap(idxMap);

						for (int i = ORACLE_COL_START_POS; i < rowCnt; i++) {
							tbr = tb.getRow(i);
							List<XWPFTableCell> cellList = tbr.getTableCells();
							int cellCnt = cellList.size();

							if (cellCnt > 0) {
								String c0 = Utils.cutStr2(tbr.getCell(0).getText());

								// 文档中表结构中可能会有 补充说明 一行，跳过此行
								if (c0.equals("补充说明") || c0.equals("相关序列")) {
									continue;
								} else if (c0.equals("相关索引")) {
									// 表相关的索引都写在一个单元格内，格式为
									// 索引名:字段1,字段2,字段3，索引名与字段间用英文冒号分隔，多个字段用英文分号分隔
									// 每个索引占一行
									// getTextRecursively()
									String idxStr = Utils.cutStr2(tbr.getCell(1).getTextRecursively());
									if (idxStr != null && !idxStr.equals("")) {
										String[] idxes = idxStr.split("\t");

										for (String idx : idxes) {

											String[] idxFields = idx.split(":");

											try {
												idxFields = idxFields[1].trim().replace(" ", "").split(",");

												for (String col : idxFields) {
													idxMap.put(col, null);
												}

											} catch (ArrayIndexOutOfBoundsException aiobe) {
												System.err.println(
														Constants.INDENT_SPACE + "请检查表 " + tbName + " 相关索引是否有格式错误");

												System.exit(1);
											}
										}
									}
								} else {
									// 解析文档中表结构文件字段信息
									MetaRow row = new MetaRow();

									// 解析字段信息：字段名、数据类型、是否可空、说明
									// 默认每个字段都会有以上四类型信息，如果不够则打印错误，并跳过此字段
									if (cellCnt == ORACLE_COL_CNT) {
										// 字段名中不会包含空格， 如果有空格可以直接删除
										String colName = Utils.cutStr2(tbr.getCell(0).getText());

										// 文档表中可能会存在一些看不到的元素，比如多出多出看不到的行，需要过滤一下
										if (colName != null && !colName.equals("")) {
											if (!colName.contains(Constants.OBSOLETE)) {
												row.setEname(colName);
												row.setType(Utils
														.convertType2MetaFor(Utils.cutStr2(tbr.getCell(1).getText())));

												rowMap.put(colName, row);
											}
										} else {
											infoPrinter.showDocNullColErr(tbName, i + 1);
										}
									} else {
										System.err.println(Constants.INDENT_SPACE + "表 " + tbName + " 第 " + (i + 1)
												+ " 行不够 " + ORACLE_COL_CNT + " 列");
									}
								}
							}

						}
					}
				}
			}
		}

		return tbMap;
	}

	/*
	 * 解析 海量元数据文档（xlsx格式）
	 */
	public LinkedHashMap<String, TableBody> getDocMetaTableMapMassOracle(Workbook wb) {
		LinkedHashMap<String, TableBody> tbMap = new LinkedHashMap<String, TableBody>();

		// xls行数计算器
		int rowNo = 0;
		// 从第几个sheet开始取数据
		int startSheet = 5;
		// 从第几行开始取数据
		int startRow = 1;
		// sheet个数
		int sheetCnt = wb.getNumberOfSheets();

		// 字段名 所在列，xls第一列的位置是0
		int enamePos = 1;
		// 字段类型 所在列
		int typePos = 2;
		// 索引标识 所在列
		int idxPos = 9;

		for (int i = startSheet; i < sheetCnt; i++) {
			try {
				rowNo = 0;

				Sheet sheet = wb.getSheetAt(i);

				// sheet name
				String sheetName = sheet.getSheetName();
				String prot = sheetName.split("-")[1];

				// 海量sheet的名字可能会出现中文，此种格式是不需要的，所以将 数字、字母、下划线 之后的都替换掉
				// 替换后如果值为空就不用处理此sheet的数据了
				prot = prot.replaceAll("[\\W&&[^_]]", "");

				if (!prot.equals("")) {
					TableBody tbb = new TableBody();
					tbb.setTableName(prot);
					tbMap.put(prot, tbb);

					LinkedHashMap<String, MetaRow> rowMap = new LinkedHashMap<String, MetaRow>();
					tbb.setMetaRowMap(rowMap);

					LinkedHashMap<String, Index> idxMap = new LinkedHashMap<String, Index>();
					tbb.setIdxMap(idxMap);

					Iterator<Row> it = sheet.rowIterator();

					while (it.hasNext()) {
						if (rowNo++ < startRow) {
							it.next();
							continue;
						}

						Row row = (Row) it.next();

						Cell enameCell = row.getCell(enamePos);
						Cell typeCell = row.getCell(typePos);
						Cell isIndexCell = row.getCell(idxPos);

						// String ename = enameCell.getStringCellValue();
						String ename = Utils.cutStr2(enameCell.getStringCellValue());
						ename = ename.replaceAll("[\\W&&[^_]]", "");

						// 元数据只要数据类型，字段长度不需要，所以要作一个转换
						String type = Utils.convertType2MetaFor(typeCell.getStringCellValue());
						String isIndexStr = isIndexCell.getStringCellValue();

						if (isIndexStr.equals("是")) {
							idxMap.put(ename, null);
						}

						MetaRow metaRow = new MetaRow();
						metaRow.setEname(ename);
						metaRow.setType(type);

						rowMap.put(ename, metaRow);
					}
				} else {
					System.err.println(Constants.INDENT_SPACE + "Sheet Name: " + sheetName + " 协议名错误");
				}
			} catch (Exception e) {
				System.err.println(Constants.INDENT_SPACE + e.getMessage());
			}
		}

		return tbMap;
	}
}
