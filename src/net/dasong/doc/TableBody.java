package net.dasong.doc;

import java.util.LinkedHashMap;

import net.dasong.db.MetaRow;

public class TableBody {
	private String tableName;
	private LinkedHashMap<String, WordTableRow> rowMap;
	private LinkedHashMap<String, Index> idxMap;

	private LinkedHashMap<String, MetaRow> metaRowMap;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public LinkedHashMap<String, WordTableRow> getRowMap() {
		return rowMap;
	}

	public void setRowMap(LinkedHashMap<String, WordTableRow> rowMap) {
		this.rowMap = rowMap;
	}

	public LinkedHashMap<String, Index> getIdxMap() {
		return idxMap;
	}

	public void setIdxMap(LinkedHashMap<String, Index> idxMap) {
		this.idxMap = idxMap;
	}

	public LinkedHashMap<String, MetaRow> getMetaRowMap() {
		return metaRowMap;
	}

	public void setMetaRowMap(LinkedHashMap<String, MetaRow> metaRowMap) {
		this.metaRowMap = metaRowMap;
	}

}
