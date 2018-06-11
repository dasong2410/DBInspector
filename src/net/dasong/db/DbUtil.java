package net.dasong.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import net.dasong.common.Constants;

public class DbUtil {
	public static Connection CONNECTION;

	// 加载oracle驱动
	static {
		try {
			if (Constants.DB_TYPE == "pg") {
				Class.forName("org.postgresql.Driver");
			} else if (Constants.DB_TYPE == "ora") {
				Class.forName("oracle.jdbc.driver.OracleDriver");
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();

			System.exit(1);
		}
	}

	// 建立数据库连接
	public static void createConnection() {
		if (DbUtil.CONNECTION == null) {
			try {
				DbUtil.CONNECTION = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USER, Constants.DB_PWD);
			} catch (SQLException e) {
				e.printStackTrace();

				System.exit(1);
			}
		}
	}

	// 获取数据库会话
	public static Statement getStatement() {
		Statement stat = null;

		if (DbUtil.CONNECTION == null) {
			DbUtil.createConnection();
		}

		try {
			stat = DbUtil.CONNECTION.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();

			System.exit(1);
		}

		return stat;
	}

	// 获取Prepared会话
	public static PreparedStatement getPreparedStatement(String sql) {
		PreparedStatement pstat = null;

		if (DbUtil.CONNECTION == null) {
			DbUtil.createConnection();
		}

		try {
			pstat = DbUtil.CONNECTION.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();

			System.exit(1);
		}

		return pstat;
	}

	// 关闭数据库连接
	public static void closeConn() {
		try {
			DbUtil.CONNECTION.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 关闭会话
	public static void closeStat(Statement stat) {
		try {
			stat.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Statement stat = DbUtil.getStatement();
		try {
			stat.execute("create table t_yy(c1 number)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
