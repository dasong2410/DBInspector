package net.dasong.common;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SqlReader {

	public static void read() {
		// TODO Auto-generated method stub
		String name;
		String sql;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			LinkedHashMap<String, String> sqlMap = new LinkedHashMap<String, String>();

			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(Constants.SQL_FILEDIR + "/" + Constants.DB_TYPE + ".xml");

			NodeList nodeList = doc.getElementsByTagName("sql");
			int nodeCnt = nodeList.getLength();

			// 读取sql语句，并存储
			for (int i = 0; i < nodeCnt; i++) {
				name = nodeList.item(i).getAttributes().getNamedItem("name").getNodeValue();
				sql = nodeList.item(i).getFirstChild().getNodeValue();

				sqlMap.put(name, sql);
			}

			Constants.SQL_MAP = sqlMap;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 载入参数
		PropsReader.read();
		SqlReader.read();

		Set<?> entrySet = Constants.SQL_MAP.entrySet();

		Iterator<?> it = entrySet.iterator();

		while (it.hasNext()) {
			Map.Entry<?, ?> entry = (Entry<?, ?>) it.next();

			System.out.println(entry.getKey());

			System.out.println(entry.getValue());
		}
	}

}
