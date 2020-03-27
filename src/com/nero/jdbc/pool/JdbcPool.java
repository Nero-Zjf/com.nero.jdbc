package com.nero.jdbc.pool;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * jdbc连接池
 *
 * @created nero
 * @date 2018/8/23 16:28
 */
public class JdbcPool implements DataSource {

    /**
     * 连接池默认连接数量
     */
    private int connPoolSize;
    /**
     * 最大连接数量
     */
    private int connPoolMaxSize;
    /**
     * 连接字符串
     */
    private String connString;
    /**
     * 使用LinkedList集合来存放数据库链接，
     * 由于要频繁读写List集合，所以这里使用LinkedList存储数据库连接比较合适
     */
    private LinkedList<Connection> connLinkeList = new LinkedList<>();
    /**
     * 从连接池中取出使用的数量
     */
    private int connUsingCount = 0;

    /**
     * 限制使用默认构造函数
     */
    private JdbcPool() {

    }

    /**
     * 构造函数
     *
     * @param connPoolSize
     * @param connPoolMaxSize
     * @param connString
     */
    public JdbcPool(int connPoolSize, int connPoolMaxSize, String connString) {
        this.connPoolSize = connPoolSize;
        this.connPoolMaxSize = connPoolMaxSize;
        this.connString = connString;
        try {

            for (int i = 0; i < this.connPoolSize; i++) {
                addConnToPool();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加一个连接到连接池中
     *
     * @throws SQLException
     */
    private void addConnToPool() throws SQLException {
        Connection conn = DriverManager.getConnection(connString);
        System.out.println("数据库连接池获取到了链接" + conn);
        //将获取到的数据库连接加入到connLinkeList集合中，connLinkeList集合此时就是一个存放了数据库连接的连接池
        connLinkeList.add(conn);
        System.out.println("connLinkeList数据库连接池大小是" + connLinkeList.size());
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connLinkeList.size() == 0) {
            if (connUsingCount < connPoolMaxSize) {//连接数量未达到连接池允许的最大值，创建新连接
                addConnToPool();
            } else {
                throw new RuntimeException("对不起，数据库忙");
            }
        }

        //从connLinkeList集合中获取一个数据库连接
        final Connection conn = connLinkeList.removeFirst();
        connUsingCount++;
        System.out.println("connLinkeList数据库连接池大小是" + connLinkeList.size());
        //返回Connection对象的代理对象
        return (Connection) Proxy.newProxyInstance(JdbcPool.class.getClassLoader(), conn.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                if (!method.getName().equals("close")) {
                    return method.invoke(conn, args);
                } else {
                    //如果调用的是Connection对象的close方法，就把conn还给数据库连接池
                    connUsingCount--;
                    //如果线程池的当前线程数>=默认线程数，则释放返回的连接
                    if (connLinkeList.size() >= connPoolSize) {
                        conn.close();
                        System.out.println(conn + "线程被释放！！");
                    } else {
                        connLinkeList.add(conn);
                        System.out.println(conn + "被还给listConnections数据库连接池了！！");
                        System.out.println("connLinkeList数据库连接池大小为" + connLinkeList.size());
                    }
                    return null;
                }
            }
        });
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
