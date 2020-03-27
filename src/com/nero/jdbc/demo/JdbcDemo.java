package com.nero.jdbc.demo;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @created nero
 * @date 2018/8/23 11:46
 */
public class JdbcDemo {
    public static final String DRIVER = "com.mysql.jdbc.Driver";//mysql驱动名称
    public static final String URL = "jdbc:mysql://localhost:3306/cims_201803";//数据库
    public static final String USERNAME = "basic";//数据库账号
    public static final String PASSWORD = "000000";//数据库密码

    public static void test() throws SQLException {
        Connection conn = null;//数据库连接，执行sql时必须打开连接
        Statement statement = null;//执行SQL的主要对象
        PreparedStatement preparedStatement = null;//执行SQL的主要对象,与Statement不同的是可以通过？预留参数，通过setString设置参数值

        String sql = "INSERT INTO user(WorkNumber,Name) VALUES('statement','statement')";
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        //statement操作数据库
        statement = conn.createStatement();
        //增删改
        int reInt = statement.executeUpdate(sql);
        statement.close();//关闭statement

        //采用preparedStatement执行SQL
        sql = "INSERT INTO user(WorkNumber,Name) VALUES(?,?)";
        preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, "preparedStatement");
        preparedStatement.setString(2, "preparedStatement");
        reInt = preparedStatement.executeUpdate();
        preparedStatement.close();
        //查询
        sql = "SELECT Id,WorkNumber,Name FROM user LIMIT 20";
        preparedStatement = conn.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();

        preparedStatement.close();

        conn.close();
    }
}
