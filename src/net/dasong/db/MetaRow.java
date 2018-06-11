package net.dasong.db;

public class MetaRow {
	private String ename;
	private String type;
	private int isIndex;

	public String getEname() {
		return ename;
	}

	public void setEname(String ename) {
		this.ename = ename;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIsIndex() {
		return isIndex;
	}

	public void setIsIndex(int isIndex) {
		this.isIndex = isIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MetaRow) {
			MetaRow metaRow = (MetaRow) o;

			if (ename.equals(metaRow.getEname()) && type.equals(metaRow.getType()) && isIndex == metaRow.getIsIndex()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return "MetaRow [ename=" + ename + ", type=" + type + ", isIndex=" + isIndex + "]";
	}

}
