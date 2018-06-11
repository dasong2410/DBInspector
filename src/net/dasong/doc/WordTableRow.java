package net.dasong.doc;

/**
 * @author Administrator
 *
 */
public class WordTableRow {
	private String colName;
	private String dataType;
	private String nullable;
	private String defaultVal;
	private String comment;

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getNullable() {
		return nullable;
	}

	public String getNullableSQL() {
		return nullable.toLowerCase().equals("yes") ? "null" : "not null";
	}

	public void setNullable(String nullable) {
		this.nullable = nullable;
	}

	public String getDefaultVal() {
		return defaultVal;
	}

	public void setDefaultVal(String defaultVal) {
		this.defaultVal = defaultVal;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WordTableRow) {
			WordTableRow row = (WordTableRow) o;

			if (colName.equals(row.getColName()) && dataType.equals(row.getDataType())
					&& nullable.equals(row.getNullable())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return "Row [colName=" + colName + ", dataType=" + dataType + ", nullable=" + nullable + ", comment=" + comment
				+ "]";
	}

}
