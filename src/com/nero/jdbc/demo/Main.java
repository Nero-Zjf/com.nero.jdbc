package com.nero.jdbc.demo;

import com.nero.jdbc.pool.JdbcHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws SQLException, InterruptedException {
        String sql = "select * from user";
        List<Map<String, Object>> mapList = JdbcHelper.excuteSqlFromPool(sql);
        for (Map<String, Object> map : mapList) {
            System.out.println("数据\n");
            for (String key : map.keySet()) {
                System.out.println(key + "=" + map.get(key)+";");
            }
        }
        System.out.println("Hello World!");
    }
}
