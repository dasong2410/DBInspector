package net.dasong.common;

import java.util.LinkedHashMap;

public class Constants {

	// 程序根目录
	public static final String ROOT_DIR = System.getProperty("user.dir");
	public static final String CFG_DIR = ROOT_DIR + "/cfg";
	public static final String CFG_FILE = CFG_DIR + "/config.cfg";
	public static final String DOC_DIR = ROOT_DIR + "/doc";
	public static final String DBDOC_DIR = DOC_DIR + "/db";
	public static final String METADOC_DIR = DOC_DIR + "/meta";

	// 数据库连接串（username/password@ip:1521/sid）
	public static String DB_CONN_STR;

	// 元数据 数据库连接串（username/password@ip:1521/sid）
	public static String META_CONN_STR;

	// jdbc:oracle:thin:@172.16.26.116:1521:rac4
	public static String DB_URL;
	public static String DB_USER;
	public static String DB_PWD;

	// public static int SHOW_DOC_DIFF = 1;
	// 默认中作单向比较，以文档为基准，比较文档中的内容在数据库中是否存在
	// 如果有双向比较参数传入，则作双向比较，即，比较文档中的内容是否在数据库中存在，数据库中的内容是否在文档中存在
	public static int SHOW_DB_DIFF = 2;

	// 表名中包含 废弃 则此表不需要处理
	public static String OBSOLETE = "废弃";

	// 1：单向，以文档为基准，比较文档中的内容在数据库中是否存在
	// 2：双向，比较文档中的内容是否在数据库中存在，数据库中的内容是否在文档中存在
	public static int COMP_DIRECTION = 1;

	public static String INDENT_SPACE = "  ";

	// 数据库文档中表结构如果有空行，是否打印警告
	// 0：不打印
	// 1：打印
	public static int NULL_COL_WARN = 1;

	public static String DB_TYPE;

	public static LinkedHashMap<String, String> SQL_MAP;

	public static final String SQL_FILEDIR = CFG_DIR + "/sql";
}
