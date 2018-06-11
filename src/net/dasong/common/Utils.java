package net.dasong.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	// 去除doc中表格cell内容行尾的特殊字符
	public static String cutStr(String val) {
		return val.replaceAll("", "").trim().toLowerCase();
	}

	// 去除doc中表格cell内容行尾的特殊字符，并删除空格
	public static String cutStr2(String val) {
		return val.replaceAll("", "").trim().toLowerCase().replaceAll(" ", "");
	}

	// 提取字段类型，不加长度；
	// VARCHAR2(1024)=>VARCHAR2
	// CLOB=>VARCHAR2
	// INT=>NUMBER
	// NUMBER(38)=>NUMBER
	public static String convertType2MetaFor(String type) {
		if (type.startsWith("VARCHAR2") || type.startsWith("CLOB")) {
			return "VARCHAR2";
		} else if (type.startsWith("INT") || type.startsWith("NUMBER")) {
			return "NUMBER";
		}

		return type;
	}

	public static String getDefault(String comment) {
		String defaultStr = "";

		// System.out.println(comment);

		// 默认值中可能为包含英文单双引号、中文引号等，需要处理这些符号
		Pattern p = Pattern.compile("(.+)?（默认为['\"“”]?([[^'\"“”]?]+)+['\"“”]?）(.+)?");

		// 字符太长matcher会卡住，原因不知道，暂时先取前30个配置，默认值一定要写在前30个字符里
		Matcher m = p.matcher(comment.substring(0, comment.length() > 30 ? 30 : comment.length()));
		boolean b = m.matches();

		if (b) {
			defaultStr = m.group(2);

			// 删除空格
			defaultStr = defaultStr.trim();
		}

		return defaultStr;
	}
}
