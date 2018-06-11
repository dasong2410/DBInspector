package net.dasong.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;

import net.dasong.common.Constants;
import net.dasong.doc.Index;
import net.dasong.doc.TableBody;
import net.dasong.doc.WordTableRow;

public class DbBodyUtils {
	// 获取db中table内容信息，并封装到HashMap
	public LinkedHashMap<String, TableBody> getDBTableMap() {
		String sql = Constants.SQL_MAP.get("tbs");

		LinkedHashMap<String, TableBody> thm = new LinkedHashMap<String, TableBody>();
		Statement stmt = DbUtil.getStatement();

		try {
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String tableName = rs.getString("table_name");
				// System.out.println(tableName);

				TableBody tb = new TableBody();
				thm.put(tableName, tb);

				// 获取db中表字段信息，存入hashmap
				LinkedHashMap<String, WordTableRow> rhm = new LinkedHashMap<String, WordTableRow>();
				tb.setRowMap(rhm);

				sql = Constants.SQL_MAP.get("cols");

				PreparedStatement pstmt = DbUtil.getPreparedStatement(sql);
				pstmt.setString(1, tableName);
				ResultSet colRs = pstmt.executeQuery();

				while (colRs.next()) {
					String colName = colRs.getString("column_name");
					String dataType = colRs.getString("data_type");
					String nullable = colRs.getString("is_nullable");

					WordTableRow row = new WordTableRow();
					row.setColName(colName);
					row.setDataType(dataType);
					row.setNullable(nullable);
					rhm.put(colName, row);

					// System.out.println(" " + row);
				}

				colRs.close();
				pstmt.close();

				// 获取db中的索引信息，存入hashmap
				LinkedHashMap<String, Index> ihm = new LinkedHashMap<String, Index>();
				tb.setIdxMap(ihm);

				sql = Constants.SQL_MAP.get("idxes");

				pstmt = DbUtil.getPreparedStatement(sql);
				pstmt.setString(1, tableName);
				ResultSet idxRs = pstmt.executeQuery();

				while (idxRs.next()) {
					String idxName = idxRs.getString("idx_name");
					String idxCols = idxRs.getString("idx_cols");

					Index idx = new Index(idxName, idxCols);
					ihm.put(idxName, idx);
				}

				idxRs.close();
				pstmt.close();
			}

			rs.close();
			DbUtil.closeStat(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return thm;
	}

	// 获取db中table内容信息，并封装到HashMap
	public LinkedHashMap<String, String> getDBSeqMap() {
		String sql = "select sequence_name from user_sequences";

		LinkedHashMap<String, String> shm = new LinkedHashMap<String, String>();
		Statement stmt = DbUtil.getStatement();

		try {
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String seqName = rs.getString("SEQUENCE_NAME");
				shm.put(seqName, seqName);
			}

			rs.close();
			DbUtil.closeStat(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return shm;
	}

	// 获取db中metadata table内容信息，并封装到HashMap
	public LinkedHashMap<String, TableBody> getDBTableMetaMap() {
		String sql = "select template_name,\n"
				+ "cursor(select ename, decode(type, 1, 'VARCHAR2', 2, 'NUMBER', 4, 'ERROR') type, decode(isindex, 0, 0, null, 0, 1) isindex from base_field_info where template_id=a.template_id) fields\n"
				+ "from base_template_info a where template_name!='暂无' order by template_id";

		LinkedHashMap<String, TableBody> thm = new LinkedHashMap<String, TableBody>();
		Statement stmt = DbUtil.getStatement();

		try {
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String tableName = rs.getString("TEMPLATE_NAME");
				// System.out.println(tableName);

				TableBody tb = new TableBody();
				thm.put(tableName, tb);

				// 获取db中表字段信息，存入hashmap
				LinkedHashMap<String, MetaRow> mrhm = new LinkedHashMap<String, MetaRow>();
				tb.setMetaRowMap(mrhm);

				ResultSet colRs = (ResultSet) rs.getObject("FIELDS");

				while (colRs.next()) {
					String ename = colRs.getString("ENAME");
					String type = colRs.getString("TYPE");
					int isIndex = colRs.getInt("ISINDEX");

					MetaRow metaRow = new MetaRow();
					metaRow.setEname(ename);
					metaRow.setType(type);
					metaRow.setIsIndex(isIndex);

					mrhm.put(ename, metaRow);
					// System.out.println(" " + row);
				}

				colRs.close();
			}

			rs.close();
			DbUtil.closeStat(stmt);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return thm;
	}
}
