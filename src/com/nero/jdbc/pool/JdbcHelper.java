package com.nero.jdbc.pool;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * JDBC帮助类
 *
 * @created nero
 * @date 2018/8/23 14:28
 */
public class JdbcHelper {
    public static String DRIVER;//mysql驱动名称
    public static String URL;//默认数据库连接信息
    public static String FULLURL;//默认数据库连接信息(包含账号密码)
    public static String USERNAME;//默认数据库账号
    public static String PASSWORD;//默认数据库密码

    public static Connection conn;//默认数据库连接
    public static Properties jdbcProperties = new Properties();//数据库配置信息
    private static JdbcPool connPool;//默认数据库连接池
    public static int CONNPOOLSIZE;//默认数据库连接池大小
    public static int CONNPOOLMAXSIZE;//默认数据库连接池最大大小

    static {
        try {
            jdbcProperties.load(JdbcHelper.class.getResourceAsStream("jdbcConfig.Properties"));
            DRIVER = jdbcProperties.getProperty("mysql.jdbc.driver");
            URL = jdbcProperties.getProperty("mysql.jdbc.url");
            FULLURL = jdbcProperties.getProperty("mysql.jdbc.fullurl");
            USERNAME = jdbcProperties.getProperty("mysql.jdbc.user");
            PASSWORD = jdbcProperties.getProperty("mysql.jdbc.password");
            CONNPOOLSIZE = Integer.parseInt(jdbcProperties.getProperty("mysql.jdbc.pool.size"));
            CONNPOOLMAXSIZE = Integer.parseInt(jdbcProperties.getProperty("mysql.jdbc.pool.maxsize"));

            //打开驱动
            Class.forName(DRIVER);
            connPool = new JdbcPool(CONNPOOLSIZE,CONNPOOLMAXSIZE,FULLURL);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回默认的数据库连接
     *
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException, IOException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        return conn;
    }

    /**
     * 从数据库连接池中获取Connection
     *
     * @return
     */
    public static Connection getConnectionFromPool() throws SQLException {
        return connPool.getConnection();
    }

    /**
     * 释放资源
     * 释放的资源包括Connection数据库连接对象，负责执行SQL命令的Statement对象，存储查询结果的ResultSet对象
     * @param conn
     * @param statement
     * @param resultSet
     */
    public static void release(Connection conn, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                //关闭存储查询结果的ResultSet对象
                resultSet.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            resultSet = null;
        }
        if (statement != null) {
            try {
                //关闭负责执行SQL命令的Statement对象
                statement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                //关闭Connection数据库连接对象
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行查询SQL(连接池)
     * @param sql
     * @param args
     * @return
     */
    public static List<Map<String,Object>> excuteSqlFromPool(String sql, Object... args) throws SQLException, InterruptedException {
        Connection conn = getConnectionFromPool();
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[0]);
        }
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Map<String, Object>> mapList = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();//ResultSet元数据，可以获取
        while (resultSet.next()) {
            Map<String,Object> map = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                map.put(metaData.getColumnName(i), resultSet.getString(i));
            }
            mapList.add(map);
        }
        Thread.sleep(5000);
        release(conn, preparedStatement, resultSet);
        return mapList;
    }

    /**
     * 查询单个map对象(程序池)
     * @param sql
     * @param args
     * @return
     * @throws SQLException
     */
    public  static  Map<String,Object> getMapFromPool(String sql, Object... args) throws SQLException, InterruptedException {
        List<Map<String, Object>> mapList = excuteSqlFromPool(sql, args);
        if (mapList != null && mapList.size() > 0) {
            return mapList.get(0);
        } else {
            return null;
        }
    }
}
