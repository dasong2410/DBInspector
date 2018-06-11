package net.dasong.doc;

public class Index {
	private String idxName;
	private String idxCols;

	public Index(String idxName, String idxCols) {
		this.idxName = idxName;
		this.idxCols = idxCols;
	}

	public String getIdxName() {
		return idxName;
	}

	public String getIdxCols() {
		return idxCols;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Index) {
			Index idx = (Index) o;

			if (idxName.equals(idx.getIdxName()) && idxCols.equals(idx.getIdxCols())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		return "Index [idxName=" + idxName + ", idxCols=" + idxCols + "]";
	}
}
